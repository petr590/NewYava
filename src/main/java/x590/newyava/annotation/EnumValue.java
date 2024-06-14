package x590.newyava.annotation;

import x590.newyava.context.ClassContext;
import x590.newyava.context.ConstantWriteContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;

record EnumValue(ClassType enumType, String name) implements AnnotationValue {
	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(enumType);
	}

	@Override
	public void write(DecompilationWriter out, ConstantWriteContext context) {
		out.record(enumType, context).record('.').record(name);
	}
}
