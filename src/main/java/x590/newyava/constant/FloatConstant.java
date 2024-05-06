package x590.newyava.constant;

import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FloatConstant extends Constant {

	private static final Float2ObjectMap<FloatConstant> CACHE = new Float2ObjectOpenHashMap<>();

	public static FloatConstant valueOf(float value) {
		return CACHE.computeIfAbsent(value, FloatConstant::new);
	}

	public static final FloatConstant
			ZERO = valueOf(0),
			ONE = valueOf(1),
			TWO = valueOf(2);

	private final float value;

	@Override
	public Type getType() {
		return PrimitiveType.FLOAT;
	}

	@Override
	public void addImports(ClassContext context) {}

	@Override
	public void write(DecompilationWriter out, Context context, @Nullable Type type) {
		out.record(String.valueOf(value)).record('f');
	}

	@Override
	public String toString() {
		return "FloatConstant(" + value + ")";
	}
}
