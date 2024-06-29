package x590.newyava.io;

import x590.newyava.context.Context;

/**
 * Представляет объект, который можно записать в {@link DecompilationWriter}
 * с использованием контекста
 */
@FunctionalInterface
public interface GenericWritable<C extends Context> {

	/** Записывает объект в {@code out} с использованием {@code context} */
	void write(DecompilationWriter out, C context);
}
