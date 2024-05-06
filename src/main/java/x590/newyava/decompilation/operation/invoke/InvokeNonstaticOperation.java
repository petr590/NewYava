package x590.newyava.decompilation.operation.invoke;

import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class InvokeNonstaticOperation extends InvokeOperation {
	protected final Operation object;

	public InvokeNonstaticOperation(MethodContext context, MethodDescriptor descriptor) {
		super(context, descriptor);
		this.object = context.popAs(descriptor.hostClass());
	}

	@Override
	public void inferType(Type ignored) {
		super.inferType(ignored);
		object.inferType(descriptor.hostClass());
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		var operations = new ArrayList<>(arguments);
		operations.add(object);
		return operations;
	}

	@Override
	public void addImports(ClassContext context) {
		super.addImports(context);
		context.addImportsFor(object);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.record(object, context, getPriority()).record('.');
		writeNameAndArgs(out, context);
	}

	@Override
	public String toString() {
		return String.format("%s %08x(%s %s.%s(%s))", getClass().getSimpleName(),
				hashCode(), descriptor.returnType(), object, descriptor.name(),
				arguments.stream().map(Objects::toString).collect(Collectors.joining(" ")));
	}
}
