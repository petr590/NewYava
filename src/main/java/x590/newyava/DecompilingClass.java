package x590.newyava;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.ClassReader;
import x590.newyava.context.ClassContext;
import x590.newyava.exception.IllegalModifiersException;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.visitor.DecompileClassVisitor;

import java.util.List;
import java.util.function.Predicate;

import static x590.newyava.Modifiers.*;
import static x590.newyava.Literals.*;

@Getter
public class DecompilingClass implements Writable {

	private final ClassContext classContext = new ClassContext(this);

	private final int version;
	private final int modifiers;

	private final ClassType thisType, superType;
	private final @Nullable ClassType visibleSuperType;
	private final @Unmodifiable List<ClassType> interfaces;

	private final @Unmodifiable List<DecompilingField> fields;
	private final @Unmodifiable List<DecompilingMethod> methods;

	private final @Unmodifiable List<DecompilingField> visibleFields;
	private @Unmodifiable List<DecompilingMethod> visibleMethods;

	private final @Nullable @Unmodifiable List<DecompilingField> enumConstants;

	public DecompilingClass(ClassReader classReader) {
		var visitor = new DecompileClassVisitor();
		classReader.accept(visitor, 0);

		this.version    = visitor.getVersion();
		this.modifiers  = visitor.getModifiers();
		this.thisType   = visitor.getThisType();
		this.superType  = visitor.getSuperType();
		this.visibleSuperType = realSuperType(modifiers, superType);
		this.interfaces = visitor.getInterfaces();

		this.fields         = visitor.getFields(classContext);
		this.methods        = visitor.getMethods(classContext);

		this.visibleFields  = fields.stream().filter(DecompilingField::keep).toList();

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


	public void decompile() {
		methods.forEach(method -> method.decompile(classContext));
		visibleMethods = methods.stream().filter(method -> method.keep(classContext)).toList();
	}

	public void addImports() {
		classContext.addImport(thisType).addImport(visibleSuperType).addImports(interfaces)
				.addImportsFor(visibleFields).addImportsFor(visibleMethods);
	}

	public void computeImports() {
		classContext.computeImports();
	}

	@Override
	public void write(DecompilationWriter out) {
		writeHeader(out);
		writeModifiers(out);

		out.recordsp(thisType.getSimpleName());

		if (visibleSuperType != null) {
			out.recordsp("extends").recordsp(visibleSuperType, classContext);
		}

		out.record('{').incIndent();

		writeFields(out);
		writeMethods(out);

		out.decIndent().record('}').ln();
	}


	private boolean importRequiredFor(ClassType classType) {
		var packageName = classType.getPackageName();
		return !packageName.equals("java.lang") && !packageName.equals(thisType.getPackageName());
	}


	private void writeHeader(DecompilationWriter out) {
		if (!thisType.getPackageName().isEmpty()) {
			out.recordsp("package").record(thisType.getPackageName()).record(';').ln().ln();
		}

		if (classContext.getImports().stream().filter(this::importRequiredFor)
				.peek(classType -> out.recordsp("import").record(classType.getName()).record(';').ln())
				.count() != 0) { // Не используйте здесь findAny().isPresent(), иначе peek обрабатывает только 1-й элемент

			out.ln(); // Дополнительный перенос строки, если есть импорты
		}
	}

	private void writeFields(DecompilationWriter out) {
		if (enumConstants != null) {
			out.ln().indent().record(enumConstants, ", ", (field, i) -> field.writeAsEnumConstant(out, classContext)).record(';');
		}

		if (out.writeIf(visibleFields, classContext, "", Predicate.not(DecompilingField::isEnum))) {
			out.ln();
		}
	}

	private void writeMethods(DecompilationWriter out) {
		out.record(visibleMethods, classContext).ln();
	}

	private void writeModifiers(DecompilationWriter out) {
		out.record(switch (modifiers & ACC_ACCESS) {
			case ACC_VISIBLE   -> "";
			case ACC_PUBLIC    -> LIT_PUBLIC + " ";
			case ACC_PRIVATE   -> LIT_PRIVATE + " ";
			case ACC_PROTECTED -> LIT_PROTECTED + " ";
			default -> throw new IllegalModifiersException(modifiers, EntryType.CLASS);
		});

		if ((modifiers & ACC_STATIC) != 0 && (modifiers & (ACC_ENUM | ACC_RECORD | ACC_INTERFACE)) == 0)
			out.record(LIT_STATIC + " ");

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
