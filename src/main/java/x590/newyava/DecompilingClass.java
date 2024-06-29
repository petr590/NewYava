package x590.newyava;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.ClassReader;
import x590.newyava.context.ClassContext;
import x590.newyava.exception.DecompilationException;
import x590.newyava.exception.IllegalModifiersException;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.io.Writable;
import x590.newyava.modifiers.EntryType;
import x590.newyava.type.ClassType;
import x590.newyava.annotation.DecompilingAnnotation;
import x590.newyava.visitor.DecompileClassVisitor;
import x590.newyava.visitor.ModuleInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static x590.newyava.modifiers.Modifiers.*;
import static x590.newyava.Literals.*;

/**
 * Декомпилируемый класс
 */
@Getter
public class DecompilingClass implements Writable {

	private final ClassContext classContext;

	/** Мажорная версия {@code class}-файла */
	private final int version;

	/** Набор модификаторов класса */
	private final int modifiers;

	/** Тип этого класса */
	private final ClassType thisType;

	/** Тип суперкласса. Для {@link Object} равен самому себе. */
	private final ClassType superType;

	/** Реализуемые интерфейсы. */
	private final @Unmodifiable List<ClassType> interfaces;

	/** Видимый суперкласс. Если суперкласса нет или он не видим, это поле равно {@code null} */
	private final @Nullable ClassType visibleSuperType;

	/** Видимые интерфейсы - все, кроме {@link java.lang.annotation.Annotation Annotation}. */
	private final @Unmodifiable List<ClassType> visibleInterfaces;

	private final @Nullable ClassType outerClassType;

	/** Все поля класса */
	private final @Unmodifiable List<DecompilingField> fields;

	/** Все методы класса */
	private final @Unmodifiable List<DecompilingMethod> methods;

	/** Аннотации */
	private final @Unmodifiable List<DecompilingAnnotation> annotations;

	/** Разрешённые подклассы. Если список не пуст, то данный класс является sealed */
	private final @Unmodifiable List<ClassType> permittedSubclasses;

	/** Видимые поля класса кроме {@code enum}-констант */
	private final @Unmodifiable List<DecompilingField> visibleFields;

	/** Видимые методы. Инициализируется только после декомпиляции всех методов */
	private @Unmodifiable List<DecompilingMethod> visibleMethods;

	/** Список {@code enum}-констант класса или {@code null},
	 * если класс не может содержать {@code enum}-констант */
	private final @Nullable @Unmodifiable List<DecompilingField> enumConstants;

	/** Список {@code enum}-констант класса или {@code null},
	 * если класс не может содержать {@code enum}-констант */
	private final @Nullable @Unmodifiable List<DecompilingField> recordComponents;

	private final @Nullable ModuleInfo moduleInfo;

	public DecompilingClass(Decompiler decompiler, ClassReader classReader) {
		this.classContext = new ClassContext(decompiler, this);

		var visitor = new DecompileClassVisitor(decompiler);
		classReader.accept(visitor, 0);

		this.version   = visitor.getVersion();
		this.modifiers = visitor.getModifiers();
		this.thisType  = visitor.getThisType();

		this.superType        = visitor.getSuperType();
		this.visibleSuperType = realSuperType(modifiers, superType);

		this.interfaces        = visitor.getInterfaces();
		this.visibleInterfaces =
				(modifiers & ACC_ANNOTATION) == 0 ? interfaces :
				interfaces.stream().filter(interf -> !interf.equals(ClassType.ANNOTATION)).toList();

		this.outerClassType = visitor.getOuterClassType();

		this.fields              = visitor.getFields();
		this.methods             = visitor.getMethods();
		this.annotations         = visitor.getAnnotations();
		this.permittedSubclasses = visitor.getPermittedSubclasses();
		this.moduleInfo          = visitor.getModuleInfo();

		// Анонимные enum-классы тоже имеют влаг ACC_ENUM, но не содержат enum-констант.
		// Для такого случая нужна проверка суперкласса
		if ((modifiers & ACC_ENUM) != 0 && superType.equals(ClassType.ENUM)) {
			this.visibleFields = fields.stream().filter(field -> field.keep() && !field.isEnum()).toList();
			this.enumConstants = fields.stream().filter(DecompilingField::isEnum).toList();
			this.recordComponents = null;

		} else if ((modifiers & ACC_RECORD) != 0) {
			this.visibleFields = fields.stream().filter(field -> field.keep() && field.isStatic()).toList();
			this.enumConstants = null;

			this.recordComponents = fields.stream().filter(Predicate.not(DecompilingField::isStatic))
					.peek(field -> {
						if (field.getModifiers() != (ACC_PRIVATE | ACC_FINAL)) {
							throw new DecompilationException("Illegal modifiers for record component: " +
									EntryType.FIELD.modifiersToString(field.getModifiers()));
						}
					}).toList();

		} else {
			this.visibleFields = fields.stream().filter(DecompilingField::keep).toList();
			this.enumConstants = null;
			this.recordComponents = null;
		}
	}

	private static @Nullable ClassType realSuperType(int modifiers, ClassType formalSuperType) {
		if ((modifiers & ACC_ENUM) != 0 && formalSuperType == ClassType.ENUM ||
			(modifiers & ACC_RECORD) != 0 && formalSuperType == ClassType.RECORD ||
			formalSuperType == ClassType.OBJECT) {

			return null;
		}

		return formalSuperType;
	}


	private @Nullable DecompilingClass outerClass;

	private final List<DecompilingClass> nestedClasses = new ArrayList<>();

	/** Если {@code true}, то это класс верхнего уровня. */
	private boolean topLevel = true;

	public void initNested(@Unmodifiable Map<ClassType, DecompilingClass> classMap) {
		if (outerClassType != null) {
			var outerClass = this.outerClass = classMap.get(outerClassType);

			if (outerClass != null) {
				if (!thisType.isEnclosedInMethod()) {
					outerClass.nestedClasses.add(this);
				}

				this.topLevel = false;
				this.classContext.setOuterContext(outerClass.classContext);
			}
		}
	}

	public boolean keep() {
		return topLevel;
	}


	/* ----------------------------------------------- decompilation ----------------------------------------------- */

	public void decompile() {
		methods.forEach(method -> method.decompile(classContext));
		visibleMethods = methods.stream().filter(method -> method.keep(classContext, recordComponents)).toList();
	}

	public void processVariables() {
		methods.forEach(method -> method.beforeVariablesInit(classContext));

		var comparator = Comparator.comparingInt(DecompilingMethod::getVariablesInitPriority).reversed();

		methods.stream().sorted(comparator).forEach(method -> method.initVariables(classContext));
		methods.stream().sorted(comparator).forEach(method -> method.inferVariableTypesAndNames(classContext));
		fields.forEach(DecompilingField::inferVariableTypes);
	}

	public void addImports() {
		classContext.addImport(thisType).addImport(visibleSuperType).addImports(visibleInterfaces)
				.addImportsFor(visibleFields).addImportsFor(enumConstants).addImportsFor(recordComponents)
				.addImportsFor(visibleMethods).addImportsFor(annotations).addImports(permittedSubclasses)
				.addImportsFor(moduleInfo);
	}

	public void computeImports() {
		classContext.computeImports();
	}


	/* --------------------------------------------------- write --------------------------------------------------- */
	@Override
	public void write(DecompilationWriter out) {
		if (moduleInfo != null) {
			writeAsModuleInfo(out);
		} else if (thisType.isPackageInfo()) {
			writeAsPackageInfo(out);
		} else {
			writeAsClass(out);
		}
	}


	private void writeAsModuleInfo(DecompilationWriter out) {
		writeHeader(out);

		DecompilingAnnotation.writeAnnotations(out, classContext, annotations);

		assert moduleInfo != null;
		moduleInfo.write(out, classContext);
	}

	private void writeAsPackageInfo(DecompilationWriter out) {
		DecompilingAnnotation.writeAnnotations(out, classContext, annotations);

		writeHeader(out);
	}


	private void writeAsClass(DecompilationWriter out) {
		if (topLevel) {
			writeHeader(out);
		} else {
			out.ln().ln();
		}

		out.indent();
		DecompilingAnnotation.writeAnnotations(out, classContext, annotations);

		writeModifiers(out);
		out.record(thisType.getSimpleName());

		if (recordComponents != null) {
			out .record('(')
					.record(recordComponents, ", ", (field, index) -> field.writeAsRecordComponent(out, classContext))
					.record(')');
		}

		out.space();

		if (visibleSuperType != null) {
			out.recordSp("extends").recordSp(visibleSuperType, classContext);
		}

		if (!visibleInterfaces.isEmpty()) {
			// Интерфейс не реализует другие интерфейсы, он наследуется от них
			out .record((modifiers & ACC_INTERFACE) != 0 ? "extends" : "implements").space()
					.record(visibleInterfaces, classContext, ", ").space();
		}

		// Если все классы внутри данного, то permits можно опустить
		if (isSealed() && permittedSubclasses.stream().anyMatch(permitted -> !permitted.isInside(thisType))) {
			out.recordSp("permits").record(permittedSubclasses, classContext, ", ").space();
		}

		writeBody(out);
	}


	/** Записывает тело класса, т.е. всё, что между фигурными скобками */
	public void writeBody(DecompilationWriter out) {
		classContext.enter();
		out.record('{').incIndent();

		boolean wrote = writeFields(out) | writeMethods(out) | writeNestedClasses(out);

		out.decIndent();

		if (wrote)
			out.indent();

		out.record('}').lnIf(topLevel);
		classContext.exit();
	}


	private boolean importRequiredFor(ClassType classType) {
		var packageName = classType.getPackageName();
		return !packageName.isEmpty() && !packageName.equals("java.lang") &&
				!(packageName.equals(thisType.getPackageName()) && !classType.isNested());
	}


	/** Записывает {@code package} и {@code import}-ы */
	private void writeHeader(DecompilationWriter out) {
		if (!thisType.getPackageName().isEmpty()) {
			out.recordSp("package").record(thisType.getPackageName()).record(';').ln().ln();
		}

		var count = classContext.getImports().stream()
				.filter(this::importRequiredFor)
				.sorted(Comparator.comparing(ClassType::getName))
				.peek(classType -> out.recordSp("import").record(classType.getName()).record(';').ln())
				.count(); // Не используйте здесь findAny().isPresent(), иначе peek обрабатывает только 1-й элемент

		out.lnIf(count != 0);
	}

	private boolean writeFields(DecompilationWriter out) {
		boolean enumsWrote = false;

		if (enumConstants != null) {
			boolean allEmpty = visibleFields.isEmpty() && visibleMethods.isEmpty() && nestedClasses.isEmpty();

			if (!(allEmpty && enumConstants.isEmpty())) {
				boolean inline = enumConstants.stream().allMatch(DecompilingField::canInlineEnumConstant);

				if (inline) {
					out.ln().indent().record(enumConstants, ", ",
							(field, i) -> field.writeAsEnumConstant(out, classContext, 0));

				} else {
					int minWidth = enumConstants.stream()
							.mapToInt(field -> field.getDescriptor().name().length()).max().orElse(-1) + 1;

					out.ln().indent().record(enumConstants, ",\n" + out.getIndent(),
							(field, i) -> field.writeAsEnumConstant(out, classContext, minWidth));
				}

				if (!allEmpty)
					out.record(';');

				out.ln();

				enumsWrote = true;
			}
		}

		return enumsWrote | out.record(visibleFields, classContext).lnIf(!visibleFields.isEmpty());
	}

	private boolean writeMethods(DecompilationWriter out) {
		return out.record(visibleMethods, classContext).lnIf(!visibleMethods.isEmpty());
	}

	private boolean writeNestedClasses(DecompilationWriter out) {
		return out.record(nestedClasses).lnIf(!nestedClasses.isEmpty());
	}

	private boolean isOuterInterface() {
		return outerClass != null && (outerClass.getModifiers() & ACC_INTERFACE) != 0;
	}

	private boolean isSealed() {
		return !permittedSubclasses.isEmpty() && (modifiers & ACC_ENUM) == 0;
	}

	/** Записывает модификаторы класса, в том числе {@code interface}, {@code enum} и др. */
	private void writeModifiers(DecompilationWriter out) {
		out.record(switch (modifiers & ACC_ACCESS) {
			case ACC_VISIBLE   -> "";
			case ACC_PUBLIC    -> isOuterInterface() ? "" : LIT_PUBLIC + " ";
			case ACC_PRIVATE   -> LIT_PRIVATE + " ";
			case ACC_PROTECTED -> LIT_PROTECTED + " ";
			default -> throw new IllegalModifiersException(modifiers, EntryType.CLASS);
		});


		if ((modifiers & ACC_STATIC) != 0 &&
				(modifiers & (ACC_ENUM | ACC_RECORD | ACC_INTERFACE)) == 0 && !isOuterInterface()) {
			out.record(LIT_STATIC + " ");
		}


		if (isSealed()) {
			out.record(LIT_SEALED + " ");

		} else if ((modifiers & ACC_FINAL) == 0) {
			var superClass = classContext.findClass(superType);

			if (superClass.isPresent() && superClass.get().isSealed()) {
				out.record(LIT_NON_SEALED + " ");
			}
		}


		out.recordSp(switch (modifiers & (ACC_FINAL | ACC_ENUM | ACC_RECORD | ACC_ABSTRACT | ACC_INTERFACE | ACC_ANNOTATION)) {
			case ACC_NONE                                                -> LIT_CLASS;
			case ACC_FINAL                                               -> LIT_FINAL + " " + LIT_CLASS;
			case ACC_FINAL | ACC_RECORD                                  -> LIT_RECORD;
			case ACC_ENUM, ACC_FINAL | ACC_ENUM, ACC_ABSTRACT | ACC_ENUM -> LIT_ENUM;
			case ACC_ABSTRACT                                            -> LIT_ABSTRACT + " " + LIT_CLASS;
			case ACC_ABSTRACT | ACC_INTERFACE                            -> LIT_INTERFACE;
			case ACC_ABSTRACT | ACC_INTERFACE | ACC_ANNOTATION           -> LIT_ANNOTATION;
			default -> throw new IllegalModifiersException(modifiers, EntryType.CLASS);
		});
	}

	@Override
	public String toString() {
		return EntryType.CLASS.modifiersToString(modifiers) + " " + thisType;
	}
}
