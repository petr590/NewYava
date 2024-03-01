package x590.newyava;

import x590.newyava.context.ClassContext;
import x590.newyava.io.DecompilationWriter;

public interface ContextualWritable {

	void write(DecompilationWriter out, ClassContext context);
}
