package x590.newyava.decompilation.operation;

import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ArrayType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.List;

public class NewArrayOperation implements Operation {
	private final ArrayType arrayType;
	private final Type memberType;

	private final List<Operation> indexes;

	public NewArrayOperation(MethodContext context, ArrayType arrayType) {
		this(arrayType, List.of(context.popAs(PrimitiveType.INT)));
	}

	public NewArrayOperation(ArrayType arrayType, List<Operation> indexes) {
		this.arrayType = arrayType;
		this.memberType = arrayType.getMemberType();
		this.indexes = indexes;
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
	}

	@Override
	public void write(DecompilationWriter out, ClassContext context) {
		out.recordsp("new").record(memberType, context);

		for (Operation index : indexes) {
			out.record('[').record(index, context, Priority.ZERO).record(']');
		}

		for (int i = indexes.size(); i < arrayType.getNestLevel(); i++) {
			out.record("[]");
		}
	}
}
