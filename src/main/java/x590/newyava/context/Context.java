package x590.newyava.context;

import x590.newyava.DecompilingField;
import x590.newyava.DecompilingMethod;
import x590.newyava.Modifiers;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.type.ClassType;
import x590.newyava.type.ReferenceType;

import java.util.Optional;

/**
 * Предоставляет доступ к основным свойствам класса
 */
public interface Context {
	int getClassModifiers();

	default boolean isEnumClass() {
		return (getClassModifiers() & Modifiers.ACC_ENUM) != 0;
	}

	ReferenceType getThisType();

	ClassType getSuperType();

	boolean imported(ClassType classType);

	Optional<DecompilingField> findField(FieldDescriptor descriptor);

	Optional<DecompilingMethod> findMethod(MethodDescriptor descriptor);
}
