package x590.newyava.annotation;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import x590.newyava.Util;
import x590.newyava.context.ClassContext;
import x590.newyava.context.ConstantWriteContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = false)
class ArrayValue extends AnnotationVisitor implements AnnotationValue {
	@Getter(AccessLevel.PACKAGE)
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
		return Util.addAndGetBack(values, new DecompilingAnnotation(descriptor));
	}

	@Override
	public AnnotationVisitor visitArray(String name) {
		return Util.addAndGetBack(values, new ArrayValue());
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

	@Override
	public String toString() {
		return String.format("ArrayValue(type = %s, values = [%s])",
				elementType,
				values.stream().map(Object::toString).collect(Collectors.joining(", ")));
	}
}
