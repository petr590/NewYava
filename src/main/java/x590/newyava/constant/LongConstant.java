package x590.newyava.constant;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import x590.newyava.context.ConstantWriteContext;
import x590.newyava.context.Context;
import x590.newyava.descriptor.FieldDescriptor;
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
	public Type getImplicitType() {
		return (int)value == value ? PrimitiveType.INT : getType();
	}

	@Override
	public boolean valueEquals(int value) {
		return this.value == value;
	}

	@Override
	protected @Nullable FieldDescriptor getConstant(Context context) {
		return context.getConstant(value);
	}

	@Override
	public void write(DecompilationWriter out, ConstantWriteContext context) {
		writeConstantOrValue(out, context, () -> {
			out.record(Long.toString(value));
			writePostfixIfNecessary(out, context, (int) value != value);
		});
	}

	@Override
	public void writeHex(DecompilationWriter out, ConstantWriteContext context) {
		String hex = Long.toHexString(value);
		out.record("0x").record(hex);
		writePostfixIfNecessary(out, context, hex.length() > 8);
	}

	private void writePostfixIfNecessary(DecompilationWriter out, ConstantWriteContext context, boolean condition) {
		if (condition || !context.isImplicitCastAllowed()) {
			out.record('l');
		}
	}

	@Override
	public String toString() {
		return "LongConstant(" + value + ")";
	}
}
