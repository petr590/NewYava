package x590.newyava.decompilation.operation;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.constant.IntConstant;
import x590.newyava.constant.LongConstant;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

import java.util.List;

public class BinaryOperation implements Operation {

	private final Operation operand1, operand2;
	private final Operator operator;
	private final Type requiredType1, requiredType2, returnType;

	public BinaryOperation(MethodContext context, Operator operator, Type requiredType) {
		this(context, operator, requiredType, requiredType);
	}

	public BinaryOperation(MethodContext context, Operator operator, Type requiredType1, Type requiredType2) {
		this.operand2 = context.popAs(requiredType2);
		this.operand1 = context.popAs(requiredType1);

		if (operator == Operator.XOR && operand2 instanceof LdcOperation ldc &&
			(ldc.getValue().equals(IntConstant.MINUS_ONE) ||
			 ldc.getValue().equals(LongConstant.MINUS_ONE))) {

			this.operator = Operator.NOT;
		} else {
			this.operator = operator;
		}

		this.requiredType1 = requiredType1;
		this.requiredType2 = requiredType2;
		this.returnType = requiredType1;
	}


	@RequiredArgsConstructor
	public enum Operator {
		ADD("+", Priority.ADD_SUB),
		SUB("-", Priority.ADD_SUB),
		MUL("*", Priority.MUL_DIV_REM),
		DIV("/", Priority.MUL_DIV_REM),
		REM("%", Priority.MUL_DIV_REM),
		SHL("<<", Priority.SHIFT),
		SHR(">>", Priority.SHIFT),
		USHR(">>>", Priority.SHIFT),
		AND("&", Priority.BIT_AND),
		XOR("^", Priority.BIT_XOR),
		OR ("|", Priority.BIT_OR),
		NOT("~", Priority.UNARY);

		private final String value;
		private final Priority priority;
	}


	@Override
	public Type getReturnType() {
		return returnType;
	}

	@Override
	public void inferType(Type ignored) {
		operand1.inferType(requiredType1);
		operand2.inferType(requiredType2);
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(operand1, operand2);
	}

	@Override
	public Priority getPriority() {
		return operator.priority;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(operand1).addImportsFor(operand2);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		if (operator == Operator.NOT) {
			out.record(operator.value).record(operand1, context, getPriority(), Associativity.RIGHT);

		} else {
			out.record(operand1, context, getPriority(), Associativity.LEFT)
					.recordSp().recordSp(operator.value)
					.record(operand2, context, getPriority(), Associativity.RIGHT);
		}
	}

	@Override
	public String toString() {
		return String.format("BinaryOperation %08x(%s %s %s)",
				hashCode(), operand1, operator.value, operand2);
	}
}
