package x590.newyava.constant;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import x590.newyava.context.ClassContext;
import x590.newyava.context.ConstantWriteContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LongConstant extends Constant {

	private static final Long2ObjectMap<LongConstant> CACHE = new Long2ObjectOpenHashMap<>();

	public static LongConstant valueOf(long value) {
		return CACHE.computeIfAbsent(value, LongConstant::new);
	}

	public static final LongConstant
			MINUS_ONE = valueOf(-1),
			ZERO = valueOf(0),
			ONE = valueOf(1);

	private final long value;

	@Override
	public Type getType() {
		return PrimitiveType.LONG;
	}

	@Override
	public boolean valueEquals(int value) {
		return this.value == value;
	}

	@Override
	public void addImports(ClassContext context) {}

	@Override
	public void write(DecompilationWriter out, ConstantWriteContext context) {
		long val = value;
		int intVal = (int)val;

		if (context.isImplicitCastAllowed() && intVal == val) {
			out.record(String.valueOf(intVal));
		} else {
			out.record(String.valueOf(val)).record('l');
		}
	}

	@Override
	public String toString() {
		return "LongConstant(" + value + ")";
	}
}
