package x590.newyava.annotation;

import org.jetbrains.annotations.Nullable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.Type;

record EnumValue(ClassType enumType, String name) implements AnnotationValue {
	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(enumType);
	}

	@Override
	public void write(DecompilationWriter out, Context context, @Nullable Type type) {
		out.record(enumType, context).record('.').record(name);
	}
}
