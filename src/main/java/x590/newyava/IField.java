package x590.newyava;

import x590.newyava.descriptor.FieldDescriptor;

import static x590.newyava.modifiers.Modifiers.*;

public interface IField {
	int getModifiers();

	FieldDescriptor getDescriptor();

	FieldDescriptor getVisibleDescriptor();

	default boolean isEnum() {
		return (getModifiers() & ACC_ENUM) != 0;
	}

	default boolean isStatic() {
		return (getModifiers() & ACC_STATIC) != 0;
	}

	default boolean isSynthetic() {
		return (getModifiers() & ACC_SYNTHETIC) != 0;
	}
}
