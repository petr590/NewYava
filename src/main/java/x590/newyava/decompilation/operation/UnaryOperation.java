package x590.newyava.decompilation.operation;

import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

import java.util.List;

public class UnaryOperation implements Operation {
	private final Operation operand;
	private final String operator;

	private final Type requiredType, returnType;

	public UnaryOperation(MethodContext context, String operator, Type requiredType) {
		this.operand = context.popAs(requiredType);
		this.operator = operator;
		this.requiredType = requiredType;
		this.returnType = requiredType;
	}

	@Override
	public Type getReturnType() {
		return returnType;
	}

	@Override
	public void inferType(Type ignored) {
		operand.inferType(requiredType);
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(operand);
	}

	@Override
	public Priority getPriority() {
		return Priority.UNARY;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(operand);
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		out.record(operator);

		// Нужно избегать записей типа --x, когда на самом деле должно быть -(-x)
		if (operand instanceof UnaryOperation unary && unary.operator.equals(operator))
			out.space();

		out.record(operand, context, getPriority());
	}

	@Override
	public String toString() {
		return String.format("UnaryOperation %08x(%s%s)", hashCode(), operator, operand);
	}
}
