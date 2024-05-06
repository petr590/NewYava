package x590.newyava;

import x590.newyava.io.DecompilationWriter;

@FunctionalInterface
public interface Writable {

	/** Записывает объект в {@code out} */
	void write(DecompilationWriter out);
}
