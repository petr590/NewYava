package x590.newyava.decompilation.operation;

import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.ReferenceType;
import x590.newyava.type.Type;
import x590.newyava.type.Types;

public class InstanceofOperation implements Operation {

	private final Operation value;
	private final ReferenceType type;

	public InstanceofOperation(MethodContext context, ReferenceType type) {
		this.value = context.popAs(Types.ANY_OBJECT_TYPE);
		this.type = type;
	}

	@Override
	public Type getReturnType() {
		return PrimitiveType.INT;
	}

	@Override
	public Priority getPriority() {
		return Priority.INSTANCEOF;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImport(type).addImportsFor(value);
	}

	@Override
	public void write(DecompilationWriter out, ClassContext context) {
		out.record(value, context, getPriority()).record(" instanceof ").record(type, context);
	}
}
