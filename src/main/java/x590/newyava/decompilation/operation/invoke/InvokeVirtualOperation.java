package x590.newyava.decompilation.operation.invoke;

import x590.newyava.context.MethodContext;
import x590.newyava.descriptor.MethodDescriptor;

public class InvokeVirtualOperation extends InvokeNonstaticOperation {

	public InvokeVirtualOperation(MethodContext context, MethodDescriptor descriptor) {
		super(context, descriptor);
	}
}
