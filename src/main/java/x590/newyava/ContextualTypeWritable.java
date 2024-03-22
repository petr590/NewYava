package x590.newyava;

import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

public interface ContextualTypeWritable {
	void write(DecompilationWriter out, Context context, Type type);
}
