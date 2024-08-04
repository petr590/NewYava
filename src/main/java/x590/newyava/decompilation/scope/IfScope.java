package x590.newyava.decompilation.scope;

import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.code.Chunk;
import x590.newyava.decompilation.operation.*;
import x590.newyava.decompilation.operation.condition.*;
import x590.newyava.decompilation.operation.condition.OperatorCondition.Operator;
import x590.newyava.decompilation.operation.emptyscope.EmptyScopeOperation;
import x590.newyava.decompilation.operation.emptyscope.EmptyableScopeOperation;
import x590.newyava.decompilation.operation.invoke.InvokeSpecialOperation;
import x590.newyava.decompilation.operation.other.FieldOperation;
import x590.newyava.decompilation.operation.terminal.ThrowOperation;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;
import x590.newyava.util.Utils;

import java.util.List;
import java.util.Set;

import static x590.newyava.descriptor.MethodDescriptor.constructor;

public final class IfScope extends IfElseScope {

	@Getter
	private final Condition condition;

	/** @return экземпляр {@link IfScope}, если список чанков не пуст, иначе {@link EmptyScopeOperation} */
	public static EmptyableScopeOperation create(Condition condition, @Unmodifiable List<Chunk> chunks) {
		return chunks.isEmpty() ?
				new EmptyScopeOperation("if", condition, PrimitiveType.BOOLEAN) :
				new IfScope(condition, chunks);
	}

	private IfScope(Condition condition, @Unmodifiable List<Chunk> chunks) {
		super(chunks, -1);
		this.condition = condition;
	}

	@Override
	public boolean removeLastContinueOfLoop(LoopScope loop) {
		OperationUtils.removeLastContinueOfLoop(this, loop);
		return false;
	}

	@Override
	public void inferType(Type ignored) {
		super.inferType(ignored);
		condition.inferType(PrimitiveType.BOOLEAN);
	}

	@Override
	public void addImports(ClassContext context) {
		super.addImports(context);
		context.addImportsFor(condition);
	}

	@Override
	protected Operation getHeaderOperation() {
		return condition;
	}

	private static final Set<MethodDescriptor> ASSERTION_ERROR_CONSTRUCTORS = Set.of(
			constructor(ClassType.ASSERTION_ERROR),
			constructor(ClassType.ASSERTION_ERROR, List.of(ClassType.STRING)),
			constructor(ClassType.ASSERTION_ERROR, List.of(ClassType.OBJECT)),
			constructor(ClassType.ASSERTION_ERROR, List.of(PrimitiveType.BOOLEAN)),
			constructor(ClassType.ASSERTION_ERROR, List.of(PrimitiveType.CHAR)),
			constructor(ClassType.ASSERTION_ERROR, List.of(PrimitiveType.INT)),
			constructor(ClassType.ASSERTION_ERROR, List.of(PrimitiveType.LONG)),
			constructor(ClassType.ASSERTION_ERROR, List.of(PrimitiveType.FLOAT)),
			constructor(ClassType.ASSERTION_ERROR, List.of(PrimitiveType.DOUBLE))
	);


	private boolean isAssertionField(Context context, Operation operation) {
		if (operation instanceof FieldOperation fieldOp &&
			fieldOp.isStatic() && fieldOp.isGetter() &&
			fieldOp.getDescriptor().equals(context.getThisType(), "$assertionsDisabled", PrimitiveType.BOOLEAN)) {

			var foundField = context.findIField(fieldOp.getDescriptor());
			return foundField.isPresent() && foundField.get().isSynthetic();
		}

		return false;
	}


	private @Nullable Pair<Operation, @Nullable Operation> getAssertValues(Context context) {
		// Проверяем условие
		if (condition instanceof OperatorCondition operatorCondition &&
			operatorCondition.getOperator() == Operator.AND &&
			operatorCondition.getOperand1() instanceof CompareCondition operand1 &&
			operand1.isNot() &&
			isAssertionField(context, operand1.getOperand1()) &&

			// Проверяем тело if
			operations.size() == 1 &&
			operations.get(0) instanceof ThrowOperation throwOp &&
			throwOp.getException() instanceof InvokeSpecialOperation invokeSpecial &&
			invokeSpecial.isNew() && ASSERTION_ERROR_CONSTRUCTORS.contains(invokeSpecial.getDescriptor())) {

			return Pair.of(
					operatorCondition.getOperand2().opposite(),
					Utils.getFirstOrNull(invokeSpecial.getArguments())
			);
		}

		return null;
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		var assertValues = getAssertValues(context);

		if (assertValues != null) {
			out.record("assert ").record(assertValues.first(), context, Priority.ZERO);

			var second = assertValues.second();

			if (second != null) {
				out.record(" : ").record(second, context, Priority.ZERO);
			}

			out.record(';');

		} else {
			super.write(out, context);
		}
	}

	@Override
	protected boolean writeHeader(DecompilationWriter out, MethodWriteContext context) {
		out.record("if (").record(condition, context, Priority.ZERO).record(')');
		return true;
	}
}
