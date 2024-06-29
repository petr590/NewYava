package x590.newyava.annotation;

import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import x590.newyava.context.ClassContext;
import x590.newyava.context.ConstantWriteContext;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.ReferenceType;
import x590.newyava.type.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = false)
public class DecompilingAnnotation extends AnnotationVisitor implements AnnotationValue {
	private final ReferenceType annotationType;

	private final List<Parameter> parameters = new ArrayList<>();

	public DecompilingAnnotation(String descriptor) {
		super(Opcodes.ASM9);
		this.annotationType = ClassType.valueOfL(descriptor);
	}


	@Override
	public void visit(String name, Object value) {
		Type type = switch (value) {
			case Boolean   ignored -> PrimitiveType.BOOLEAN;
			case Byte      ignored -> PrimitiveType.BYTE;
			case Short     ignored -> PrimitiveType.SHORT;
			case Character ignored -> PrimitiveType.CHAR;
			case Integer   ignored -> PrimitiveType.INT;
			case Long      ignored -> PrimitiveType.LONG;
			case Float     ignored -> PrimitiveType.FLOAT;
			case Double    ignored -> PrimitiveType.DOUBLE;
			default -> Type.valueOf(value.getClass());
		};

		parameters.add(new Parameter(name, type, AnnotationValue.of(value)));
	}

	@Override
	public void visitEnum(String name, String descriptor, String constantName) {
		ClassType enumType = ClassType.valueOfL(descriptor);
		parameters.add(new Parameter(name, enumType, new EnumValue(enumType, constantName)));
	}

	@Override
	public AnnotationVisitor visitAnnotation(String name, String descriptor) {
		var annotation = new DecompilingAnnotation(descriptor);
		parameters.add(new Parameter(name, annotation.annotationType, annotation));
		return annotation;
	}

	@Override
	public AnnotationVisitor visitArray(String name) {
		var value = new ArrayValue();
		parameters.add(new Parameter(name, null, value));
		return value;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(annotationType).addImportsFor(parameters);
	}

	@Override
	public void write(DecompilationWriter out, ConstantWriteContext context) {
		out.record('@').record(annotationType, context);

		if (!parameters.isEmpty()) {
			out.record('(');

			if (parameters.size() == 1 && parameters.get(0).name().equals("value")) {
				out.record(parameters.get(0).value(), context);
			} else {
				out.record(parameters, context, ", ");
			}

			out.record(')');
		}
	}

	/**
	 * Записывает аннотации. Каждая аннотация записывается с новой строки с учётом отступа.
	 */
	public static void writeAnnotations(DecompilationWriter out, Context context,
	                                    @Unmodifiable List<DecompilingAnnotation> annotations) {

		writeAnnotations(out, context, annotations, false);
	}

	/**
	 * Записывает аннотации.
	 * @param inline если {@code true}, то все аннотации записываются через пробел.
	 * Иначе каждая аннотация записывается с новой строки с учётом отступа.
	 */
	public static void writeAnnotations(DecompilationWriter out, Context context,
	                                    @Unmodifiable List<DecompilingAnnotation> annotations,
	                                    boolean inline) {

		var constantWriteContext = new ConstantWriteContext(context);

		ObjIntConsumer<DecompilingAnnotation> writer = inline ?
				(annotation, index) -> out.record(annotation, constantWriteContext).space() :
				(annotation, index) -> out.record(annotation, constantWriteContext).ln().indent();

		out.record(annotations, writer);
	}


	public String toString() {
		return parameters.isEmpty() ?
				"@" + annotationType :
				String.format("@%s(%s)", annotationType,
						parameters.stream().map(Object::toString).collect(Collectors.joining(", ")));
	}
}
