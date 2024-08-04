package x590.newyava.decompilation.operation.other;

import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.*;

import java.util.List;

public class InstanceofOperation implements Operation {

	private final Operation value;
	private final ClassArrayType type;

	public InstanceofOperation(MethodContext context, ClassArrayType type) {
		this.value = context.popAs(Types.ANY_OBJECT_TYPE);
		this.type = type;
	}

	@Override
	public Type getReturnType() {
		return PrimitiveType.BOOLEAN;
	}

	@Override
	public void inferType(Type ignored) {
		value.inferType(Types.ANY_OBJECT_TYPE);
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(value);
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
	public void write(DecompilationWriter out, MethodWriteContext context) {
		out.record(value, context, getPriority()).record(" instanceof ").record(type, context);
	}

	@Override
	public String toString() {
		return String.format("InstanceofOperation %08x(%s instanceof %s)", hashCode(), value, type);
	}
}
