package x590.newyava.decompilation.operation.variable;

import lombok.EqualsAndHashCode;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.variable.VarUsage;
import x590.newyava.decompilation.variable.Variable;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

@EqualsAndHashCode
public class IIncOperation implements Operation {
	private final VariableReference varRef;

	private final int value;

	private final boolean preInc, hasReturnType;

	private IIncOperation(MethodContext context, int slotId, int value, boolean preInc, boolean hasReturnType) {
		this.varRef = context.getVarRef(slotId);
		this.value = value;
		this.preInc = preInc;
		this.hasReturnType = hasReturnType;
	}

	public static IIncOperation voidInc(MethodContext context, int slotId, int value) {
		return new IIncOperation(context, slotId, value, false, false);
	}

	public static IIncOperation preInc(MethodContext context, int slotId, int value) {
		return new IIncOperation(context, slotId, value, true, true);
	}

	public static IIncOperation postInc(MethodContext context, int slotId, int value) {
		return new IIncOperation(context, slotId, value, false, true);
	}


	@Override
	public boolean usesAnyVariable() {
		return true;
	}

	@Override
	public boolean usesVariable(Variable variable) {
		return varRef.getVariable() == variable || Operation.super.usesVariable(variable);
	}

	@Override
	public VarUsage getVarUsage(int slotId) {
		return slotId == varRef.getSlotId() ? VarUsage.LOAD : VarUsage.NONE;
	}

	@Override
	public void inferType(Type ignored) {
		varRef.assignUp(PrimitiveType.INT);
	}

	@Override
	public Type getReturnType() {
		return hasReturnType ? varRef.getType() : PrimitiveType.VOID;
	}

	@Override
	public Priority getPriority() {
		return preInc ? Priority.PRE_INC_DEC : Priority.POST_INC_DEC;
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		int value = this.value;

		if (value == 1 || value == -1) {
			if (preInc) {
				out.record(value > 0 ? "++" : "--");
				out.record(varRef.getName());
			} else {
				out.record(varRef.getName());
				out.record(value > 0 ? "++" : "--");
			}

		} else {
			out.record(varRef.getName()).wrapSpaces(value > 0 ? "+=" : "-=").record(String.valueOf(Math.abs(value)));
		}
	}

	@Override
	public String toString() {
		return String.format("IIncOperation(%s %c= %d)", varRef, value > 0 ? '+' : '-', Math.abs(value));
	}
}
