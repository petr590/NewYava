package x590.newyava.decompilation.operation;

import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

import java.util.List;
import java.util.Objects;

public class StoreOperation extends AssignOperation {
	private final VariableReference varRef;

	private boolean definition;

	public StoreOperation(MethodContext context, int slotId, Type requiredType) {
		super(
				context, context.popAs(requiredType), null,
				operation -> operation instanceof LoadOperation load && load.getSlotId() == slotId
		);

		this.varRef = context.getVarRef(slotId);
	}

	@Override
	public boolean usesAnyVariable() {
		return true;
	}

	@Override
	public void inferType(Type ignored) {
		super.inferType(ignored);

		varRef.assignUp(Objects.requireNonNull(value).getReturnType());
		value.inferType(varRef.getType());
	}

	@Override
	public void declareVariableOnStore() {
		if (varRef.attemptDeclare()) {
			definition = true;
		}

		super.declareVariableOnStore();
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(Objects.requireNonNull(value));
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(value);

		if (definition) {
			context.addImport(varRef.getType());
		}
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		if (definition) {
			out.recordSp(varRef.getType(), context);
		}

		super.write(out, context);
	}

	@Override
	protected void writeTarget(DecompilationWriter out, MethodWriteContext context) {
		out.record(varRef.getName());
	}

	@Override
	protected void writeValue(DecompilationWriter out, MethodWriteContext context) {
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
