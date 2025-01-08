package x590.newyava.decompilation.operation.invokedynamic;

import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.code.Code;
import x590.newyava.decompilation.code.InvalidCode;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.OperationUtils;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.terminal.ReturnValueOperation;
import x590.newyava.decompilation.operation.variable.ILoadOperation;
import x590.newyava.decompilation.scope.MethodScope;
import x590.newyava.decompilation.variable.Variable;
import x590.newyava.descriptor.IncompleteMethodDescriptor;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;
import x590.newyava.util.Utils;

import java.util.List;
import java.util.Objects;

@EqualsAndHashCode
public class LambdaOperation implements Operation {
	/** Дескриптор самой инструкции invokedynamic */
	private final IncompleteMethodDescriptor indyDescriptor;

	/** Дескриптор метода реализации лямбды */
	private MethodDescriptor implDescriptor;

	/** Аргументы, которые передаются в лямбду. Их количество
	 * должно совпадать с количеством аргументов в {@link #indyDescriptor} */
	private final List<Operation> indyArgs;

	private Code code;

	public LambdaOperation(MethodContext context, IncompleteMethodDescriptor indyDescriptor,
	                       MethodDescriptor implDescriptor) {

		this.indyDescriptor = indyDescriptor;
		this.implDescriptor = implDescriptor;
		this.indyArgs = OperationUtils.readArgs(context, indyDescriptor.arguments());
		this.code = findCode(context, implDescriptor);
	}

	private static Code findCode(MethodContext context, MethodDescriptor implDescriptor) {
		if (implDescriptor.hostClass().equals(context.getThisType())) {
			var foundMethod = context.findMethod(implDescriptor);

			if (foundMethod.isPresent() && foundMethod.get().isSynthetic()) {
				return foundMethod.get().getCode();
			}
		}

		return InvalidCode.EMPTY;
	}

	@Override
	public boolean canUnite(MethodContext context, Operation prev) {
		return !code.isValid() && Utils.isFirst(indyArgs, arg -> OperationUtils.isNullCheck(prev, arg))
				|| Operation.super.canUnite(context, prev);
	}

	@Override
	public Type getReturnType() {
		return indyDescriptor.returnType();
	}

	@Override
	public void inferType(Type ignored) {
		OperationUtils.inferArgTypes(indyArgs, indyDescriptor.arguments());
	}


	/** Связывает внутренние переменные лямбды с переменными внешнего метода. */
	@Override
	public void beforeVariablesInit(Context context, @Nullable MethodScope methodScope) {
		Operation.super.beforeVariablesInit(context, methodScope);

		if (methodScope != null && code.isValid()) {
			code.getMethodScope().setOuterScope(methodScope);

			var varTable = code.getVarTable();

			for (int i = 0, slot = 0, size = indyArgs.size(); i < size; i++) {
				if (indyArgs.get(i) instanceof ILoadOperation load) {
					var ref = varTable.get(slot).get(0);

					if (ref != null) {
						ref.bind(load.getVarRef());
					}
				}

				slot += indyDescriptor.arguments().get(i).getSize().slots();
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

		if (!code.isValid()) return;

		var arrayDescriptor = OperationUtils.recognizeArrayLambda(implDescriptor, code);
		if (arrayDescriptor != null) {
			implDescriptor = arrayDescriptor;
			code = InvalidCode.EMPTY;
			return;

		}

		var descriptorAndObject = OperationUtils.recognizeFunctionLambda(implDescriptor, code, indyArgs);
		if (descriptorAndObject != null) {
			var descriptor = descriptorAndObject.first();
			var object = descriptorAndObject.second();

			implDescriptor = descriptor;
			code = InvalidCode.EMPTY;

			indyArgs.clear();
			indyArgs.add(object);
		}
	}


	@Override
	public boolean needEmptyLinesAround() {
		return code.isValid();
	}

	@Override
	public Priority getPriority() {
		simplifyLambda();
		return code.isValid() ? Priority.LAMBDA : Priority.DEFAULT;
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		simplifyLambda();

		if (code.isValid()) {
			// this не передаётся явно в метод лямбды
			int offset = !indyArgs.isEmpty() && indyArgs.get(0).isThisRef() ? 1 : 0;
			int indySlots = indyDescriptor.slots();

			var variables = code.getMethodScope().getVariables();

			List<String> names = variables.stream()
					.skip(indySlots)
					.limit(implDescriptor.slots() - indySlots + offset)
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

				if (!operation.needWrapWithBrackets()) {
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

			if (code.caughtException()) {
				out.space().record(code, context);
			}
		}
	}

	@Override
	public String toString() {
		return String.format(
				"LambdaOperation(indyDescriptor: %s, implDescriptor: %s, indyArgs: %s, code: %s)",
				indyDescriptor, implDescriptor, indyArgs, code
		);
	}
}
