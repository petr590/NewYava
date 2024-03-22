package x590.newyava.constant;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LongConstant extends Constant {

	private static final Long2ObjectMap<LongConstant> CACHE = new Long2ObjectOpenHashMap<>();

	public static LongConstant valueOf(long value) {
		return CACHE.computeIfAbsent(value, LongConstant::new);
	}

	public static final LongConstant
			ZERO = valueOf(0),
			ONE = valueOf(1);

	private final long value;

	@Override
	public Type getType() {
		return PrimitiveType.LONG;
	}

	@Override
	public void addImports(ClassContext context) {}

	@Override
	public void write(DecompilationWriter out, Context context, Type type) {
		out.record(String.valueOf(value)).record('l');
	}
}
