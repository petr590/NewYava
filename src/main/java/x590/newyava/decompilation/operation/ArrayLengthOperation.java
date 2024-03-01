package x590.newyava.decompilation.operation;

import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;
import x590.newyava.type.Types;

public class ArrayLengthOperation implements Operation {
	private final Operation array;

	public ArrayLengthOperation(MethodContext context) {
		this.array = context.popAs(Types.ANY_ARRAY_TYPE);
	}

	@Override
	public Type getReturnType() {
		return PrimitiveType.INT;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(array);
	}

	@Override
	public void write(DecompilationWriter out, ClassContext context) {
		out.record(array, context, getPriority()).record(".length");
	}
}
