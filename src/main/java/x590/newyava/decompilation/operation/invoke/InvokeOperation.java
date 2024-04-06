package x590.newyava.decompilation.operation.invoke;

import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.OperationUtil;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

import java.util.Collections;
import java.util.List;

public abstract class InvokeOperation implements Operation {

	protected final MethodDescriptor descriptor;

	protected final List<Operation> arguments;

	private final @UnmodifiableView List<Operation> argumentsView;

	public InvokeOperation(MethodContext context, MethodDescriptor descriptor) {
		this.descriptor = descriptor;
		this.arguments = OperationUtil.readArgs(context, descriptor.arguments());
		this.argumentsView = Collections.unmodifiableList(arguments);
	}

	public @UnmodifiableView List<Operation> getArguments() {
		return argumentsView;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(arguments);
	}

	@Override
	public Type getReturnType() {
		return descriptor.returnType();
	}

	protected void writeNameAndArgs(DecompilationWriter out, Context context) {
		out.record(descriptor.name()).record('(').record(arguments, context, Priority.ZERO, ", ").record(')');
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return arguments;
	}
}
