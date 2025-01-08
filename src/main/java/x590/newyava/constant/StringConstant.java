package x590.newyava.constant;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import x590.newyava.context.ConstantWriteContext;
import x590.newyava.context.Context;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.Type;
import x590.newyava.util.JavaEscapeUtils;

@EqualsAndHashCode(callSuper = false)
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
	protected @Nullable FieldDescriptor getConstant(Context context) {
		return context.getConstant(value);
	}

	@Override
	public void write(DecompilationWriter out, ConstantWriteContext context) {
		writeConstantOrValue(out, context,
				() -> out.record('"').record(JavaEscapeUtils.escapeString(value)).record('"'));
	}

	@Override
	public String toString() {
		return "StringConstant(\"" + JavaEscapeUtils.escapeString(value) + "\")";
	}
}
