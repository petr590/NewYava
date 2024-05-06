package x590.newyava.decompilation.operation;

import lombok.RequiredArgsConstructor;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;

@RequiredArgsConstructor
public class NewOperation implements Operation {

	private final ClassType type;

	@Override
	public ClassType getReturnType() {
		return type;
	}

	@Override
	public Priority getPriority() {
		return Priority.NEW;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(type);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.recordSp("new").record(type, context);
	}

	@Override
	public String toString() {
		return String.format("NewOperation %08x(%s)", hashCode(), type);
	}
}
