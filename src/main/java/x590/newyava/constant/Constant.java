package x590.newyava.constant;

import x590.newyava.Importable;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ReferenceType;
import x590.newyava.type.Type;

/**
 * Константное значение, такое как boolean, число, строка или класс.
 */
public abstract sealed class Constant implements Importable
		permits IntConstant, LongConstant, FloatConstant, DoubleConstant,
		StringConstant, ClassConstant {

	public static Constant fromObject(Object value) {
		return switch (value) {
			case Integer val -> IntConstant.valueOf(val);
			case Long val -> new LongConstant(val);
			case Float val -> new FloatConstant(val);
			case Double val -> new DoubleConstant(val);
			case String val -> new StringConstant(val);
			case org.objectweb.asm.Type type -> new ClassConstant(ReferenceType.valueOf(type.getInternalName()));
			case null -> throw new NullPointerException();
			default -> throw new IllegalArgumentException("value " + value + " of type " + value.getClass());
		};
	}

	public abstract Type getType();

	public abstract void write(DecompilationWriter out, Context context, Type type);
}
