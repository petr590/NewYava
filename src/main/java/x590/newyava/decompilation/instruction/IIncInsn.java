package x590.newyava.decompilation.instruction;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.IIncOperation;
import x590.newyava.decompilation.operation.Operation;

import static org.objectweb.asm.Opcodes.ILOAD;

public record IIncInsn(int slotId, int increment) implements Instruction {

	@Override
	public int getOpcode() {
		return Opcodes.IINC;
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
