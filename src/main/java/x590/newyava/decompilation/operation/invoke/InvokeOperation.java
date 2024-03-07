package x590.newyava.decompilation.operation.invoke;

import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public abstract class InvokeOperation implements Operation {

	protected final MethodDescriptor descriptor;

	protected final List<Operation> arguments;

	private final @UnmodifiableView List<Operation> argumentsView;

	public InvokeOperation(MethodContext context, MethodDescriptor descriptor) {
		this.descriptor = descriptor;
		this.arguments = new ArrayList<>(descriptor.arguments().size());
		this.argumentsView = Collections.unmodifiableList(arguments);

		var argTypes = descriptor.arguments();

		for (ListIterator<Type> iter = argTypes.listIterator(argTypes.size()); iter.hasPrevious();) {
			arguments.add(context.popAs(iter.previous()));
		}

		Collections.reverse(arguments);
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

	protected void writeNameAndArgs(DecompilationWriter out, ClassContext context) {
		out.record(descriptor.name()).record('(').record(arguments, context, Priority.ZERO, ", ").record(')');
	}
}
