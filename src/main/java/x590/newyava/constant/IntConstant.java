package x590.newyava.constant;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import x590.newyava.context.ClassContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class IntConstant extends Constant {
	private static final Int2ObjectMap<IntConstant> CACHE = new Int2ObjectOpenHashMap<>();

	public static IntConstant valueOf(int value) {
		return CACHE.computeIfAbsent(value, IntConstant::new);
	}

	public static final IntConstant
			MINUS_ONE = IntConstant.valueOf(-1),
			ZERO = IntConstant.valueOf(0),
			ONE = IntConstant.valueOf(1),
			TWO = IntConstant.valueOf(2),
			THREE = IntConstant.valueOf(3),
			FOUR = IntConstant.valueOf(4),
			FIVE = IntConstant.valueOf(5);


	@Getter
	private final int value;

	@Override
	public Type getType() {
		return PrimitiveType.INTEGRAL;
	}

	@Override
	public void addImports(ClassContext context) {}

	@Override
	public void write(DecompilationWriter out, ClassContext context, Type type) {
		out.record(
				type == PrimitiveType.BOOLEAN ? String.valueOf(value != 0) :
				type == PrimitiveType.CHAR ? "'" + JavaEscapeUtils.escapeChar((char)value) + "'" :
						String.valueOf(value)
		);
	}
}
