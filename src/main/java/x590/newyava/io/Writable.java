package x590.newyava.io;

/**
 * Представляет объект, который можно записать в {@link DecompilationWriter}
 */
@FunctionalInterface
public interface Writable {

	/** Записывает объект в {@code out} */
	void write(DecompilationWriter out);
}
