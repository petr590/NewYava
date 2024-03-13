package x590.newyava.decompilation.operation;

import lombok.RequiredArgsConstructor;
import x590.newyava.context.ClassContext;
import x590.newyava.context.WriteContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.Type;

@RequiredArgsConstructor
public class NewOperation implements Operation {

	private final ClassType type;

	@Override
	public Type getReturnType() {
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
	public void write(DecompilationWriter out, WriteContext context) {
		out.recordsp("new").record(type, context);
	}
}
