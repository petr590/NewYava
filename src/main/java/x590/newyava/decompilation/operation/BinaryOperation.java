package x590.newyava.decompilation.operation;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

import java.util.List;

public class BinaryOperation implements Operation {

	private final Operation operand1, operand2;
	private final Operator operator;
	private final Type returnType;

	public BinaryOperation(MethodContext context, Operator operator, Type requiredType) {
		this(context, operator, requiredType, requiredType);
	}

	public BinaryOperation(MethodContext context, Operator operator, Type requiredType1, Type requiredType2) {
		this.operand2 = context.popAs(requiredType2);
		this.operand1 = context.popAs(requiredType1);
		this.operator = operator;
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
		OR ("|", Priority.BIT_OR);

		private final String value;
		private final Priority priority;
	}


	@Override
	public Type getReturnType() {
		return returnType;
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
		out .record(operand1, context, getPriority(), Associativity.LEFT)
			.recordSp().recordSp(operator.value)
			.record(operand2, context, getPriority(), Associativity.RIGHT);
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(operand1, operand2);
	}
}
