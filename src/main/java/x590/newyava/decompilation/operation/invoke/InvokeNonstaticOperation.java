package x590.newyava.decompilation.operation.invoke;

import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.io.DecompilationWriter;

public abstract class InvokeNonstaticOperation extends InvokeOperation {
	protected final Operation object;

	public InvokeNonstaticOperation(MethodContext context, MethodDescriptor descriptor) {
		super(context, descriptor);
		this.object = context.popAs(descriptor.hostClass());
	}

	@Override
	public void addImports(ClassContext context) {
		super.addImports(context);
		context.addImportsFor(object);
	}

	@Override
	public void write(DecompilationWriter out, ClassContext context) {
		out.record(object, context, getPriority()).record('.');
		writeNameAndArgs(out, context);
	}
}
