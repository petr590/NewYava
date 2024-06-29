package x590.newyava.decompilation.operation.variable;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.AssignOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.variable.VarUsage;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

import java.util.List;
import java.util.Objects;

public class StoreOperation extends AssignOperation {
	private final VariableReference varRef;

	private boolean declaration;

	public static @Nullable StoreOperation of(MethodContext context, int slotId, Type requiredType) {
		var value = context.popAs(requiredType);

		if (value instanceof CatchOperation catchOp) {
			catchOp.initVarRef(context.getVarRef(slotId));
			return null;
		}

		return new StoreOperation(context, slotId, value);
	}

	public StoreOperation(MethodContext context, int slotId, Operation value) {
		super(
				context, value, null,
				operation -> operation instanceof ILoadOperation load && load.getSlotId() == slotId
		);

		this.varRef = context.getVarRef(slotId);
	}

	@Override
	public boolean usesAnyVariable() {
		return true;
	}

	@Override
	public VarUsage getVarUsage(int slotId) {
		var usage = super.getVarUsage(slotId);
		return usage == VarUsage.NONE && slotId == varRef.getSlotId() ?
				VarUsage.STORE :
				usage;
	}

	@Override
	public void inferType(Type ignored) {
		super.inferType(ignored);

		varRef.assignUp(Objects.requireNonNull(value).getReturnType());
		value.inferType(varRef.getType());
	}

	@Override
	public boolean declareVariables() {
		if (varRef.attemptDeclare()) {
			declaration = true;

			super.declareVariables();
			return true;
		}

		return super.declareVariables();
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(Objects.requireNonNull(value));
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(value);

		if (declaration) {
			context.addImport(varRef.getType());
		}
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		if (declaration) {
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
				declaration && Type.isArray(varRef.getType()) ?
						Operation::writeAsArrayInitializer :
						Operation::write
		);
	}

	@Override
	public String toString() {
		return String.format("StoreOperation %08x(%s = %s)", hashCode(), varRef, value);
	}
}
