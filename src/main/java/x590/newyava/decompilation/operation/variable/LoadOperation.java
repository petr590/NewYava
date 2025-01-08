package x590.newyava.decompilation.operation.variable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.variable.VarUsage;
import x590.newyava.decompilation.variable.Variable;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

@EqualsAndHashCode
public class LoadOperation implements ILoadOperation {
	@Getter
	private final VariableReference varRef;

	private final Type requiredType;

	private final boolean isThisRef;

	public LoadOperation(MethodContext context, int slotId, Type requiredType) {
		this.varRef = context.getVarRef(slotId);
		this.requiredType = requiredType;
		this.isThisRef = slotId == 0 && !context.isStatic();
	}

	@Override
	public boolean isThisRef() {
		return isThisRef;
	}

	@Override
	public Type getReturnType() {
		return varRef.getType();
	}

	@Override
	public boolean usesAnyVariable() {
		return true;
	}

	@Override
	public boolean usesVariable(Variable variable) {
		return varRef.getVariable() == variable || ILoadOperation.super.usesVariable(variable);
	}

	@Override
	public VarUsage getVarUsage(int slotId) {
		return slotId == varRef.getSlotId() ? VarUsage.LOAD : VarUsage.NONE;
	}

	@Override
	public void inferType(Type requiredType) {
		varRef.assignDown(Type.assignDown(this.requiredType, requiredType));
	}

	@Override
	public void addPossibleVarName(@Nullable String name) {
		varRef.requireVariable().addPossibleName(name);
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		out.record(varRef.getName());
	}

	@Override
	public String toString() {
		return String.format("LoadOperation(%s)", varRef);
	}
}
