package x590.newyava.decompilation.operation.condition;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Associativity;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

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
	public void inferType(Type ignored) {
		operand1.inferType(PrimitiveType.BOOLEAN);
		operand2.inferType(PrimitiveType.BOOLEAN);
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(operand1).addImportsFor(operand2);
	}

	@Override
	public Priority getPriority() {
		return operator.priority;
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		out .record(operand1, context, getPriority(), Associativity.LEFT)
			.wrapSpaces(operator.value)
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
