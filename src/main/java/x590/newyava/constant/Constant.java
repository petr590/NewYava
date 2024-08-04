package x590.newyava.constant;

import x590.newyava.annotation.AnnotationValue;
import x590.newyava.context.ConstantWriteContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassArrayType;
import x590.newyava.type.Type;

import static org.objectweb.asm.Type.*;

/**
 * Константное значение, такое как boolean, число, строка или класс.
 */
public abstract sealed class Constant implements AnnotationValue
		permits IntConstant, LongConstant, FloatConstant, DoubleConstant, StringConstant, ClassConstant {

	public static Constant fromObject(Object value) {
		return switch (value) {
			case Boolean val -> val ? IntConstant.ONE : IntConstant.ZERO;
			case Byte val -> IntConstant.valueOf(val);
			case Short val -> IntConstant.valueOf(val);
			case Character val -> IntConstant.valueOf(val);
			case Integer val -> IntConstant.valueOf(val);
			case Long val -> LongConstant.valueOf(val);
			case Float val -> FloatConstant.valueOf(val);
			case Double val -> DoubleConstant.valueOf(val);
			case String val -> StringConstant.valueOf(val);

			case org.objectweb.asm.Type type ->
					switch (type.getSort()) {
						case VOID    -> ClassConstant.VOID;
						case BOOLEAN -> ClassConstant.BOOLEAN;
						case CHAR    -> ClassConstant.CHAR;
						case BYTE    -> ClassConstant.BYTE;
						case SHORT   -> ClassConstant.SHORT;
						case INT     -> ClassConstant.INT;
						case FLOAT   -> ClassConstant.FLOAT;
						case LONG    -> ClassConstant.LONG;
						case DOUBLE  -> ClassConstant.DOUBLE;
						default -> ClassConstant.valueOf(ClassArrayType.valueOf(type.getInternalName()));
					};

			case null -> throw new NullPointerException();
			default -> throw new IllegalArgumentException("value " + value + " of type " + value.getClass());
		};
	}

	/** Тип константы */
	public abstract Type getType();

	/** Неявный тип константы (например, {@code 1.0f} можно записать как {@code 1},
	 * для неё неявный тип - это {@code int}) */
	public Type getImplicitType() {
		return getType();
	}

	/** @return {@code true}, если значение константы равно переданному значению, иначе {@code false} */
	public boolean valueEquals(int value) {
		return false;
	}

	/** Записывает литерал {@code int} как {@code char}, если возможно. */
	public void writeIntAsChar(DecompilationWriter out, ConstantWriteContext context) {
		write(out, context);
	}

	/** Записывает литералы {@code int} и {@code long} в hex-формате. */
	public void writeHex(DecompilationWriter out, ConstantWriteContext context) {
		write(out, context);
	}
}
