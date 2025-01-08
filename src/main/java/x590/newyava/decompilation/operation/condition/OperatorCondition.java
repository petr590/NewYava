package x590.newyava.decompilation.operation.condition;

import lombok.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Associativity;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.List;

@Getter
@EqualsAndHashCode
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

	@EqualsAndHashCode.Exclude
	private @Nullable OperatorCondition opposite;

	@Override
	public Condition opposite() {
		if (opposite != null)
			return opposite;

		var opposite = new OperatorCondition(operator.opposite(), operand1.opposite(), operand2.opposite());
		opposite.opposite = this;

		return this.opposite = opposite;
	}

	@Override
	public void inferType(Type ignored) {
		operand1.inferType(PrimitiveType.BOOLEAN);
		operand2.inferType(PrimitiveType.BOOLEAN);
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(operand1, operand2);
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
	public enum Operator {
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
		return String.format("OperatorCondition(%s, %s, %s)", operator, operand1, operand2);
	}
}
