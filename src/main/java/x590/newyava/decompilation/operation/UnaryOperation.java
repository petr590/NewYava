package x590.newyava.decompilation.operation;

import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

import java.util.List;

public class UnaryOperation implements Operation {

	private final Operation operand;
	private final String operator;
	private final Type returnType;

	public UnaryOperation(MethodContext context, String operator, Type requiredType) {
		this.operand = context.popAs(requiredType);
		this.operator = operator;
		this.returnType = requiredType;
	}

	@Override
	public Type getReturnType() {
		return returnType;
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
	public void write(DecompilationWriter out, Context context) {
		out.record(operator).record(operand, context, getPriority());
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(operand);
	}
}
