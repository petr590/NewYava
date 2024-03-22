package x590.newyava.annotation;

import lombok.Getter;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import x590.newyava.Importable;
import x590.newyava.context.ClassContext;
import x590.newyava.type.ClassType;

/** Значение аннотации по умолчанию */
public class DefaultValue extends AnnotationVisitor implements Importable {

	@Getter
	private AnnotationValue annotationValue;

	public DefaultValue() {
		super(Opcodes.ASM9);
	}

	private void set(AnnotationValue annotationValue) {
		if (this.annotationValue != null)
			throw new IllegalStateException("Reinitialization of default annotation value");

		this.annotationValue = annotationValue;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(annotationValue);
	}

	@Override
	public void visit(String name, Object value) {
		set(AnnotationValue.of(value));
	}

	@Override
	public void visitEnum(String name, String descriptor, String constantName) {
		set(new EnumValue(ClassType.valueOfL(descriptor), constantName));
	}

	@Override
	public AnnotationVisitor visitAnnotation(String name, String descriptor) {
		var annotation = new DecompilingAnnotation(descriptor);
		set(annotation);
		return annotation;
	}

	@Override
	public AnnotationVisitor visitArray(String name) {
		var array = new ArrayValue();
		set(array);
		return array;
	}
}
