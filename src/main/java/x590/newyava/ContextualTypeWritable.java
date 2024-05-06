package x590.newyava;

import org.jetbrains.annotations.Nullable;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

@FunctionalInterface
public interface ContextualTypeWritable {

	/** Записывает объект в {@code out} с использованием {@code context} и {@code type}.
	 * {@code type} влияет только на {@link x590.newyava.constant.IntConstant IntConstant}. */
	void write(DecompilationWriter out, Context context, @Nullable Type type);
}
