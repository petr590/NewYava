package x590.newyava.constant;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import x590.newyava.context.ClassContext;
import x590.newyava.context.ConstantWriteContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.Type;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringConstant extends Constant {

	public static StringConstant valueOf(String value) {
		return new StringConstant(value);
	}

	private final String value;

	@Override
	public Type getType() {
		return ClassType.STRING;
	}

	@Override
	public void addImports(ClassContext context) {}

	@Override
	public void write(DecompilationWriter out, ConstantWriteContext context) {
		out.record('"').record(JavaEscapeUtils.escapeString(value)).record('"');
	}

	@Override
	public String toString() {
		return "StringConstant(\"" + JavaEscapeUtils.escapeString(value) + "\")";
	}
}
