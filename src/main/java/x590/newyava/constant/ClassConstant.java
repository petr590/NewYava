package x590.newyava.constant;

import lombok.RequiredArgsConstructor;
import x590.newyava.context.ClassContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.Type;

@RequiredArgsConstructor
public final class ClassConstant extends Constant {

	private final Type type;

	@Override
	public Type getType() {
		return ClassType.CLASS;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImport(type);
	}

	@Override
	public void write(DecompilationWriter out, ClassContext context, Type type) {
		out.record(type, context).record(".class");
	}
}
