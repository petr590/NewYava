package x590.newyava;

import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.type.IClassArrayType;
import x590.newyava.type.IClassType;

import java.util.List;
import java.util.Optional;

public interface IClass {
	int getModifiers();

	IClassArrayType getThisType();

	IClassType getSuperType();

	@Unmodifiable List<IClassType> getInterfaces();

	Optional<? extends IField> findField(FieldDescriptor descriptor);

	Optional<? extends IMethod> findMethod(MethodDescriptor descriptor);
}
