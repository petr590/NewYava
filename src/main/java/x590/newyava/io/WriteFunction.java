package x590.newyava.io;

import x590.newyava.ContextualWritable;
import x590.newyava.context.ClassContext;

@FunctionalInterface
public interface WriteFunction<T extends ContextualWritable> {
	void write(DecompilationWriter out, T value, ClassContext context, int index);
}
