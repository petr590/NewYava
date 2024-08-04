package x590.newyava.decompilation.instruction;

import org.jetbrains.annotations.Nullable;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.variable.IIncOperation;
import x590.newyava.decompilation.operation.variable.LoadOperation;
import x590.newyava.decompilation.operation.variable.StoreOperation;
import x590.newyava.exception.UnknownOpcodeException;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Types;

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
			case ALOAD -> new LoadOperation(context, slotId, Types.ANY_OBJECT_TYPE);

			case ISTORE -> StoreOperation.of(context, slotId, PrimitiveType.INTEGRAL);
			case LSTORE -> StoreOperation.of(context, slotId, PrimitiveType.LONG);
			case FSTORE -> StoreOperation.of(context, slotId, PrimitiveType.FLOAT);
			case DSTORE -> StoreOperation.of(context, slotId, PrimitiveType.DOUBLE);
			case ASTORE -> StoreOperation.of(context, slotId, Types.ANY_OBJECT_TYPE);

			default -> throw new UnknownOpcodeException(opcode);
		};
	}

	@Override
	public String toString() {
		return String.format("VarInsn(%s, slot=%d)", InsnUtils.opcodeToString(opcode), slotId);
	}
}
