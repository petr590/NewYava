package x590.newyava.decompilation.instruction;

import x590.newyava.constant.IntConstant;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.LdcOperation;
import x590.newyava.decompilation.operation.NewArrayOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.exception.DisassemblingException;
import x590.newyava.exception.UnknownOpcodeException;
import x590.newyava.type.ArrayType;
import x590.newyava.type.PrimitiveType;

import static org.objectweb.asm.Opcodes.*;

public record IntInsn(int opcode, int operand) implements Instruction {

	@Override
	public int getOpcode() {
		return opcode;
	}

	@Override
	public Operation toOperation(MethodContext context) {
		return switch (opcode) {
			case BIPUSH, SIPUSH -> new LdcOperation(IntConstant.valueOf(operand));

			case NEWARRAY -> new NewArrayOperation(context, ArrayType.forType(
					switch (operand) {
						case T_BOOLEAN -> PrimitiveType.BOOLEAN;
						case T_CHAR    -> PrimitiveType.CHAR;
						case T_FLOAT   -> PrimitiveType.FLOAT;
						case T_DOUBLE  -> PrimitiveType.DOUBLE;
						case T_BYTE    -> PrimitiveType.BYTE;
						case T_SHORT   -> PrimitiveType.SHORT;
						case T_INT     -> PrimitiveType.INT;
						case T_LONG    -> PrimitiveType.LONG;
						default -> throw new DisassemblingException("Illegal type for newarray instruction: 0x" + Integer.toHexString(operand));
					}
			));

			default -> throw new UnknownOpcodeException(opcode);
		};
	}
}
