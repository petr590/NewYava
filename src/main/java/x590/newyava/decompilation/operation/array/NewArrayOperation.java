package x590.newyava.decompilation.operation.array;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.constant.DoubleConstant;
import x590.newyava.constant.FloatConstant;
import x590.newyava.constant.IntConstant;
import x590.newyava.constant.LongConstant;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.WriteContext;
import x590.newyava.decompilation.operation.ConstNullOperation;
import x590.newyava.decompilation.operation.LdcOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.exception.DecompilationException;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ArrayType;
import x590.newyava.type.IntMultiType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NewArrayOperation implements Operation {
	private final ArrayType arrayType;
	private final Type memberType;

	@Getter
	private final @Unmodifiable List<Operation> sizes;

	private final @Nullable List<Operation> initializers;

	private boolean useInitializersList;

	public NewArrayOperation(MethodContext context, ArrayType arrayType) {
		this(arrayType, List.of(context.popAs(PrimitiveType.INT)));
	}

	public NewArrayOperation(ArrayType arrayType, List<Operation> sizes) {
		this.arrayType = arrayType;
		this.memberType = arrayType.getMemberType();
		this.sizes = Collections.unmodifiableList(sizes);

		var indexConst = LdcOperation.getIntConstant(sizes.get(0));

		this.initializers = indexConst == null ? null :
				createInitializers(indexConst.getValue(), arrayType.getElementType());

		this.useInitializersList = indexConst != null && indexConst.getValue() == 0;
	}

	private static List<Operation> createInitializers(int size, Type elementType) {
		var operations = new Operation[size];

		var operation = defaultOperation(elementType);
		operation.updateReturnType(elementType);

		Arrays.fill(operations, operation);
		return Arrays.asList(operations);
	}

	private static Operation defaultOperation(Type elementType) {
		if (elementType instanceof PrimitiveType.NonIntType nonIntType) {
			return new LdcOperation(switch (nonIntType) {
				case LONG -> LongConstant.ZERO;
				case FLOAT -> FloatConstant.ZERO;
				case DOUBLE -> DoubleConstant.ZERO;
				case VOID -> throw new DecompilationException("Array type cannot be void");
			});
		}

		if (elementType instanceof IntMultiType) {
			return new LdcOperation(IntConstant.ZERO);
		}

		return ConstNullOperation.INSTANCE;
	}

	boolean addInitializer(int index, Operation value) {
		if (initializers != null && index < initializers.size()) {
			value.updateReturnType(arrayType.getElementType());
			initializers.set(index, value);

			useInitializersList = true;

			return true;
		} else {
			return false;
		}
	}

	public boolean hasInitializer() {
		return initializers != null && !initializers.isEmpty();
	}

	@Override
	public Type getReturnType() {
		return arrayType;
	}

	@Override
	public Priority getPriority() {
		return Priority.NEW;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImport(memberType).addImportsFor(sizes);

		if (initializers != null)
			context.addImportsFor(initializers);
	}

	@Override
	public void write(DecompilationWriter out, WriteContext context) {
		if (useInitializersList) {
			out .recordsp("new").record(memberType, context)
				.recordN("[]", arrayType.getNestLevel()).recordsp();
		}

		writeAsArrayInitializer(out, context);
	}

	@Override
	public void writeAsArrayInitializer(DecompilationWriter out, WriteContext context) {
		if (useInitializersList) {
			if (Objects.requireNonNull(initializers).isEmpty()) {
				out.record("{}");
			} else {
				out .record("{ ")
					.record(initializers, context, Priority.ZERO, ", ", Operation::writeAsArrayInitializer)
					.record(" }");
			}

		} else {
			out.recordsp("new").record(memberType, context);

			for (Operation index : sizes) {
				out.record('[').record(index, context, Priority.ZERO).record(']');
			}

			out.recordN("[]", arrayType.getNestLevel() - sizes.size());
		}
	}
}
