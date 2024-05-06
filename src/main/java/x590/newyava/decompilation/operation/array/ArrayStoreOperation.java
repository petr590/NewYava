package x590.newyava.decompilation.operation.array;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.LdcOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ArrayType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ArrayStoreOperation implements Operation {

	private final Operation array, index, value;

	private final Type requiredType;

	public static @Nullable ArrayStoreOperation valueOf(MethodContext context, Type requiredType) {
		var value = context.popAs(requiredType);
		var index = context.popAs(PrimitiveType.INT);
		var array = context.popAs(ArrayType.forType(requiredType));

		if (array instanceof NewArrayOperation newArray &&
			!context.isStackEmpty() && context.peek() == newArray) {

			var indexConst = LdcOperation.getIntConstant(index);

			if (indexConst != null && newArray.addInitializer(indexConst.getValue(), value)) {
				return null;
			}
		}

		return new ArrayStoreOperation(array, index, value, requiredType);
	}

	@Override
	public Type getReturnType() {
		return PrimitiveType.VOID;
	}

	@Override
	public void inferType(Type ignored) {
		value.inferType(requiredType);
		index.inferType(PrimitiveType.INT);
		array.inferType(ArrayType.forType(requiredType));
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(array, index, value);
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(array).addImportsFor(index).addImportsFor(value);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.record(array, context, getPriority())
			.record('[').record(index, context, Priority.ZERO).record("] = ")
			.record(value, context, Priority.ASSIGNMENT);
	}

	@Override
	public String toString() {
		return String.format("Operation %08x(%s[%s] = %s)", hashCode(), array, index, value);
	}
}
