package x590.newyava.decompilation.operation.operator;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.constant.IntConstant;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Associativity;
import x590.newyava.decompilation.operation.other.LdcOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.condition.*;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.List;

public class TernaryOperator implements Operation {

	private final Condition condition;
	private final Operation operand1, operand2;
	private Type returnType;

	/** Содержит упрощённое условие */
	private @Nullable Operation shortCondition;

	public TernaryOperator(Condition condition, Operation operand1, Operation operand2) {
		this.condition = condition;
		this.operand1 = operand1;
		this.operand2 = operand2;

		this.returnType = Type.assign(
				operand1.getReturnType().wideUp(),
				operand2.getReturnType().wideUp()
		);
	}

	@Override
	public Type getReturnType() {
		return returnType;
	}

	@Override
	public void inferType(Type requiredType) {
		condition.inferType(PrimitiveType.BOOLEAN);
		operand1.inferType(requiredType);
		operand2.inferType(requiredType);
		this.returnType = requiredType;

		if (requiredType == PrimitiveType.BOOLEAN) {
			IntConstant const1 = LdcOperation.getIntConstant(operand1),
						const2 = LdcOperation.getIntConstant(operand2);

			boolean firstTrue = const1 != null && const1.valueEquals(1);
			boolean secondFalse = const2 != null && const2.valueEquals(0);

			if (firstTrue & secondFalse) {
				shortCondition = condition;

			} else if (firstTrue) {
				shortCondition = OperatorCondition.or(
						condition,
						new CompareCondition(CompareType.NOT_EQUALS, PrimitiveType.BOOLEAN, ConstCondition.FALSE, operand2)
				);

			} else if (secondFalse) {
				shortCondition = OperatorCondition.and(
						condition,
						new CompareCondition(CompareType.NOT_EQUALS, PrimitiveType.BOOLEAN, ConstCondition.FALSE, operand1)
				);
			}
		}
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(condition, operand1, operand2);
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(condition).addImportsFor(operand1).addImportsFor(operand2);
	}

	@Override
	public Priority getPriority() {
		return shortCondition != null ? shortCondition.getPriority() : Priority.TERNARY;
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		var priority = getPriority();

		if (shortCondition != null) {
			out.record(shortCondition, context, priority);
		} else {
			// Левая ассоциативность нудна для большей ясности кода,
			// когда один тернарный оператор вложен в другой
			out.record(condition, context, priority, Associativity.LEFT).record(" ? ")
					.record(operand1, context, priority, Associativity.LEFT).record(" : ")
					.record(operand2, context, priority, Associativity.LEFT);
		}
	}

	@Override
	public String toString() {
		return String.format("TernaryOperator %08x(%s %s %s)",
				hashCode(), condition, operand1, operand2);
	}
}
