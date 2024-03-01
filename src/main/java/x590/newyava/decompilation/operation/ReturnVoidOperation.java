package x590.newyava.decompilation.operation;

import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

public enum ReturnVoidOperation implements Operation {
	INSTANCE;

	@Override
	public Type getReturnType() {
		return PrimitiveType.VOID;
	}

	@Override
	public void write(DecompilationWriter out, ClassContext context) {
		out.record("return");
	}
}
