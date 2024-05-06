package x590.newyava.decompilation.operation.array;

import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ArrayType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.List;

public class ArrayLoadOperation implements Operation {

	private final Operation array, index;

	private final Type requiredType, returnType;

	public ArrayLoadOperation(MethodContext context, Type requiredType) {
		this.index = context.popAs(PrimitiveType.INT);
		this.array = context.popAs(ArrayType.forType(requiredType));
		this.requiredType = requiredType;
		this.returnType = ((ArrayType)array.getReturnType()).getElementType();
	}

	@Override
	public Type getReturnType() {
		return returnType;
	}

	@Override
	public void inferType(Type ignored) {
		index.inferType(PrimitiveType.INT);
		array.inferType(requiredType);
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(array, index);
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(array).addImportsFor(index);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.record(array, context, getPriority())
			.record('[').record(index, context, Priority.ZERO).record(']');
	}

	@Override
	public String toString() {
		return String.format("ArrayLoadOperation %08x(%s[%s])", hashCode(), array, index);
	}
}
