package x590.newyava.annotation;

import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import x590.newyava.context.ClassContext;
import x590.newyava.context.ConstantWriteContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.Type;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
class ArrayValue extends AnnotationVisitor implements AnnotationValue {
	private final List<AnnotationValue> values;

	private @Nullable Type elementType;

	ArrayValue() {
		super(Opcodes.ASM9);
		this.values = new ArrayList<>();
	}

	ArrayValue(List<AnnotationValue> values, @Nullable Type elementType) {
		super(Opcodes.ASM9);
		this.values = values;
		this.elementType = elementType;
	}

	@Override
	public void visit(String name, Object value) {
		if (elementType == null) {
			elementType = AnnotationValue.typeFor(value);
		}

		values.add(AnnotationValue.of(value));
	}

	@Override
	public void visitEnum(String name, String descriptor, String constantName) {
		values.add(new EnumValue(ClassType.valueOfL(descriptor), constantName));
	}

	@Override
	public AnnotationVisitor visitAnnotation(String name, String descriptor) {
		var annotation = new DecompilingAnnotation(descriptor);
		values.add(annotation);
		return annotation;
	}

	@Override
	public AnnotationVisitor visitArray(String name) {
		var value = new ArrayValue();
		values.add(value);
		return value;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(values);
	}

	@Override
	public void write(DecompilationWriter out, ConstantWriteContext context) {
		switch (values.size()) {
			case 0 -> out.record("{}");
			case 1 -> out.record(values.get(0), new ConstantWriteContext(context, elementType));
			default -> out.record("{ ").record(values, new ConstantWriteContext(context, elementType), ", ").record(" }");
		}
	}
}
