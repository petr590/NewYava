package x590.newyava.decompilation.operation.terminal;

import x590.newyava.context.WriteContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

public enum ReturnVoidOperation implements ReturnOperation {
	INSTANCE;

	@Override
	public Type getReturnType() {
		return PrimitiveType.VOID;
	}

	@Override
	public void write(DecompilationWriter out, WriteContext context) {
		out.record("return");
	}
}
