package x590.newyava.type;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.exception.InvalidTypeException;
import x590.newyava.io.SignatureReader;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public interface ReferenceType extends Type {

	@Nullable ReferenceType getSuperType();

	@Unmodifiable List<? extends ReferenceType> getInterfaces();

	default boolean isNested() {
		return false;
	}

	default boolean isAnonymous() {
		return false;
	}

	default @Nullable ClassType getOuter() {
		return null;
	}

	@Deprecated(since = "0.8.15")
	static boolean isAssignable(ReferenceType givenType, ReferenceType requiredType) {
		if (requiredType.equals(givenType) ||
			requiredType.equals(Types.ANY_OBJECT_TYPE) ||
			requiredType.equals(ClassType.OBJECT) ||
			givenType.equals(Types.ANY_OBJECT_TYPE)) {
			return true;
		}

		if (givenType instanceof ArrayType type1 &&
			requiredType instanceof ArrayType type2) {

			int nest = Math.min(type1.getNestLevel(), type2.getNestLevel());
			return Type.isAssignable(type1.getMemberType(nest), type2.getMemberType(nest));
		}

		var superType = givenType.getSuperType();

		return superType == null || isAssignable(superType, requiredType) ||
				givenType.getInterfaces().stream().anyMatch(interf -> isAssignable(interf, requiredType));
	}

	static @Nullable ReferenceType assignQuiet(ReferenceType givenType, ReferenceType requiredType) {
		if (requiredType.equals(givenType) ||
			requiredType.equals(Types.ANY_OBJECT_TYPE) ||
			requiredType.equals(ClassType.OBJECT)) {
			return givenType;
		}

		if (givenType.equals(Types.ANY_OBJECT_TYPE)) {
			return requiredType;
		}

		if (givenType instanceof ArrayType type1 &&
			requiredType instanceof ArrayType type2) {

			int nest = Math.min(type1.getNestLevel(), type2.getNestLevel());
			Type type = Type.assignQuiet(type1.getMemberType(nest), type2.getMemberType(nest));

			return type == null ? null : ArrayType.forType(type, nest);
		}

		var superType = givenType.getSuperType();
		if (superType == null) return givenType;

		var result = assignQuiet(superType, requiredType);
		if (result != null) return result;

		return givenType.getInterfaces().stream()
				.map(interf -> assignQuiet(interf, requiredType))
				.filter(Objects::nonNull).findAny().orElse(null);
	}

	static ReferenceType valueOf(String typeName) {
		return IClassArrayType.valueOf(typeName);
	}

	static ReferenceType valueOf(java.lang.reflect.Type type) {
		return switch (type) {
			case Class<?> clazz -> IClassArrayType.valueOf(clazz);

			case ParameterizedType parameterized -> ParametrizedClassType.valueOf(
					ClassType.valueOf((Class<?>) parameterized.getRawType()),
					Arrays.stream(parameterized.getActualTypeArguments()).map(ReferenceType::valueOf).toList()
			);

			case TypeVariable<?> typeVar -> GenericType.valueOf(typeVar.getName(), typeVar.getBounds()[0]);

			case java.lang.reflect.WildcardType wildcard -> {
				var upper = wildcard.getUpperBounds();
				var lower = wildcard.getLowerBounds();

				if (upper.length > 1 || lower.length > 1) {
					throw new IllegalArgumentException(String.format(
							"Wildcard type %s has too many bounds: upper = %s, lower = %s",
							wildcard, Arrays.toString(upper), Arrays.toString(lower)
					));
				}

				boolean hasUpper = upper.length != 1 || upper[0] != Object.class;
				boolean hasLower = lower.length != 0;

				if (hasUpper && hasLower) {
					throw new IllegalArgumentException("Wildcard type " + wildcard + " has both upper and lower bounds");
				}

				yield   hasUpper ? WildcardType.extendsFrom(valueOf(upper[0])) :
						hasLower ? WildcardType.superOf(valueOf(lower[0])) :
						Types.ANY_WILDCARD_TYPE;
			}

			case GenericArrayType genericArray -> ArrayType.forType(valueOf(genericArray.getGenericComponentType()));

			default -> throw new IllegalArgumentException(String.format(
					"Unknown type %s (class %s)", type, type.getClass()
			));
		};
	}

	static ReferenceType parse(SignatureReader reader) {
		return switch (reader.next()) {
			case 'L' -> IClassType.parse(reader.dec());
			case 'T' -> GenericType.parse(reader);
			case '[' -> ArrayType.parse(reader.dec());
			case '+' -> WildcardType.extendsFrom(parse(reader));
			case '-' -> WildcardType.superOf(parse(reader));
			case '*' -> Types.ANY_WILDCARD_TYPE;
			default -> throw new InvalidTypeException(reader.dec());
		};
	}
}
