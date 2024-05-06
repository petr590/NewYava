package x590.newyava.type;

import org.jetbrains.annotations.Nullable;
import x590.newyava.exception.InvalidTypeException;

import java.util.List;
import java.util.Objects;

public interface ReferenceType extends Type {

	@Nullable ReferenceType getSuperType();

	List<? extends ReferenceType> getInterfaces();

	default boolean isAnonymous() {
		return false;
	}

	static boolean isAssignable(ReferenceType givenType, ReferenceType requiredType) {
		if (requiredType.equals(givenType) ||
			requiredType.equals(Types.ANY_OBJECT_TYPE) ||
			requiredType.equals(ClassType.OBJECT)) {
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

		if (givenType instanceof ArrayType type1 &&
			requiredType instanceof ArrayType type2) {

			int nest = Math.min(type1.getNestLevel(), type2.getNestLevel());
			return ArrayType.forType(
					Type.assignQuiet(type1.getMemberType(nest), type2.getMemberType(nest)),
					nest
			);
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
		if (typeName.isEmpty())
			throw new InvalidTypeException("Empty type");

		return switch (typeName.charAt(0)) {
			case '[' -> ArrayType.valueOf(typeName);
			default -> ClassType.valueOf(typeName);
		};
	}
}
