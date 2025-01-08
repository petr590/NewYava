package x590.newyava.decompilation.instruction;

import org.jetbrains.annotations.Nullable;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.variable.IIncOperation;

import static org.objectweb.asm.Opcodes.*;

public record IIncInsn(int slotId, int increment) implements Instruction {

	@Override
	public int getOpcode() {
		return IINC;
	}


	@Override
	public @Nullable Operation toOperation(MethodContext context, Instruction next) {
		if (next.getOpcode() == ILOAD && next instanceof VarInsn iload && iload.slotId() == slotId) {
			return IIncOperation.preInc(context, slotId, increment);
		}

		return null;
	}

	@Override
	public Operation toOperation(MethodContext context) {
		return IIncOperation.voidInc(context, slotId, increment);
	}
}
