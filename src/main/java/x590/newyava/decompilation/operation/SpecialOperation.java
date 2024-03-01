package x590.newyava.decompilation.operation;

import x590.newyava.context.ClassContext;
import x590.newyava.exception.DecompilationException;
import x590.newyava.io.DecompilationWriter;

/**
 * Специальная операция, которая не поддерживает запись.
 */
public abstract class SpecialOperation implements Operation {

	/**
	 * @throws DecompilationException всегда. Это поведение может быть переопределено в подклассах.
	 */
	@Override
	public void write(DecompilationWriter out, ClassContext context) {
		throw new DecompilationException("Cannot write " + this.getClass());
	}
}
