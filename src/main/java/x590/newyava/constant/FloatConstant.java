package x590.newyava.constant;

import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import x590.newyava.context.ClassContext;
import x590.newyava.context.ConstantWriteContext;
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
	public boolean valueEquals(int value) {
		return this.value == value;
	}

	@Override
	public void addImports(ClassContext context) {}

	@Override
	public void write(DecompilationWriter out, ConstantWriteContext context) {
		float val = value;
		int intVal = (int)val;

		if (context.isImplicitCastAllowed() && intVal == val) {
			out.record(String.valueOf(intVal));
		} else {
			out.record(String.valueOf(val)).record('f');
		}
	}

	@Override
	public String toString() {
		return "FloatConstant(" + value + ")";
	}
}
