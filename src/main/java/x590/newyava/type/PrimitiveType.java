package x590.newyava.type;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;

import static x590.newyava.type.IntMultiType.*;

public sealed interface PrimitiveType extends Type
	permits PrimitiveType.NonIntType, IntMultiType {

	static PrimitiveType valueOf(Class<?> clazz) {
		if (!clazz.isPrimitive()) {
			throw new IllegalArgumentException("Class " + clazz + " not represents primitive type");
		}

		// Отсортировано примерно по частоте использования этих типов в большом проекте
		// Just a little optimization
		if (clazz == int.class)     return PrimitiveType.INT;
		if (clazz == long.class)    return PrimitiveType.LONG;
		if (clazz == float.class)   return PrimitiveType.FLOAT;
		if (clazz == double.class)  return PrimitiveType.DOUBLE;
		if (clazz == boolean.class) return PrimitiveType.BOOLEAN;
		if (clazz == void.class)    return PrimitiveType.VOID;
		if (clazz == byte.class)    return PrimitiveType.BYTE;
		if (clazz == short.class)   return PrimitiveType.SHORT;
		if (clazz == char.class)    return PrimitiveType.CHAR;
		throw new IllegalArgumentException("Class " + clazz + " is primitive but does not matches any primitive class");
	}

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

			/** Включает в себя boolean, byte, short, char, int */
			INTEGRAL = IntMultiType.valueOf(ALL_FLAGS),
			BYTE_OR_BOOLEAN = IntMultiType.valueOf(BYTE_FLAG | BOOLEAN_FLAG);

	NonIntType
			LONG = NonIntType.LONG,
			FLOAT = NonIntType.FLOAT,
			DOUBLE = NonIntType.DOUBLE,
			VOID = NonIntType.VOID;

	@RequiredArgsConstructor
	enum NonIntType implements PrimitiveType {
		LONG("long", TypeSize.LONG),
		FLOAT("float", TypeSize.WORD),
		DOUBLE("double", TypeSize.LONG),
		VOID("void", TypeSize.VOID);

		private final String name;
		private final TypeSize size;

		@Override
		public TypeSize getSize() {
			return size;
		}

		@Override
		public void write(DecompilationWriter out, Context context) {
			out.record(name);
		}

		@Override
		public String getVarName() {
			return name.substring(0, 1);
		}


		@Override
		public String toString() {
			return name;
		}
	}
}
