package x590.newyava.decompilation.operation.invokedynamic;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.Modifiers;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.ReadonlyCode;
import x590.newyava.decompilation.operation.LoadOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.OperationUtil;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.terminal.ReturnValueOperation;
import x590.newyava.decompilation.scope.MethodScope;
import x590.newyava.decompilation.variable.Variable;
import x590.newyava.descriptor.IncompleteMethodDescriptor;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;
import x590.newyava.type.TypeSize;

import java.util.List;
import java.util.Objects;

public class LambdaOperation implements Operation {
	/** Дескриптор самой инструкции invokedynamic */
	private final IncompleteMethodDescriptor invokedynamicDescriptor;

	/** Дескриптор метода реализации лямбды */
	private final MethodDescriptor implementationDescriptor;

	private final List<Operation> indyArgs;

	private final @Nullable ReadonlyCode code;

	public LambdaOperation(MethodContext context, IncompleteMethodDescriptor indyDescriptor,
	                       MethodDescriptor implDescriptor) {

		this.invokedynamicDescriptor = indyDescriptor;
		this.implementationDescriptor = implDescriptor;


		this.indyArgs = OperationUtil.readArgs(context, indyDescriptor.arguments());

		if (implDescriptor.hostClass().equals(context.getThisType())) {
			var foundMethod = context.findMethod(implDescriptor);

			if (foundMethod.isPresent() && (foundMethod.get().getModifiers() & Modifiers.ACC_SYNTHETIC) != 0) {
				this.code = foundMethod.get().getCode();
				return;
			}
		}

		this.code = null;
	}

	@Override
	public void beforeVariablesInit(MethodScope methodScope) {
		Operation.super.beforeVariablesInit(methodScope);

		if (code != null) {
			code.getMethodScope().setOuterScope(methodScope);

			var varTable = code.getVarTable();
			int slot = 0;

			for (Operation arg : indyArgs) {
				if (arg instanceof LoadOperation load) {
					var ref = varTable.get(slot).get(0);

					if (ref != null) {
						ref.bind(load.getVarRef());
					}
				}

				slot += arg.getReturnType().getSize() == TypeSize.LONG ? 2 : 1;
			}
		}
	}

	@Override
	public Type getReturnType() {
		return invokedynamicDescriptor.returnType();
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(code).addImportsFor(implementationDescriptor.hostClass());
	}

	@Override
	public Priority getPriority() {
		return Priority.LAMBDA;
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		if (code != null) {
			if (OperationUtil.writeArrayLambda(out, context, implementationDescriptor, code)) {
				return;
			}

			var variables = code.getMethodScope().getVariables();

			List<String> names = variables.stream()
					.skip(invokedynamicDescriptor.slots())
					.filter(Objects::nonNull).map(Variable::getName)
					.toList();

			if (names.size() == 1) {
				out.record(names, ", ");
			} else {
				out.record('(').record(names, ", ").record(')');
			}

			out.record(" -> ");


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
			if (!indyArgs.isEmpty()) {
				out.record(indyArgs.get(0), context, Priority.ZERO);
			} else {
				out.record(implementationDescriptor.hostClass(), context);
			}

			out.record("::").record(implementationDescriptor.isConstructor() ? "new" : implementationDescriptor.name());
		}
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return indyArgs;
	}
}
