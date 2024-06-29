package x590.newyava.decompilation.operation;

import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.constant.IntConstant;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.condition.Condition;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.List;

public class TernaryOperator implements Operation {

	private final Condition condition;
	private final Operation operand1, operand2;
	private Type returnType;

	/** Равно {@code true}, когда операция фактически является условием */
	private boolean isCondition;

	public TernaryOperator(Condition condition, Operation operand1, Operation operand2) {
		this.condition = condition;
		this.operand1 = operand1;
		this.operand2 = operand2;

		this.returnType = Type.assignUp(operand1.getReturnType(), operand2.getReturnType());
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

			if (const1 != null && const2 != null &&
				const1.valueEquals(1) && const2.valueEquals(0)) {

				isCondition = true;
			}
		}
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(condition, operand1, operand2);
	}

	@Override
	public Priority getPriority() {
		return isCondition ? condition.getPriority() : Priority.TERNARY;
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		var priority = getPriority();

		if (isCondition) {
			out.record(condition, context, priority);
		} else {
			// Левая ассоциативность нудна для большей ясности кода,
			// когда один тернарный оператор вложен в другой
			out.record(condition, context, priority, Associativity.LEFT).record(" ? ")
					.record(operand1, context, priority, Associativity.LEFT).record(" : ")
					.record(operand2, context, priority, Associativity.LEFT);
		}
	}
}
