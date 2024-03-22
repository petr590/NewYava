package x590.newyava.decompilation.operation.invokedynamic;

import org.jetbrains.annotations.Nullable;
import x590.newyava.Modifiers;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.WriteContext;
import x590.newyava.decompilation.ReadonlyCode;
import x590.newyava.decompilation.operation.LoadOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.OperationUtil;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.terminal.ReturnValueOperation;
import x590.newyava.decompilation.variable.Variable;
import x590.newyava.descriptor.IncompleteMethodDescriptor;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.exception.DecompilationException;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;
import x590.newyava.type.TypeSize;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LambdaOperation implements Operation {
	/** Формальный дескриптор инструкции invokedynamic */
	private final IncompleteMethodDescriptor invokedynamicDescriptor;

	/** Видимый дескриптор */
	private final MethodDescriptor descriptor;

	private final @Nullable ReadonlyCode code;

	public LambdaOperation(MethodContext context, IncompleteMethodDescriptor indyDescriptor,
	                       MethodDescriptor lambdaDescriptor) {

		this.invokedynamicDescriptor = indyDescriptor;

		var indyArgTypes = indyDescriptor.arguments();
		var lambdaArgTypes = lambdaDescriptor.arguments();

		if (lambdaArgTypes.size() < indyArgTypes.size() ||
			!lambdaArgTypes.subList(0, indyArgTypes.size()).equals(indyArgTypes)) {

			throw new DecompilationException(
					"Lambda args list is not starts with invokedynamic args list: " + lambdaArgTypes + ", " + indyArgTypes
			);
		}

		this.descriptor = indyArgTypes.isEmpty() ? lambdaDescriptor :
				new MethodDescriptor(lambdaDescriptor.hostClass(), lambdaDescriptor.name(), lambdaDescriptor.returnType(),
						lambdaDescriptor.arguments().subList(indyArgTypes.size(), lambdaDescriptor.arguments().size()));

		List<Operation> indyArgs = OperationUtil.readArgs(context, indyArgTypes);


		if (lambdaDescriptor.hostClass().equals(context.getThisType())) {
			var foundMethod = context.findMethod(lambdaDescriptor);

			if (foundMethod.isPresent() && (foundMethod.get().getModifiers() & Modifiers.ACC_SYNTHETIC) != 0) {
				this.code = foundMethod.get().getCode();

				var variables = code.getMethodScope().getVariables();
				int index = 0;

				for (Operation arg : indyArgs) {
					if (arg instanceof LoadOperation load) {
						variables.get(index).setName(load.getVarRef().getName());
					}

					index += arg.getReturnType().getSize() == TypeSize.LONG ? 2 : 1;
				}

				 return;
			}
		}

		this.code = null;
	}

	@Override
	public Type getReturnType() {
		return invokedynamicDescriptor.returnType();
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(code).addImportsFor(descriptor.hostClass());
	}

	@Override
	public void write(DecompilationWriter out, WriteContext context) {
		if (code != null) {
			if (OperationUtil.writeArrayLambda(out, context, descriptor, code)) {
				return;
			}

			var variables = code.getMethodScope().getVariables();

			out.record(variables.stream()
					.skip(invokedynamicDescriptor.slots())
					.limit(descriptor.slots())
					.filter(Objects::nonNull).map(Variable::getName)
					.collect(Collectors.joining(", ", "(", ") -> ")));

			var operations = code.getMethodScope().getOperations();

			if (operations.size() == 1) {
				var operation = operations.get(0);

				if (!operation.isScopeLike()) {
					out.record(operation instanceof ReturnValueOperation ret ? ret.getValue() : operation,
							context, Priority.ZERO);

					return;
				}
			}

			out.record(code, context);

		} else {
			out.record(descriptor.hostClass(), context).record("::")
					.record(descriptor.isConstructor() ? "new" : descriptor.name());
		}
	}
}
