package x590.newyava.type;

import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;

/**
 * @deprecated используйте {@link Types#ANY_TYPE}.
 */
@Deprecated(since = "0.8", forRemoval = true)
public enum AnyType implements Type {
	INSTANCE;

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.record(ClassType.OBJECT, context);
	}


	@Override
	public String toString() {
		return "<any-type>";
	}
}
