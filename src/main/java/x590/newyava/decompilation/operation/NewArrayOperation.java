package x590.newyava.decompilation.operation;

import org.jetbrains.annotations.Nullable;
import x590.newyava.constant.DoubleConstant;
import x590.newyava.constant.FloatConstant;
import x590.newyava.constant.IntConstant;
import x590.newyava.constant.LongConstant;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.exception.DecompilationException;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ArrayType;
import x590.newyava.type.IntMultiType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class NewArrayOperation implements Operation {
	private final ArrayType arrayType;
	private final Type memberType;

	private final List<Operation> indexes;

	private final @Nullable List<Operation> initializers;

	private boolean useInitializersList;

	public NewArrayOperation(MethodContext context, ArrayType arrayType) {
		this(arrayType, List.of(context.popAs(PrimitiveType.INT)));
	}

	public NewArrayOperation(ArrayType arrayType, List<Operation> indexes) {
		this.arrayType = arrayType;
		this.memberType = arrayType.getMemberType();
		this.indexes = indexes;

		var indexConst = LdcOperation.getIntConstant(indexes.get(0));

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
		if (elementType instanceof PrimitiveType primitive) {
			return new LdcOperation(switch (primitive) {
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
		context.addImport(memberType).addImportsFor(indexes);

		if (initializers != null)
			context.addImportsFor(initializers);
	}

	@Override
	public void write(DecompilationWriter out, ClassContext context) {
		if (useInitializersList) {
			out .recordsp("new").record(memberType, context)
				.recordN("[]", arrayType.getNestLevel()).recordsp();
		}

		writeAsArrayInitializer(out, context);
	}

	@Override
	public void writeAsArrayInitializer(DecompilationWriter out, ClassContext context) {
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

			for (Operation index : indexes) {
				out.record('[').record(index, context, Priority.ZERO).record(']');
			}

			out.recordN("[]", arrayType.getNestLevel() - indexes.size());
		}
	}
}
