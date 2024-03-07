package x590.newyava.constant;

import lombok.RequiredArgsConstructor;
import x590.newyava.context.ClassContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

@RequiredArgsConstructor
public final class DoubleConstant extends Constant {

	public static final DoubleConstant
			ZERO = new DoubleConstant(0),
			ONE = new DoubleConstant(1);

	private final double value;

	@Override
	public Type getType() {
		return PrimitiveType.DOUBLE;
	}

	@Override
	public void addImports(ClassContext context) {}

	@Override
	public void write(DecompilationWriter out, ClassContext context, Type type) {
		out.record(String.valueOf(value));
	}
}
