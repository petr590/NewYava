package x590.newyava.decompilation.operation;

import x590.newyava.context.MethodWriteContext;
import x590.newyava.exception.DecompilationException;
import x590.newyava.io.DecompilationWriter;

/**
 * Специальная операция, которая не поддерживает запись.
 */
public interface SpecialOperation extends Operation {

	/**
	 * @throws DecompilationException всегда. Это поведение может быть переопределено в подклассах.
	 */
	@Override
	default void write(DecompilationWriter out, MethodWriteContext context) {
		throw new DecompilationException("Cannot write " + this.getClass().getCanonicalName());
	}
}
