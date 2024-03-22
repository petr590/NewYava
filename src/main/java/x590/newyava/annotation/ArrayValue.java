package x590.newyava.annotation;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.Type;

import java.util.ArrayList;
import java.util.List;

class ArrayValue extends AnnotationVisitor implements AnnotationValue {
	private final List<AnnotationValue> values;

	ArrayValue() {
		this(new ArrayList<>());
	}

	ArrayValue(List<AnnotationValue> values) {
		super(Opcodes.ASM9);
		this.values = values;
	}

	@Override
	public void visit(String name, Object value) {
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
	public void write(DecompilationWriter out, Context context, Type type) {
		if (values.isEmpty()) {
			out.record("{}");
		} else {
			out.record("{ ").record(values, context, type, ", ").record(" }");
		}
	}
}
