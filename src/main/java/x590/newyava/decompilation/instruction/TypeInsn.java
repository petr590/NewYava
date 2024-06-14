package x590.newyava.decompilation.instruction;

import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.*;
import x590.newyava.decompilation.operation.array.NewArrayOperation;
import x590.newyava.exception.UnknownOpcodeException;
import x590.newyava.type.*;

import static org.objectweb.asm.Opcodes.*;

public record TypeInsn(int opcode, String typeName) implements Instruction {

	@Override
	public int getOpcode() {
		return opcode;
	}

	@Override
	public Operation toOperation(MethodContext context) {
		return switch (opcode) {
			case NEW -> new NewOperation(ClassType.valueOf(typeName));
			case ANEWARRAY -> new NewArrayOperation(context, ArrayType.forType(ReferenceType.valueOf(typeName)));
			case CHECKCAST -> CastOperation.narrow(context, Types.ANY_OBJECT_TYPE, ReferenceType.valueOf(typeName));
			case INSTANCEOF -> new InstanceofOperation(context, ReferenceType.valueOf(typeName));
			default -> throw new UnknownOpcodeException(opcode);
		};
	}

	@Override
	public String toString() {
		return String.format("TypeInsn(%s, %s)", InsnUtil.opcodeToString(opcode), typeName);
	}
}
