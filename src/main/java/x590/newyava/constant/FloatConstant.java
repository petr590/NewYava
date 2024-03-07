package x590.newyava.constant;

import lombok.RequiredArgsConstructor;
import x590.newyava.context.ClassContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

@RequiredArgsConstructor
public final class FloatConstant extends Constant {

	public static final FloatConstant
			ZERO = new FloatConstant(0),
			ONE = new FloatConstant(1),
			TWO = new FloatConstant(2);

	private final float value;

	@Override
	public Type getType() {
		return PrimitiveType.FLOAT;
	}

	@Override
	public void addImports(ClassContext context) {}

	@Override
	public void write(DecompilationWriter out, ClassContext context, Type type) {
		out.record(String.valueOf(value)).record('f');
	}
}
