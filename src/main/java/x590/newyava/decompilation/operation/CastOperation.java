package x590.newyava.decompilation.operation;

import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

import java.util.List;

public class CastOperation implements Operation {
	private final Operation operand;
	private final Type returnType;

	public CastOperation(MethodContext context, Type requiredType, Type returnType) {
		this.operand = context.popAs(requiredType);
		this.returnType = returnType;
	}


	@Override
	public Type getReturnType() {
		return returnType;
	}

	@Override
	public Priority getPriority() {
		return Priority.CAST;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImport(returnType).addImportsFor(operand);
	}


	@Override
	public void write(DecompilationWriter out, Context context) {
		out.record('(').record(returnType, context).record(')')
			.record(operand, context, getPriority());
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(operand);
	}
}
