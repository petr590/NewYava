package x590.newyava.type;

import org.jetbrains.annotations.Nullable;
import x590.newyava.io.ContextualWritable;
import x590.newyava.Importable;
import x590.newyava.context.ClassContext;
import x590.newyava.exception.InvalidTypeException;
import x590.newyava.exception.TypeCastException;
import x590.newyava.io.SignatureReader;

import java.util.ArrayList;
import java.util.List;

/**
 * Представляет тип данных Java
 */
public interface Type extends ContextualWritable, Importable {

	default TypeSize getSize() {
		return TypeSize.WORD;
	}

	@Override
	default void addImports(ClassContext context) {}

	/** @return имя переменной данного типа */
	default String getVarName() {
		return "var";
	}

	/** @return имя переменной данного типа для массива */
	default String getArrVarName() {
		return getVarName();
	}


	/** @return суперпозицию типов, описывающую все типы, в которые может быть
	 * преобразован данный тип */
	default Type wideUp() {
		return this;
	}

	static boolean isAssignableUp(Type givenType, Type requiredType) {
		return isAssignable(givenType, requiredType.wideUp());
	}

	/** @return тип, соответствующий {@code givenType} и всем супертипам {@code requiredType}.
	 * @throws TypeCastException если такого типа нет. */
	static Type assignUp(Type givenType, Type requiredType) {
		return assign(givenType, requiredType.wideUp());
	}

	/** @return тип, соответствующий {@code givenType} и всем супертипам {@code requiredType}
	 * или {@code null}, если такого типа нет. */
	static @Nullable Type assignUpQuiet(Type givenType, Type requiredType) {
		return assignQuiet(givenType, requiredType.wideUp());
	}


	/** @return суперпозицию типов, описывающую все типы, которые могут быть
	 * преобразованы в данный тип */
	default Type wideDown() {
		return this;
	}

	static boolean isAssignableDown(Type givenType, Type requiredType) {
		return isAssignable(givenType, requiredType.wideDown());
	}

	/** @return тип, соответствующий {@code givenType} и всем подтипам {@code requiredType}.
	 * @throws TypeCastException если такого типа нет. */
	static Type assignDown(Type givenType, Type requiredType) {
		return assign(givenType, requiredType.wideDown());
	}

	/** @return тип, соответствующий {@code givenType} и всем подтипам {@code requiredType}
	 * или {@code null}, если такого типа нет. */
	static @Nullable Type assignDownQuiet(Type givenType, Type requiredType) {
		return assignQuiet(givenType, requiredType.wideDown());
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

	/**
	 * Парсит список аргументов метода, который должен начинаться с {@code '('},
	 * заканчиваться {@code ')'} и содержать полные бинарные имена типов, записанные подряд.
	 * @apiNote {@code reader} также может содержать другие данные, следующие за аргументами.
	 * @return список аргументов.
	 */
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

	/**
	 * Парсит полное бинарное имя типа.
	 * @return тип, соответствующий этому имени.
	 * @apiNote {@code reader} также может содержать другие данные, следующие за именем типа.
	 */
	static Type parseReturnType(SignatureReader reader) {
		return reader.next() == 'V' ? PrimitiveType.VOID : parse(reader.dec());
	}

	/**
	 * Парсит полное бинарное имя типа.
	 * @return тип, соответствующий этому имени.
	 * @apiNote {@code reader} также может содержать другие данные, следующие за именем типа.
	 * @throws IllegalArgumentException если тип является {@link PrimitiveType#VOID}.
	 * Для парсинга этого типа используйте {@link #parseReturnType(SignatureReader)}.
	 */
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

	/**
	 * @param typeName полное бинарное имя типа.
	 * @return тип, соответствующий этому имени.
	 */
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

	/**
	 * @param clazz класс, представляющий тип.
	 * @return тип, соответствующий этому классу:
	 * {@link PrimitiveType}, {@link ClassType} или {@link ArrayType}
	 */
	static Type valueOf(Class<?> clazz) {
		if (clazz.isPrimitive())
			return PrimitiveType.valueOf(clazz);

		if (clazz.isArray())
			return ArrayType.forType(valueOf(clazz.componentType()));

		return ClassType.valueOf(clazz);
	}

	static boolean isArray(Type type) {
		return type instanceof ArrayType;
	}
}
