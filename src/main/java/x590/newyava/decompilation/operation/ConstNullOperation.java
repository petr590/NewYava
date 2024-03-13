package x590.newyava.decompilation.operation;

import x590.newyava.context.WriteContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;
import x590.newyava.type.Types;

public enum ConstNullOperation implements Operation {
	INSTANCE;

	@Override
	public void write(DecompilationWriter out, WriteContext context) {
		out.record("null");
	}

	@Override
	public Type getReturnType() {
		return Types.ANY_OBJECT_TYPE;
	}
}
