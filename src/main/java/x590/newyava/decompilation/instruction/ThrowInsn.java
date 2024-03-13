package x590.newyava.decompilation.instruction;

import org.objectweb.asm.Opcodes;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.terminal.ThrowOperation;

public enum ThrowInsn implements Instruction {
	INSTANCE;

	@Override
	public int getOpcode() {
		return Opcodes.ATHROW;
	}

	@Override
	public Operation toOperation(MethodContext context) {
		return new ThrowOperation(context);
	}
}
