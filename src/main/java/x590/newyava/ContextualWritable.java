package x590.newyava;

import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;

@FunctionalInterface
public interface ContextualWritable {

	/** Записывает объект в {@code out} с использованием {@code context} */
	void write(DecompilationWriter out, Context context);
}
