package x590.newyava.decompilation.instruction;

import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.FieldOperation;
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
			case GETSTATIC -> FieldOperation.getStatic(descriptor);
			case PUTSTATIC -> FieldOperation.putStatic(context, descriptor, context.popAs(descriptor.type()));
			case GETFIELD  -> new FieldOperation(descriptor, context.popAs(descriptor.hostClass()), null);
			case PUTFIELD  -> new FieldOperation(descriptor, context.popAs(descriptor.hostClass()), context.popAs(descriptor.type()));
			default -> throw new UnknownOpcodeException(opcode);
		};
	}
}
