package x590.newyava.decompilation.operation.terminal;

import lombok.Getter;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.List;

public class ReturnValueOperation implements ReturnOperation {
	@Getter
	private final Operation value;

	private final Type requiredType;

	public ReturnValueOperation(MethodContext context, Type requiredType) {
		this.value = context.popAs(requiredType);
		this.requiredType = context.getDescriptor().returnType();
	}

	@Override
	public Type getReturnType() {
		return PrimitiveType.VOID;
	}

	@Override
	public void inferType(Type ignored) {
		value.inferType(requiredType);
		value.allowImplicitCast();
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(value);
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(value);
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		out.record("return ").record(value, context, Priority.ZERO);
	}

	@Override
	public String toString() {
		return String.format("ReturnValueOperation %08x(%s)", hashCode(), value);
	}
}
