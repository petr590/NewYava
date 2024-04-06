package x590.newyava.decompilation.operation.terminal;

import x590.newyava.context.Context;
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
	public void write(DecompilationWriter out, Context context) {
		out.record("return");
	}


	@Override
	public String toString() {
		return "ReturnVoidOperation";
	}
}
