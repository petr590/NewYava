package x590.newyava.decompilation.operation.condition;

import lombok.RequiredArgsConstructor;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.io.DecompilationWriter;

@RequiredArgsConstructor
public enum ConstCondition implements Condition {
	TRUE("true"),
	FALSE("false");

	private final String literal;

	@Override
	public Condition opposite() {
		return this == TRUE ? FALSE : TRUE;
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		out.record(literal);
	}
}
