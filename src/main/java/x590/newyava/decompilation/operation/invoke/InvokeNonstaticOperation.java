package x590.newyava.decompilation.operation.invoke;

import lombok.Getter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.OperationUtils;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class InvokeNonstaticOperation extends InvokeOperation {
	@Getter
	protected Operation object;

	public InvokeNonstaticOperation(MethodContext context, MethodDescriptor descriptor) {
		super(context, descriptor);
		this.object = context.popAs(descriptor.hostClass());
	}

	@Override
	@MustBeInvokedByOverriders
	public void inferType(Type ignored) {
		super.inferType(ignored);
		object.inferType(descriptor.hostClass());
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return OperationUtils.addBefore(object, super.getNestedOperations());
	}

	@Override
	public void addImports(ClassContext context) {
		super.addImports(context);
		context.addImportsFor(object);
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		if (!canOmitThis(context)) {
			out.record(object, context, getPriority()).record('.');
		}

		writeNameAndArgs(out, context);
	}

	private boolean canOmitThis(MethodWriteContext context) {
		return context.getConfig().canOmitThisAndClass() &&
				object.isThisRef();
	}

	@Override
	public String toString() {
		return String.format("%s %08x(%s %s.%s(%s))", getClass().getSimpleName(),
				hashCode(), descriptor.returnType(), object, descriptor.name(),
				arguments.stream().map(Objects::toString).collect(Collectors.joining(" ")));
	}
}
