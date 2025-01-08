package x590.newyava.constant;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import x590.newyava.context.ConstantWriteContext;
import x590.newyava.context.Context;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;
import x590.newyava.util.JavaEscapeUtils;

@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class IntConstant extends Constant implements Comparable<IntConstant> {
	private static final Int2ObjectMap<IntConstant> CACHE = new Int2ObjectOpenHashMap<>();

	public static IntConstant valueOf(int value) {
		return CACHE.computeIfAbsent(value, IntConstant::new);
	}

	public static final IntConstant
			MINUS_ONE = valueOf(-1),
			ZERO = valueOf(0),
			ONE = valueOf(1),
			TWO = valueOf(2),
			THREE = valueOf(3),
			FOUR = valueOf(4),
			FIVE = valueOf(5);


	@Getter
	private final int value;

	@Override
	public Type getType() {
		return PrimitiveType.INTEGRAL;
	}

	@Override
	public boolean valueEquals(int value) {
		return this.value == value;
	}

	@Override
	public FieldDescriptor getConstant(Context context) {
		var descriptor = context.getConstant(value);

		if (descriptor == null && (byte)value == value)  descriptor = context.getConstant((byte)value);
		if (descriptor == null && (short)value == value) descriptor = context.getConstant((short)value);
		if (descriptor == null && (char)value == value)  descriptor = context.getConstant((char)value);

		return descriptor;
	}

	@Override
	public void write(DecompilationWriter out, ConstantWriteContext context) {
		var type = context.getType();

		if (type == PrimitiveType.BOOLEAN) {
			out.record(Boolean.toString(value != 0));
			return;
		}

		if (type == PrimitiveType.CHAR) {
			writeConstantOrValue(
					out, context, context.getConstant((char)value),
					() -> out.record('\'').record(JavaEscapeUtils.escapeChar((char)value)).record('\'')
			);
			return;
		}

		if (type != null && !context.isImplicitByteShortCastAllowed()) {
			if (check(type, PrimitiveType.BYTE)) {
				writeConstantOrValue(
						out, context, context.getConstant((byte)value),
						() -> out.record("(byte)").record(Integer.toString(value))
				);
				return;
			}

			if (check(type, PrimitiveType.SHORT)) {
				writeConstantOrValue(
						out, context, context.getConstant((short)value),
						() -> out.record("(short)").record(Integer.toString(value))
				);
				return;
			}
		}

		var constant =
				check(type, PrimitiveType.BYTE) ? context.getConstant((byte)value) :
				check(type, PrimitiveType.SHORT) ? context.getConstant((short)value) : context.getConstant(value);

		writeConstantOrValue(out, context, constant, () -> out.record(Integer.toString(value)));
	}

	private boolean check(@Nullable Type given, Type assumed) {
		return given != null && Type.assignDownQuiet(given, assumed) == given;
	}


	@Override
	public void writeIntAsChar(DecompilationWriter out, ConstantWriteContext context) {
		out.record((char)value == value ?
				JavaEscapeUtils.wrapChar((char)value) :
				Integer.toString(value));
	}

	@Override
	public void writeHex(DecompilationWriter out, ConstantWriteContext context) {
		if (context.getType() == PrimitiveType.BOOLEAN) {
			out.record(String.valueOf(value != 0));
		} else {
			out.record("0x").record(Integer.toHexString(value));
		}
	}

	@Override
	public int compareTo(IntConstant other) {
		return value - other.value;
	}

	@Override
	public String toString() {
		return "IntConstant(" + value + ")";
	}
}
