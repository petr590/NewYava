package x590.newyava.constant;

import lombok.RequiredArgsConstructor;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.Type;

@RequiredArgsConstructor
public final class StringConstant extends Constant {

	private final String value;

	@Override
	public Type getType() {
		return ClassType.STRING;
	}

	@Override
	public void addImports(ClassContext context) {}

	@Override
	public void write(DecompilationWriter out, Context context, Type type) {
		out.record('"').record(JavaEscapeUtils.escapeString(value)).record('"');
	}
}
