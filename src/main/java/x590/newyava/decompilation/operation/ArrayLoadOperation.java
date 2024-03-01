package x590.newyava.decompilation.operation;

import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ArrayType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

public class ArrayLoadOperation implements Operation {

	private final Operation array, index;

	private final Type returnType;

	public ArrayLoadOperation(MethodContext context, Type requiredType) {
		this.index = context.popAs(PrimitiveType.INT);
		this.array = context.popAs(ArrayType.forType(requiredType));
		this.returnType = ((ArrayType)array.getReturnType()).getElementType();
	}

	@Override
	public Type getReturnType() {
		return returnType;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(array).addImportsFor(index);
	}

	@Override
	public void write(DecompilationWriter out, ClassContext context) {
		out.record(array, context, getPriority())
			.record('[').record(index, context, Priority.ZERO).record(']');
	}
}
