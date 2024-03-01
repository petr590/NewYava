package x590.newyava.decompilation.operation.invoke;

import x590.newyava.ContextualWritable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

import java.util.ArrayList;
import java.util.List;

public abstract class InvokeOperation implements Operation {

	protected final MethodDescriptor descriptor;

	protected final List<Operation> arguments;

	public InvokeOperation(MethodContext context, MethodDescriptor descriptor) {
		this.descriptor = descriptor;
		this.arguments = new ArrayList<>(descriptor.arguments().size());

		for (Type type : descriptor.arguments()) {
			arguments.add(context.popAs(type));
		}
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(arguments);
	}

	@Override
	public Type getReturnType() {
		return descriptor.returnType();
	}

	protected void writeNameAndArgs(DecompilationWriter out, ClassContext context) {
		out.record(descriptor.name()).record('(').record(arguments, context, Priority.ZERO, ", ").record(')');
	}
}
