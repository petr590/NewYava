package x590.newyava.constant;

import lombok.RequiredArgsConstructor;
import x590.newyava.context.ClassContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

@RequiredArgsConstructor
public final class LongConstant extends Constant {

	private final long value;

	@Override
	public Type getType() {
		return PrimitiveType.LONG;
	}

	@Override
	public void addImports(ClassContext context) {}

	@Override
	public void write(DecompilationWriter out, ClassContext context, Type type) {
		out.record(String.valueOf(value)).record('l');
	}
}
