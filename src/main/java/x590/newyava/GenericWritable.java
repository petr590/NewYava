package x590.newyava;

import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;

@FunctionalInterface
public interface GenericWritable<C extends Context> {

	/** Записывает объект в {@code out} с использованием {@code context} */
	void write(DecompilationWriter out, C context);
}
