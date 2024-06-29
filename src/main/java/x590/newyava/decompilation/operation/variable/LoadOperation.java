package x590.newyava.decompilation.operation.variable;

import lombok.Getter;
import x590.newyava.decompilation.variable.VarUsage;
import x590.newyava.modifiers.Modifiers;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

public class LoadOperation implements ILoadOperation {
	@Getter
	private final VariableReference varRef;

	private final Type requiredType;

	private final boolean isThisRef;

	public LoadOperation(MethodContext context, int slotId, Type requiredType) {
		this.varRef = context.getVarRef(slotId);
		this.requiredType = requiredType;

		this.isThisRef = slotId == 0 && (context.getModifiers() & Modifiers.ACC_STATIC) == 0;
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
	public VarUsage getVarUsage(int slotId) {
		return slotId == varRef.getSlotId() ? VarUsage.LOAD : VarUsage.NONE;
	}

	@Override
	public void inferType(Type requiredType) {
		varRef.assignDown(Type.assignDown(this.requiredType, requiredType));
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		out.record(varRef.getName());
	}

	@Override
	public String toString() {
		return String.format("LoadOperation %08x(%s)", hashCode(), varRef);
	}
}
