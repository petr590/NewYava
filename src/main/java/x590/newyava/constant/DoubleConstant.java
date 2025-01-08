package x590.newyava.constant;

import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
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
public final class DoubleConstant extends Constant {

	private static final Double2ObjectMap<DoubleConstant> CACHE = new Double2ObjectOpenHashMap<>();

	public static DoubleConstant valueOf(double value) {
		return CACHE.computeIfAbsent(value, DoubleConstant::new);
	}

	public static final DoubleConstant
			ZERO = valueOf(0),
			ONE = valueOf(1);

	private final double value;

	@Override
	public Type getType() {
		return PrimitiveType.DOUBLE;
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
			double val = value;

			if (Double.isNaN(val)) {
				out.record("0.0 / 0.0");
				return;
			}

			if (val == Double.POSITIVE_INFINITY) {
				out.record("1.0 / 0.0");
				return;
			}

			if (val == Double.NEGATIVE_INFINITY) {
				out.record("-1.0 / 0.0");
				return;
			}

			int intVal = (int)val;

			if (context.isImplicitCastAllowed() && intVal == val) {
				out.record(String.valueOf(intVal));
			} else {
				out.record(String.valueOf(val));
			}
		});
	}

	@Override
	public String toString() {
		return "DoubleConstant(" + value + ")";
	}
}
