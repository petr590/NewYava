package x590.newyava.decompilation.operation.array;

import lombok.Getter;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ArrayType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.List;

public class ArrayLoadOperation implements Operation {

	@Getter
	private final Operation array, index;

	private final ArrayType arrayRequiredType;
	private Type returnType;

	public ArrayLoadOperation(MethodContext context, Type requiredType) {
		this.index = context.popAs(PrimitiveType.INT);

		this.arrayRequiredType = ArrayType.forType(requiredType);
		this.array = context.popAs(arrayRequiredType);

		this.returnType =
				array.getReturnType() instanceof ArrayType arrayType ?
				arrayType.getElementType() : requiredType;
	}

	@Override
	public Type getReturnType() {
		return returnType;
	}

	@Override
	public void inferType(Type ignored) {
		index.inferType(PrimitiveType.INT);
		array.inferType(arrayRequiredType);

		if (array.getReturnType() instanceof ArrayType arrayType) {
			this.returnType = arrayType.getElementType();
		}
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
	public void write(DecompilationWriter out, MethodWriteContext context) {
		out.record(array, context, getPriority())
			.record('[').record(index, context, Priority.ZERO).record(']');
	}

	@Override
	public String toString() {
		return String.format("ArrayLoadOperation %08x(%s[%s])", hashCode(), array, index);
	}
}
