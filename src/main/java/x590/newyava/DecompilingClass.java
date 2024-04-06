package x590.newyava;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.ClassReader;
import x590.newyava.context.ClassContext;
import x590.newyava.exception.IllegalModifiersException;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.annotation.DecompilingAnnotation;
import x590.newyava.visitor.DecompileClassVisitor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static x590.newyava.Modifiers.*;
import static x590.newyava.Literals.*;

@Getter
public class DecompilingClass implements Writable {

	private final ClassContext classContext = new ClassContext(this);

	private final int version;
	private final int modifiers;

	private final ClassType thisType, superType;
	private final @Unmodifiable List<ClassType> interfaces;

	private final @Nullable ClassType visibleSuperType;
	private final @Unmodifiable List<ClassType> visibleInterfaces;

	private final @Nullable ClassType outerClassType;

	private final @Unmodifiable List<DecompilingField> fields;
	private final @Unmodifiable List<DecompilingMethod> methods;
	private final @Unmodifiable List<DecompilingAnnotation> annotations;

	private final @Unmodifiable List<DecompilingField> visibleFields;
	private @Unmodifiable List<DecompilingMethod> visibleMethods;

	private final @Nullable @Unmodifiable List<DecompilingField> enumConstants;

	public DecompilingClass(ClassReader classReader) {
		var visitor = new DecompileClassVisitor();
		classReader.accept(visitor, 0);

		this.version    = visitor.getVersion();
		this.modifiers  = visitor.getModifiers();
		this.thisType   = visitor.getThisType();

		this.superType        = visitor.getSuperType();
		this.visibleSuperType = realSuperType(modifiers, superType);

		this.interfaces        = visitor.getInterfaces();
		this.visibleInterfaces =
				(modifiers & ACC_ANNOTATION) == 0 ? interfaces :
				interfaces.stream().filter(interf -> !interf.equals(ClassType.ANNOTATION)).toList();

		this.outerClassType = visitor.getOuterClassType();

		this.fields      = visitor.getFields(classContext);
		this.methods     = visitor.getMethods(classContext);
		this.annotations = visitor.getAnnotations();

		this.visibleFields = fields.stream().filter(field -> field.keep() && !field.isEnum()).toList();

		if ((modifiers & ACC_ENUM) != 0) {
			this.enumConstants = fields.stream().filter(DecompilingField::isEnum).toList();
		} else {
			this.enumConstants = null;
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

	private boolean upperLevel = true;

	public void initNested(@Unmodifiable Map<ClassType, DecompilingClass> classMap) {
		if (outerClassType != null) {
			var outerClass = this.outerClass = classMap.get(outerClassType);

			if (outerClass != null) {
				outerClass.nestedClasses.add(this);
				this.upperLevel = false;
				this.classContext.setOuterContext(outerClass.classContext);
			}
		}
	}

	public boolean keep() {
		return upperLevel;
	}


	public void decompile() {
		methods.forEach(method -> method.decompile(classContext));
		visibleMethods = methods.stream().filter(method -> method.keep(classContext)).toList();
	}

	public void initVariables() {
		methods.forEach(DecompilingMethod::beforeVariablesInit);

		methods.stream()
				.sorted(Comparator.comparingInt(DecompilingMethod::getVariablesInitPriority).reversed())
				.forEach(DecompilingMethod::initVariables);
	}

	public void addImports() {
		classContext.addImport(thisType).addImport(visibleSuperType).addImports(visibleInterfaces)
				.addImportsFor(enumConstants).addImportsFor(visibleFields)
				.addImportsFor(visibleMethods).addImportsFor(annotations);
	}

	public void computeImports() {
		classContext.computeImports();
	}

	@Override
	public void write(DecompilationWriter out) {
		if (upperLevel) {
			writeHeader(out);
		} else {
			out.ln().ln();
		}

		DecompilingAnnotation.writeAnnotations(out.indent(), classContext, annotations);

		writeModifiers(out);

		out.recordSp(thisType.getSimpleName());

		if (visibleSuperType != null) {
			out.recordSp("extends").recordSp(visibleSuperType, classContext);
		}

		if (!visibleInterfaces.isEmpty()) {
			out.recordSp((modifiers & ACC_INTERFACE) != 0 ? "extends" : "implements")
				.record(visibleInterfaces, classContext, ", ").recordSp();
		}

		out.record('{').incIndent();

		boolean wrote = writeFields(out);
		wrote |= writeMethods(out);
		wrote |= writeNestedClasses(out);

		out.decIndent();

		if (wrote)
			out.indent();

		out.record('}').lnIf(upperLevel);
	}


	private boolean importRequiredFor(ClassType classType) {
		var packageName = classType.getPackageName();
		return !packageName.isEmpty() && !packageName.equals("java.lang") && !packageName.equals(thisType.getPackageName());
	}


	private void writeHeader(DecompilationWriter out) {
		if (!thisType.getPackageName().isEmpty()) {
			out.recordSp("package").record(thisType.getPackageName()).record(';').ln().ln();
		}

		var count = classContext.getImports().stream()
				.filter(this::importRequiredFor)
				.sorted(Comparator.comparing(ClassType::getName))
				.peek(classType -> out.recordSp("import").record(classType.getName()).record(';').ln())
				.count(); // Не используйте здесь findAny().isPresent(), иначе peek обрабатывает только 1-й элемент

		if (count != 0) {
			out.ln(); // Дополнительный перенос строки, если есть импорты
		}
	}

	private boolean writeFields(DecompilationWriter out) {
		boolean enumsWrote = false;

		if (enumConstants != null) {
			boolean allEmpty = visibleFields.isEmpty() && visibleMethods.isEmpty() && nestedClasses.isEmpty();

			if (!(allEmpty && enumConstants.isEmpty())) {
				out.ln().indent().record(enumConstants, ", ", (field, i) -> field.writeAsEnumConstant(out, classContext));

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

		out.record(switch (modifiers & (ACC_FINAL | ACC_ENUM | ACC_RECORD | ACC_ABSTRACT | ACC_INTERFACE | ACC_ANNOTATION)) {
			case ACC_NONE                                      -> LIT_CLASS;
			case ACC_ENUM, ACC_FINAL | ACC_ENUM                -> LIT_ENUM;
			case ACC_FINAL | ACC_RECORD                        -> LIT_RECORD;
			case ACC_FINAL                                     -> LIT_FINAL + " " + LIT_CLASS;
			case ACC_ABSTRACT                                  -> LIT_ABSTRACT + " " + LIT_CLASS;
			case ACC_ABSTRACT | ACC_INTERFACE                  -> LIT_INTERFACE;
			case ACC_ABSTRACT | ACC_INTERFACE | ACC_ANNOTATION -> LIT_ANNOTATION;
			default -> throw new IllegalModifiersException(modifiers, EntryType.CLASS);
		}).record(' ');
	}
}
