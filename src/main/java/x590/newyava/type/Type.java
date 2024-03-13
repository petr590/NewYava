package x590.newyava.type;

import org.jetbrains.annotations.Nullable;
import x590.newyava.ContextualWritable;
import x590.newyava.Importable;
import x590.newyava.context.ClassContext;
import x590.newyava.exception.InvalidTypeException;
import x590.newyava.exception.TypeCastException;
import x590.newyava.io.SignatureReader;

import java.util.ArrayList;
import java.util.List;

public interface Type extends ContextualWritable, Importable {

	default TypeSize getSize() {
		return TypeSize.WORD;
	}

	@Override
	default void addImports(ClassContext context) {}

	default String getVarName() {
		return "var";
	}

	static boolean isAssignable(Type givenType, Type requiredType) {
		if (givenType.equals(requiredType) || givenType == Types.ANY_TYPE || requiredType == Types.ANY_TYPE) {
			return true;
		}

		if (givenType instanceof PrimitiveType type1 &&
			requiredType instanceof PrimitiveType type2) {

			return PrimitiveType.isAssignable(type1, type2);
		}

		if (givenType instanceof ReferenceType type1 &&
			requiredType instanceof ReferenceType type2) {

			return ReferenceType.isAssignable(type1, type2);
		}

		return false;
	}

	@Deprecated
	static void checkAssignable(Type givenType, Type requiredType) {
		if (!isAssignable(givenType, requiredType)) {
			throw new TypeCastException("Cannot cast " + givenType + " to " + requiredType);
		}
	}


	static Type assign(Type givenType, Type requiredType) {
		var result = assignQuiet(givenType, requiredType);

		if (result != null) return result;

		throw new TypeCastException("Cannot cast " + givenType + " to " + requiredType);
	}
	
	static @Nullable Type assignQuiet(Type givenType, Type requiredType) {
		if (givenType.equals(requiredType)) return givenType;

		if (requiredType == Types.ANY_TYPE) return givenType;
		if (givenType == Types.ANY_TYPE) return requiredType;

		if (givenType instanceof PrimitiveType type1 &&
			requiredType instanceof PrimitiveType type2) {

			return PrimitiveType.assignQuiet(type1, type2);
		}

		if (givenType instanceof ReferenceType type1 &&
			requiredType instanceof ReferenceType type2) {

			return ReferenceType.assignQuiet(type1, type2);
		}

		return null;
	}

	static List<Type> parseMethodArguments(SignatureReader reader) {
		if (reader.next() != '(') {
			throw new InvalidTypeException(reader.dec());
		}

		List<Type> result = new ArrayList<>();

		while (!reader.eat(')')) {
			result.add(parse(reader));
		}

		return result;
	}

	static Type parseReturnType(SignatureReader reader) {
		return reader.next() == 'V' ? PrimitiveType.VOID : parse(reader.dec());
	}

	static Type parse(SignatureReader reader) {
		return switch (reader.next()) {
			case 'B' -> PrimitiveType.BYTE;
			case 'S' -> PrimitiveType.SHORT;
			case 'C' -> PrimitiveType.CHAR;
			case 'I' -> PrimitiveType.INT;
			case 'J' -> PrimitiveType.LONG;
			case 'F' -> PrimitiveType.FLOAT;
			case 'D' -> PrimitiveType.DOUBLE;
			case 'Z' -> PrimitiveType.BOOLEAN;
			case 'V' -> throw new InvalidTypeException("Void is not allowed here");
			case 'L' -> ClassType.parse(reader);
			case '[' -> ArrayType.parse(reader.dec());
			default -> throw new InvalidTypeException(reader.dec());
		};
	}

	static Type valueOf(String typeName) {
		return switch (typeName) {
			case "B" -> PrimitiveType.BYTE;
			case "S" -> PrimitiveType.SHORT;
			case "C" -> PrimitiveType.CHAR;
			case "I" -> PrimitiveType.INT;
			case "J" -> PrimitiveType.LONG;
			case "F" -> PrimitiveType.FLOAT;
			case "D" -> PrimitiveType.DOUBLE;
			case "Z" -> PrimitiveType.BOOLEAN;
			case "V" -> throw new InvalidTypeException("Void is not allowed here");

			default -> switch (typeName.charAt(0)) {
				case 'L' -> ClassType.valueOf(typeName.substring(1));
				case '[' -> ArrayType.valueOf(typeName);
				default -> throw new InvalidTypeException(typeName);
			};

			case "" -> throw new InvalidTypeException(typeName);
		};
	}

	static Type valueOf(Class<?> clazz) {
		if (clazz.isPrimitive())
			return PrimitiveType.valueOf(clazz);

		if (clazz.isArray())
			return ArrayType.forType(valueOf(clazz.componentType()));

		return ClassType.valueOf(clazz);
	}
}
