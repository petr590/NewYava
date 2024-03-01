package x590.newyava.decompilation.operation;

import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

public class IIncOperation implements Operation {
	private final VariableReference variable;

	private final int value;

	public IIncOperation(MethodContext context, int varIndex, int value) {
		this.variable = context.getVariable(varIndex);
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
	public void write(DecompilationWriter out, ClassContext context) {
		out.record(variable.getName());

		int value = this.value;

		if (value == 1 || value == -1) {
			out.record(value > 0 ? "++" : "--");
		} else {
			out.recordsp().recordsp(value > 0 ? "+=" : "-=").record(String.valueOf(Math.abs(value)));
		}
	}
}
