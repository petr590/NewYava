package x590.newyava.decompilation.operation.invokedynamic;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.decompilation.operation.variable.ILoadOperation;
import x590.newyava.modifiers.Modifiers;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.ReadonlyCode;
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

import java.util.List;
import java.util.Objects;

public class LambdaOperation implements Operation {
	/** Дескриптор самой инструкции invokedynamic */
	private final IncompleteMethodDescriptor indyDescriptor;

	/** Дескриптор метода реализации лямбды */
	private MethodDescriptor implDescriptor;

	private final List<Operation> indyArgs;

	private @Nullable ReadonlyCode code;

	public LambdaOperation(MethodContext context, IncompleteMethodDescriptor indyDescriptor,
	                       MethodDescriptor implDescriptor) {

		this.indyDescriptor = indyDescriptor;
		this.implDescriptor = implDescriptor;
		this.indyArgs = OperationUtil.readArgs(context, indyDescriptor.arguments());
		this.code = findCode(context, implDescriptor);
	}

	private static @Nullable ReadonlyCode findCode(MethodContext context, MethodDescriptor implDescriptor) {
		if (implDescriptor.hostClass().equals(context.getThisType())) {
			var foundMethod = context.findMethod(implDescriptor);

			if (foundMethod.isPresent() && (foundMethod.get().getModifiers() & Modifiers.ACC_SYNTHETIC) != 0) {
				return foundMethod.get().getCode();
			}
		}

		return null;
	}


	@Override
	public Type getReturnType() {
		return indyDescriptor.returnType();
	}

	@Override
	public void inferType(Type ignored) {
		OperationUtil.inferArgTypes(indyArgs, indyDescriptor.arguments());
	}

	@Override
	public void beforeVariablesInit(MethodScope methodScope) {
		Operation.super.beforeVariablesInit(methodScope);

		if (code != null) {
			code.getMethodScope().setOuterScope(methodScope);

			var varTable = code.getVarTable();
			int slot = 0;

			for (Operation arg : indyArgs) {
				if (arg instanceof ILoadOperation load) {
					var ref = varTable.get(slot).get(0);

					if (ref != null) {
						ref.bind(load.getVarRef());
					}
				}

				slot += arg.getReturnType().getSize().slots();
			}
		}
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return indyArgs;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(code).addImportsFor(implDescriptor.hostClass());
	}


	private boolean lambdaSimplified;

	private void simplifyLambda() {
		if (lambdaSimplified) return;
		lambdaSimplified = true;

		if (code != null) {
			var arrayDescriptor = OperationUtil.recognizeArrayLambda(implDescriptor, code);

			if (arrayDescriptor != null) {
				implDescriptor = arrayDescriptor;
				code = null;

			} else {
				var descriptorAndObject = OperationUtil.recognizeFunctionLambda(implDescriptor, code, indyArgs);

				if (descriptorAndObject != null) {
					var descriptor = descriptorAndObject.first();
					var object = descriptorAndObject.second();

					implDescriptor = descriptor;
					code = null;

					indyArgs.clear();
					indyArgs.add(object);
				}
			}
		}
	}


	@Override
	public Priority getPriority() {
		simplifyLambda();
		return code == null ? Priority.DEFAULT : Priority.LAMBDA;
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		simplifyLambda();

		if (code != null) {
			var variables = code.getMethodScope().getVariables();

			List<String> names = variables.stream()
					.skip(indyDescriptor.slots())
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
				out.record(implDescriptor.hostClass(), context);
			}

			out.record("::").record(implDescriptor.isConstructor() ? "new" : implDescriptor.name());
		}
	}

	@Override
	public String toString() {
		return String.format("LambdaOperation %08x(indyDescriptor: %s, implDescriptor: %s, indyArgs: %s, code: %s)",
				hashCode(), indyDescriptor, implDescriptor, indyArgs, code);
	}
}
