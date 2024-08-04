package x590.newyava.decompilation.instruction;

import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.other.FieldOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.exception.UnknownOpcodeException;

import static org.objectweb.asm.Opcodes.*;

public record FieldInsn(int opcode, String className, String name, String typeName)
		implements Instruction {

	@Override
	public int getOpcode() {
		return opcode;
	}

	@Override
	public Operation toOperation(MethodContext context) {
		var descriptor = FieldDescriptor.of(className, name, typeName);

		return switch (opcode) {
			case GETSTATIC -> FieldOperation.getStatic(context, descriptor);
			case PUTSTATIC -> FieldOperation.putStatic(context, descriptor, context.popAs(descriptor.type()));
			case GETFIELD  -> FieldOperation.getField(context, descriptor, context.popAs(descriptor.hostClass()));
			case PUTFIELD  -> FieldOperation.putField(context, descriptor, context.popAs(descriptor.type()), context.popAs(descriptor.hostClass()));
			default -> throw new UnknownOpcodeException(opcode);
		};
	}
}
