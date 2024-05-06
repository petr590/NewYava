package x590.newyava.decompilation.operation;

import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.List;

public class StoreOperation implements Operation {
	private final VariableReference varRef;

	private final Operation value;

	private boolean definition;

	public StoreOperation(MethodContext context, int index, Type requiredType) {
		this.varRef = context.getVarRef(index);
		this.value = context.popAs(requiredType);
	}

	@Override
	public Type getReturnType() {
		return PrimitiveType.VOID;
	}

	@Override
	public boolean usesAnyVariable() {
		return true;
	}

	@Override
	public void inferType(Type ignored) {
		varRef.assignUp(value.getReturnType());
		value.inferType(varRef.getType());
	}

	@Override
	public void defineVariableOnStore() {
		if (varRef.attemptDefine()) {
			definition = true;
		}

		Operation.super.defineVariableOnStore();
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(value);
	}

	@Override
	public Priority getPriority() {
		return Priority.ASSIGNMENT;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(value);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		if (definition) {
			out.record(varRef.getType(), context).recordSp();
		}

		out.record(varRef.getName()).record(" = ");

		out.record(
				value, context, getPriority(),
				definition && Type.isArray(varRef.getType()) ?
						Operation::writeAsArrayInitializer :
						Operation::write
		);
	}

	@Override
	public String toString() {
		return String.format("StoreOperation %08x(%s = %s)", hashCode(), varRef, value);
	}
}
