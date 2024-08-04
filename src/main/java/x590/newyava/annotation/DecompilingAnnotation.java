package x590.newyava.annotation;

import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import x590.newyava.constant.ClassConstant;
import x590.newyava.context.AnnotationWriteContext;
import x590.newyava.context.ClassContext;
import x590.newyava.context.ConstantWriteContext;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;
import x590.newyava.util.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = false)
public class DecompilingAnnotation extends AnnotationVisitor implements AnnotationValue {
	private final ClassType annotationType;

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

	private @Nullable AnnotationValue getValueParameter() {
		return parameters.stream()
				.filter(parameter -> parameter.name().equals("value"))
				.findFirst().map(Parameter::value).orElse(null);
	}

	private @Nullable List<DecompilingAnnotation> getRepeatableAnnotations(ConstantWriteContext context) {
		if (parameters.size() == 1) {
			var parameter = parameters.get(0);

			if (parameter.name().equals("value") &&
				parameter.value() instanceof ArrayValue arrayValue &&
				!arrayValue.getValues().isEmpty() &&
				arrayValue.getValues().stream().allMatch(value -> value instanceof DecompilingAnnotation)) {

				var annotation = (DecompilingAnnotation) arrayValue.getValues().get(0);

				var foundRepeatableAnnotation =
						context.findClass(annotation.annotationType).flatMap(
								clazz -> clazz.getAnnotations().stream()
										.filter(ann -> ann.annotationType.equals(ClassType.REPEATABLE))
										.findAny()
						);

				if (foundRepeatableAnnotation.isPresent() &&
					foundRepeatableAnnotation.get().getValueParameter() instanceof ClassConstant clazz &&
					clazz.getTypeOfClass().equals(annotationType)) {

					@SuppressWarnings("unchecked")
					var result = (List<DecompilingAnnotation>) (List<?>) arrayValue.getValues();
					return result;
				}
			}
		}

		return null;
	}

	@Override
	public void write(DecompilationWriter out, ConstantWriteContext context) {
		var annotations = getRepeatableAnnotations(context);

		if (annotations != null && context instanceof AnnotationWriteContext annotationContext) {
			out.record(annotations, context,
					annotationContext.isInline() ? " " : "\n" + out.getIndent());
			return;
		}

		out.record('@').record(annotationType, context);

		if (!parameters.isEmpty()) {
			out.record('(');

			if (Utils.isSingle(parameters, parameter -> parameter.name().equals("value"))) {
				out.record(parameters.get(0).value(), context);
			} else {
				out.record(parameters, context, ", ");
			}

			out.record(')');
		}
	}


	public String toString() {
		return parameters.isEmpty() ?
				"@" + annotationType :
				String.format("@%s(%s)", annotationType,
						parameters.stream().map(Object::toString).collect(Collectors.joining(", ")));
	}

	/**
	 * Записывает аннотации. Каждая аннотация записывается с новой строки с учётом отступа.
	 */
	public static void writeAnnotations(
			DecompilationWriter out, Context context,
	        @Unmodifiable Collection<? extends DecompilingAnnotation> annotations
	) {
		writeAnnotations(out, context, annotations, false);
	}

	/**
	 * Записывает аннотации.
	 * @param inline если {@code true}, то все аннотации записываются через пробел.
	 * Иначе каждая аннотация записывается с новой строки с учётом отступа.
	 */
	public static void writeAnnotations(
			DecompilationWriter out, Context context,
	        @Unmodifiable Collection<? extends DecompilingAnnotation> annotations,
	        boolean inline
	) {

		var annotationWriteContext = new AnnotationWriteContext(context, inline);

		ObjIntConsumer<DecompilingAnnotation> writer = inline ?
				(annotation, index) -> out.record(annotation, annotationWriteContext).space() :
				(annotation, index) -> out.record(annotation, annotationWriteContext).ln().indent();

		out.record(annotations, writer);
	}
}
