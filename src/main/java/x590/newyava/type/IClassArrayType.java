package x590.newyava.type;

import org.jetbrains.annotations.Nullable;
import x590.newyava.exception.InvalidTypeException;

/**
 * Тип класса (возможно, параметризованного) или массива
 */
public sealed interface IClassArrayType extends ReferenceType permits IClassType, ArrayType {
	default boolean isArray() {
		return false;
	}

	@Override
	@Nullable String getBinName();

	/** @return бинарное имя класса без префикса {@code "L"} и постфикса {@code ";"}.
	 * Например, {@code "java/lang/Object"}. Для массивов такое же, как и {@link #getBinName()} */
	String getClassBinName();

	/** @return бинарное имя класса с точкой в качестве разделителя. Например, {@code "java.lang.Object"}. */
	default String getCanonicalBinName() {
		return getClassBinName().replace('/', '.');
	}


	/** @return тип без generic-параметров. */
	@Override
	IClassArrayType base();

	static IClassArrayType valueOf(Class<?> clazz) {
		return clazz.isArray() ? ArrayType.valueOf(clazz) : ClassType.valueOf(clazz);
	}

	static IClassArrayType valueOf(String typeName) {
		if (typeName.isEmpty())
			throw new InvalidTypeException("Empty type name");

		return typeName.charAt(0) == '[' ?
				ArrayType.valueOf(typeName) :
				ClassType.valueOf(typeName);
	}
}
