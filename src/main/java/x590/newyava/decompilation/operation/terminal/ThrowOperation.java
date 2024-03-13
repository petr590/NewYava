package x590.newyava.decompilation.operation.terminal;

import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.WriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

public class ThrowOperation implements TerminalOperation {

	private final Operation exception;

	public ThrowOperation(MethodContext context) {
		this.exception = context.popAs(ClassType.THROWABLE);
	}

	@Override
	public Type getReturnType() {
		return PrimitiveType.VOID;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(exception);
	}

	@Override
	public void write(DecompilationWriter out, WriteContext context) {
		out.recordsp("throw").record(exception, context, Priority.ZERO);
	}
}
