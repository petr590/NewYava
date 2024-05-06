package x590.newyava.annotation;

import x590.newyava.ContextualWritable;
import x590.newyava.Importable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

record Parameter(String name, Type type, AnnotationValue value) implements ContextualWritable, Importable {
	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(value);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.record(name).record(" = ").record(value, context, type);
	}
}
