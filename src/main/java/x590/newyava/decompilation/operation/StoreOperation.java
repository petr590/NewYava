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

	public StoreOperation(MethodContext context, int index, Type requiredType) {
		this.varRef = context.getVarRef(index);
		varRef.assignType(requiredType);

		this.value = context.popAs(requiredType);
	}

	@Override
	public Type getReturnType() {
		return PrimitiveType.VOID;
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
		out.record(varRef.getName()).record(" = ").record(value, context, getPriority());
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(value);
	}
}
