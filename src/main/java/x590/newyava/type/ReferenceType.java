package x590.newyava.type;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.exception.InvalidTypeException;

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
		return ClassArrayType.valueOf(typeName);
	}
}
