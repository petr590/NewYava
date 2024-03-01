package x590.newyava.decompilation.operation;

import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ArrayType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

public class ArrayStoreOperation implements Operation {

	private final Operation array, index, value;

	public ArrayStoreOperation(MethodContext context, Type requiredType) {
		this.value = context.popAs(requiredType);
		this.index = context.popAs(PrimitiveType.INT);
		this.array = context.popAs(ArrayType.forType(requiredType));
	}

	@Override
	public Type getReturnType() {
		return PrimitiveType.VOID;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(array).addImportsFor(index).addImportsFor(value);
	}

	@Override
	public void write(DecompilationWriter out, ClassContext context) {
		out.record(array, context, getPriority())
			.record('[').record(index, context, Priority.ZERO).record("] = ")
			.record(value, context, Priority.ASSIGNMENT);
	}
}
