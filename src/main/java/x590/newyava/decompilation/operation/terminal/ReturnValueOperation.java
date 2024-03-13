package x590.newyava.decompilation.operation.terminal;

import lombok.Getter;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.WriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

public class ReturnValueOperation implements ReturnOperation {
	@Getter
	private final Operation value;

	public ReturnValueOperation(MethodContext context, Type requiredType) {
		this.value = context.popAs(requiredType);
	}

	@Override
	public Type getReturnType() {
		return PrimitiveType.VOID;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(value);
	}

	@Override
	public void write(DecompilationWriter out, WriteContext context) {
		out.recordsp("return").record(value, context, Priority.ZERO);
	}
}
