package x590.newyava.decompilation.operation.invoke;

import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.WriteContext;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.io.DecompilationWriter;

public class InvokeStaticOperation extends InvokeOperation {

	public InvokeStaticOperation(MethodContext context, MethodDescriptor descriptor) {
		super(context, descriptor);
	}

	@Override
	public void addImports(ClassContext context) {
		super.addImports(context);
		context.addImportsFor(descriptor.hostClass());
	}

	@Override
	public void write(DecompilationWriter out, WriteContext context) {
		out.record(descriptor.hostClass(), context).record('.');
		writeNameAndArgs(out, context);
	}
}
