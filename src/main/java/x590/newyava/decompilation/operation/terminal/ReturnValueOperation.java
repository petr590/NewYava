package x590.newyava.decompilation.operation.terminal;

import lombok.Getter;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.List;

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
	public void write(DecompilationWriter out, Context context) {
		out.recordSp("return").record(value, context, Priority.ZERO);
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(value);
	}
}
