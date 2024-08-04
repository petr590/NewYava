package x590.newyava.constant;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import x590.newyava.context.ClassContext;
import x590.newyava.context.ConstantWriteContext;
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
	public void addImports(ClassContext context) {}

	@Override
	public void write(DecompilationWriter out, ConstantWriteContext context) {
		var type = context.getType();

		if (type == PrimitiveType.BOOLEAN) {
			out.record(Boolean.toString(value != 0));
			return;
		}

		if (type == PrimitiveType.CHAR) {
			out.record(JavaEscapeUtils.wrapChar((char)value));
			return;
		}

		if (type != null && !context.isImplicitByteShortCastAllowed()) {
			if (check(type, PrimitiveType.BYTE)) {
				out.record("(byte)");
			} else if (check(type, PrimitiveType.SHORT)) {
				out.record("(short)");
			}
		}

		out.record(Integer.toString(value));
	}

	private boolean check(Type given, Type assumed) {
		return Type.assignDownQuiet(given, assumed) == given;
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
