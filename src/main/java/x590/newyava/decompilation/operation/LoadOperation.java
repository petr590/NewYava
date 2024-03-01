package x590.newyava.decompilation.operation;

import org.objectweb.asm.Opcodes;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

public class LoadOperation implements Operation {

	private final VariableReference variable;
	private final boolean isThisRef;

	public LoadOperation(MethodContext context, int slotId, Type requiredType) {
		this.variable = context.getVariable(slotId);
		variable.assignType(requiredType);

		this.isThisRef = (context.getModifiers() & Opcodes.ACC_STATIC) == 0 && slotId == 0;
	}

	@Override
	public boolean isThisRef() {
		return isThisRef;
	}

	@Override
	public Type getReturnType() {
		return variable.getType();
	}

	@Override
	public void updateReturnType(Type newType) {
		variable.updateType(newType);
	}

	@Override
	public void write(DecompilationWriter out, ClassContext context) {
		out.record(variable.getName());
	}
}
