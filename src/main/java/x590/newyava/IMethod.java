package x590.newyava;

import org.objectweb.asm.Opcodes;
import x590.newyava.descriptor.MethodDescriptor;

public interface IMethod {
	default boolean isVarargs() {
		return (getModifiers() & Opcodes.ACC_VARARGS) != 0;
	}

	int getModifiers();

	MethodDescriptor getDescriptor();

	MethodDescriptor getVisibleDescriptor();
}
