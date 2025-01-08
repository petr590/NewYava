package x590.newyava.constant;

import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
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
			float val = value;

			if (Float.isNaN(val)) {
				out.record("0.0f / 0.0f");
				return;
			}

			if (val == Float.POSITIVE_INFINITY) {
				out.record("1.0f / 0.0f");
				return;
			}

			if (val == Float.NEGATIVE_INFINITY) {
				out.record("-1.0f / 0.0f");
				return;
			}

			int intVal = (int)val;

			if (context.isImplicitCastAllowed() && intVal == val) {
				out.record(String.valueOf(intVal));
			} else {
				out.record(String.valueOf(val)).record('f');
			}
		});
	}

	@Override
	public String toString() {
		return "FloatConstant(" + value + ")";
	}
}
