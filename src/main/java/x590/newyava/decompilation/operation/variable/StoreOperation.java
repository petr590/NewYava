package x590.newyava.decompilation.operation.variable;

import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.other.AssignOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.variable.VarUsage;
import x590.newyava.decompilation.variable.Variable;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
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

	private StoreOperation(MethodContext context, int slotId, Operation value) {
		super(
				context, value, null,
				operation -> operation instanceof ILoadOperation load && load.getSlotId() == slotId
		);

		this.varRef = context.getVarRef(slotId);
	}

	@Override
	public boolean needWrapWithBrackets() {
		return declaration;
	}

	@Override
	public boolean usesAnyVariable() {
		return true;
	}

	@Override
	public boolean usesVariable(Variable variable) {
		return varRef.getVariable() == variable || super.usesVariable(variable);
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

		varRef.assignUp(requireValue().getReturnType());
		requireValue().inferType(varRef.getType());
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
	public void initPossibleVarNames() {
		super.initPossibleVarNames();
		varRef.requireVariable().addPossibleName(requireValue().getPossibleVarName().orElse(null));
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(requireValue());
	}

	@Override
	public void addImports(ClassContext context) {
		super.addImports(context);

		if (declaration) {
			context.addImport(varRef.getType());
		}
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		if (declaration) {
			out.record(varRef.getType(), context).space();
		}

		super.write(out, context);
	}

	@Override
	protected void writeTarget(DecompilationWriter out, MethodWriteContext context) {
		out.record(varRef.getName());
	}

	@Override
	protected void writeShortValue(DecompilationWriter out, MethodWriteContext context) {
		out.record(
				requireShortValue(), context, getPriority(),
				declaration && Type.isArray(varRef.getType()) ?
						Operation::writeAsArrayInitializer :
						Operation::write
		);
	}

	@Override
	public String toString() {
		return String.format("StoreOperation(%s = %s)", varRef, value);
	}
}
