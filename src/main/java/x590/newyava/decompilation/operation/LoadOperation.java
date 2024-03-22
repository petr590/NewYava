package x590.newyava.decompilation.operation;

import lombok.Getter;
import org.objectweb.asm.Opcodes;
import x590.newyava.context.MethodContext;
import x590.newyava.context.WriteContext;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

public class LoadOperation implements Operation {

	@Getter
	private final VariableReference varRef;
	private final boolean isThisRef;

	public LoadOperation(MethodContext context, int slotId, Type requiredType) {
		this.varRef = context.getVarRef(slotId);
		varRef.assignType(requiredType);

		this.isThisRef = (context.getModifiers() & Opcodes.ACC_STATIC) == 0 && slotId == 0;
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
	public void updateReturnType(Type newType) {
		varRef.updateType(newType);
	}

	@Override
	public void write(DecompilationWriter out, WriteContext context) {
		out.record(varRef.getName());
	}
}
