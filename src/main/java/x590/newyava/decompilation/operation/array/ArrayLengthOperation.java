package x590.newyava.decompilation.operation.array;

import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;
import x590.newyava.type.Types;

import java.util.List;

public class ArrayLengthOperation implements Operation {
	private final Operation array;

	public ArrayLengthOperation(MethodContext context) {
		this.array = context.popAs(Types.ANY_ARRAY_TYPE);
	}

	@Override
	public Type getReturnType() {
		return PrimitiveType.INT;
	}

	public void inferType(Type ignored) {
		array.inferType(Types.ANY_ARRAY_TYPE);
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(array);
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(array);
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		out.record(array, context, getPriority()).record(".length");
	}

	@Override
	public String toString() {
		return String.format("ArrayLengthOperation %08x(%s)", hashCode(), array);
	}
}
