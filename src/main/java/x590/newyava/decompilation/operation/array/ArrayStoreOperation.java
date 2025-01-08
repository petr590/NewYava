package x590.newyava.decompilation.operation.array;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.other.AssignOperation;
import x590.newyava.decompilation.operation.other.LdcOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ArrayType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
public class ArrayStoreOperation extends AssignOperation {

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

		return new ArrayStoreOperation(context, array, index, value, requiredType);
	}

	@Getter
	private final Operation array, index;

	@EqualsAndHashCode.Exclude
	private final Type requiredType;


	private ArrayStoreOperation(MethodContext context, Operation array, Operation index, Operation value, Type requiredType) {
		super(
				context, value, null,
				operation -> operation instanceof ArrayLoadOperation aload &&
						aload.getArray() == array &&
						aload.getIndex() == index
		);

		this.array = array;
		this.index = index;
		this.requiredType = requiredType;
	}

	@Override
	public void inferType(Type ignored) {
		super.inferType(ignored);

		requireValue().inferType(requiredType);
		index.inferType(PrimitiveType.INT);
		array.inferType(ArrayType.forType(requiredType));
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(array, index, requireValue());
	}

	@Override
	public void addImports(ClassContext context) {
		super.addImports(context);
		context.addImportsFor(array).addImportsFor(index);
	}

	@Override
	protected void writeTarget(DecompilationWriter out, MethodWriteContext context) {
		out.record(array, context, getPriority())
			.record('[').record(index, context, Priority.ZERO).record(']');
	}

	@Override
	public String toString() {
		return String.format("ArrayStoreOperation(%s[%s] = %s)", array, index, value);
	}
}
