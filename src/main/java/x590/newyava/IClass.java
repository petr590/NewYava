package x590.newyava;

import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.type.ClassArrayType;
import x590.newyava.type.ClassType;

import java.util.List;
import java.util.Optional;

public interface IClass {
	int getModifiers();

	ClassArrayType getThisType();

	ClassType getSuperType();

	@Unmodifiable List<ClassType> getInterfaces();

	Optional<? extends IField> findField(FieldDescriptor descriptor);

	Optional<? extends IMethod> findMethod(MethodDescriptor descriptor);
}
