package x590.newyava.decompilation.operation.invoke;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.OperationUtils;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.array.NewArrayOperation;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;
import x590.newyava.util.Utils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@EqualsAndHashCode
public abstract class InvokeOperation implements Operation {

	@Getter
	protected final MethodDescriptor descriptor;

	protected final List<Operation> arguments;

	@EqualsAndHashCode.Exclude
	private final @UnmodifiableView List<Operation> argumentsView;

	public InvokeOperation(MethodContext context, MethodDescriptor descriptor) {
		this.descriptor = descriptor;
		this.arguments = OperationUtils.readArgs(context, descriptor.arguments());
		this.argumentsView = Collections.unmodifiableList(arguments);
	}

	@Override
	public Type getReturnType() {
		return descriptor.returnType();
	}

	public @UnmodifiableView List<Operation> getArguments() {
		return argumentsView;
	}

	@Override
	@MustBeInvokedByOverriders
	public void inferType(Type ignored) {
		OperationUtils.inferArgTypes(arguments, descriptor.arguments());
		arguments.forEach(Operation::denyByteShortImplicitCast);
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return argumentsView;
	}

	@Override
	public void initPossibleVarNames() {
		Operation.super.initPossibleVarNames();

		if (arguments.size() == 1) {
			arguments.get(0).addPossibleVarName(removePrefix("set"));
		}
	}

	@Override
	public Optional<String> getPossibleVarName() {
		return Optional.ofNullable(removePrefix("get"))
				.or(() -> {
					String name = descriptor.name();
					return Optional.ofNullable(name.equals("length") || name.equals("size") ? name : null);
				});
	}

	private @Nullable String removePrefix(String prefix) {
		String name = descriptor.name();
		int len = prefix.length();

		return name.length() > len && name.startsWith(prefix) && Character.isUpperCase(name.charAt(len)) ?
				Utils.safeToLowerCamelCase(name.substring(len)) :
				null;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(arguments);
	}

	protected void writeNameAndArgs(DecompilationWriter out, MethodWriteContext context) {
		out.record(descriptor.name()).record('(');

		var arguments = this.arguments;
		var foundMethod = context.findIMethod(descriptor);

		if (foundMethod.isPresent() && foundMethod.get().isVarargs() &&
			Utils.getLastOrNull(arguments) instanceof NewArrayOperation newArrayOp) {

			var iterator = Stream.concat(
					arguments.subList(0, arguments.size() - 1).stream(),
					newArrayOp.getInitializers().stream()
			).iterator();

			out.record(iterator, context, Priority.ZERO, ", ", Operation::write);

		} else {
			out.record(arguments, context, Priority.ZERO, ", ");
		}

		out.record(')');
	}
}
