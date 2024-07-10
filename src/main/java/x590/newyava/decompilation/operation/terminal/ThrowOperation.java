package x590.newyava.decompilation.operation.terminal;

import lombok.Getter;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.List;

public class ThrowOperation implements TerminalOperation {
	@Getter
	private final Operation exception;

	public ThrowOperation(MethodContext context) {
		this.exception = context.popAs(ClassType.THROWABLE);
	}

	@Override
	public Type getReturnType() {
		return PrimitiveType.VOID;
	}

	@Override
	public void inferType(Type ignored) {
		exception.inferType(ClassType.THROWABLE);
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(exception);
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(exception);
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		out.recordSp("throw").record(exception, context, Priority.ZERO);
	}

	@Override
	public String toString() {
		return String.format("ThrowOperation %08x(%s)", hashCode(), exception);
	}
}
