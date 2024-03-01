package x590.newyava.decompilation.instruction;

import org.objectweb.asm.Opcodes;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.IIncOperation;
import x590.newyava.decompilation.operation.Operation;

public record IIncInsn(int varIndex, int increment) implements Instruction {

	@Override
	public int getOpcode() {
		return Opcodes.IINC;
	}

	@Override
	public Operation toOperation(MethodContext context) {
		return new IIncOperation(context, varIndex, increment);
	}
}
