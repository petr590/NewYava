package x590.newyava.type;

import x590.newyava.context.ClassContext;
import x590.newyava.io.DecompilationWriter;

public enum AnyType implements Type {
	INSTANCE;

	@Override
	public void write(DecompilationWriter out, ClassContext context) {
		out.record(ClassType.OBJECT, context);
	}


	@Override
	public String toString() {
		return "AnyType";
	}
}
