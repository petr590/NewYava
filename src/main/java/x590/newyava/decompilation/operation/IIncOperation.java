package x590.newyava.decompilation.operation;

import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

public class IIncOperation implements Operation {
	private final VariableReference varRef;

	private final int value;

	public IIncOperation(MethodContext context, int varIndex, int value) {
		this.varRef = context.getVarRef(varIndex);
		this.value = value;
	}


	@Override
	public Type getReturnType() {
		return PrimitiveType.VOID;
	}

	@Override
	public Priority getPriority() {
		return Priority.POST_INC_DEC;
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.record(varRef.getName());

		int value = this.value;

		if (value == 1 || value == -1) {
			out.record(value > 0 ? "++" : "--");
		} else {
			out.recordSp().recordSp(value > 0 ? "+=" : "-=").record(String.valueOf(Math.abs(value)));
		}
	}
}
