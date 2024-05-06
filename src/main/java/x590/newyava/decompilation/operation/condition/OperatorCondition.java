package x590.newyava.decompilation.operation.condition;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import x590.newyava.context.Context;
import x590.newyava.decompilation.operation.Associativity;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.io.DecompilationWriter;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class OperatorCondition implements Condition {

	public static OperatorCondition and(Condition operand1, Condition operand2) {
		return new OperatorCondition(Operator.AND, operand1, operand2);
	}

	public static OperatorCondition or(Condition operand1, Condition operand2) {
		return new OperatorCondition(Operator.OR, operand1, operand2);
	}

	private final Operator operator;
	private final Condition operand1, operand2;

	@Override
	public Condition opposite() {
		return new OperatorCondition(operator.opposite(), operand1.opposite(), operand2.opposite());
	}

	@Override
	public Priority getPriority() {
		return operator.priority;
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out .record(operand1, context, getPriority(), Associativity.LEFT)
			.recordSp().recordSp(operator.value)
			.record(operand2, context, getPriority(), Associativity.RIGHT);
	}

	@RequiredArgsConstructor
	private enum Operator {
		AND("&&", Priority.LOGICAL_AND),
		OR("||", Priority.LOGICAL_OR);

		private final String value;
		private final Priority priority;

		public Operator opposite() {
			return this == AND ? OR : AND;
		}
	}

	@Override
	public String toString() {
		return String.format("OperatorCondition %08x(%s, %s, %s)",
				hashCode(), operator, operand1, operand2);
	}
}
