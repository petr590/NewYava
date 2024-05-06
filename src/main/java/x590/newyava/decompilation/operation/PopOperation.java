package x590.newyava.decompilation.operation;

import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;
import x590.newyava.type.TypeSize;

import java.util.List;

public class PopOperation implements Operation {

	private final Operation value;

	public PopOperation(MethodContext context, TypeSize size) {
		this.value = context.popAs(size);
	}

	@Override
	public Type getReturnType() {
		return PrimitiveType.VOID;
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
	public void write(DecompilationWriter out, Context context) {
		out.record(value, context, Priority.ZERO);
	}

	@Override
	public String toString() {
		return String.format("PopOperation %08x(%s)", hashCode(), value);
	}
}
