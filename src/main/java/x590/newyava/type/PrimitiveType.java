package x590.newyava.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;

import static x590.newyava.type.IntMultiType.*;

/**
 * Примитивные типы.
 * Все экземпляры этого класса кешируются, поэтому можно сравнивать типы напрямую.
 */
public sealed interface PrimitiveType extends Type
	permits PrimitiveType.NonIntType, IntMultiType {

	static PrimitiveType valueOf(Class<?> clazz) {
		if (!clazz.isPrimitive()) {
			throw new IllegalArgumentException("Class " + clazz + " not represents primitive type");
		}

		// Отсортировано примерно по частоте использования этих типов в большом проекте
		// Just a little optimization
		if (clazz == int.class)     return INT;
		if (clazz == long.class)    return LONG;
		if (clazz == float.class)   return FLOAT;
		if (clazz == double.class)  return DOUBLE;
		if (clazz == boolean.class) return BOOLEAN;
		if (clazz == void.class)    return VOID;
		if (clazz == byte.class)    return BYTE;
		if (clazz == short.class)   return SHORT;
		if (clazz == char.class)    return CHAR;
		throw new IllegalArgumentException("Class " + clazz + " is primitive but does not matches any primitive class");
	}

	@Deprecated(since = "0.8.15")
	static boolean isAssignable(PrimitiveType givenType, PrimitiveType requiredType) {
		if (givenType == requiredType)
			return true;

		if (givenType instanceof IntMultiType type1 &&
			requiredType instanceof IntMultiType type2) {

			return IntMultiType.isAssignable(type1, type2);
		}

		return false;
	}

	static @Nullable PrimitiveType assignQuiet(PrimitiveType givenType, PrimitiveType requiredType) {
		if (givenType == requiredType)
			return givenType;

		if (givenType instanceof IntMultiType type1 &&
			requiredType instanceof IntMultiType type2) {

			return IntMultiType.assignQuiet(type1, type2);
		}

		return null;
	}

	IntMultiType
			BOOLEAN = IntMultiType.valueOf(BOOLEAN_FLAG),
			BYTE = IntMultiType.valueOf(BYTE_FLAG),
			SHORT = IntMultiType.valueOf(SHORT_FLAG),
			CHAR = IntMultiType.valueOf(CHAR_FLAG),
			INT = IntMultiType.valueOf(INT_FLAG),

			/** Включает в себя byte, short, char, int */
			NUMERIC = IntMultiType.valueOf(NUMERIC_FLAGS),

			/** Включает в себя boolean, byte, short, char, int */
			INTEGRAL = IntMultiType.valueOf(ALL_FLAGS),

			BYTE_OR_BOOLEAN = IntMultiType.valueOf(BYTE_FLAG | BOOLEAN_FLAG),
			INT_OR_BOOLEAN = IntMultiType.valueOf(INT_FLAG | BOOLEAN_FLAG);

	NonIntType
			LONG = NonIntType.LONG,
			FLOAT = NonIntType.FLOAT,
			DOUBLE = NonIntType.DOUBLE,
			VOID = NonIntType.VOID;

	@RequiredArgsConstructor
	enum NonIntType implements PrimitiveType {
		LONG   ("long",   "J", TypeSize.LONG),
		FLOAT  ("float",  "F", TypeSize.WORD),
		DOUBLE ("double", "D", TypeSize.LONG),
		VOID   ("void",   "V", TypeSize.VOID);

		private final String name;

		@Getter
		private final String binName;

		@Getter
		private final TypeSize size;

		@Override
		public void write(DecompilationWriter out, Context context) {
			out.record(name);
		}

		@Override
		public String getVarName() {
			return name.substring(0, 1);
		}

		@Override
		public String getArrVarName() {
			return name;
		}


		@Override
		public String toString() {
			return name;
		}
	}
}
