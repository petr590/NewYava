package x590.newyava.decompilation.instruction;

import org.jetbrains.annotations.Nullable;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.IIncOperation;
import x590.newyava.decompilation.operation.LoadOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.StoreOperation;
import x590.newyava.exception.UnknownOpcodeException;
import x590.newyava.type.AnyObjectType;
import x590.newyava.type.PrimitiveType;

import static org.objectweb.asm.Opcodes.*;

public record VarInsn(int opcode, int slotId) implements Instruction {

	@Override
	public int getOpcode() {
		return opcode;
	}

	@Override
	public @Nullable Operation toOperation(MethodContext context, Instruction next) {
		if (opcode == ILOAD && next instanceof IIncInsn iinc && iinc.slotId() == slotId) {
			return IIncOperation.postInc(context, iinc.slotId(), iinc.increment());
		}

		return null;
	}

	@Override
	public Operation toOperation(MethodContext context) {
		return switch (opcode) {
			case ILOAD -> new LoadOperation(context, slotId, PrimitiveType.INTEGRAL);
			case LLOAD -> new LoadOperation(context, slotId, PrimitiveType.LONG);
			case FLOAD -> new LoadOperation(context, slotId, PrimitiveType.FLOAT);
			case DLOAD -> new LoadOperation(context, slotId, PrimitiveType.DOUBLE);
			case ALOAD -> new LoadOperation(context, slotId, AnyObjectType.INSTANCE);

			case ISTORE -> new StoreOperation(context, slotId, PrimitiveType.INTEGRAL);
			case LSTORE -> new StoreOperation(context, slotId, PrimitiveType.LONG);
			case FSTORE -> new StoreOperation(context, slotId, PrimitiveType.FLOAT);
			case DSTORE -> new StoreOperation(context, slotId, PrimitiveType.DOUBLE);
			case ASTORE -> new StoreOperation(context, slotId, AnyObjectType.INSTANCE);

			default -> throw new UnknownOpcodeException(opcode);
		};
	}

	@Override
	public String toString() {
		return String.format("VarInsn(%s, slot=%d)", InsnUtil.opcodeToString(opcode), slotId);
	}
}
