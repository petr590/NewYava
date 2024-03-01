package x590.newyava.decompilation.instruction;

import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.invoke.InvokeSpecialOperation;
import x590.newyava.decompilation.operation.invoke.InvokeStaticOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.invoke.InvokeVirtualOperation;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.exception.UnknownOpcodeException;
import x590.newyava.type.ReferenceType;

import static org.objectweb.asm.Opcodes.*;

public record MethodInsn(int opcode, String hostClass, String name, String argsAndReturnType, boolean isInterface)
		implements Instruction {

	@Override
	public int getOpcode() {
		return opcode;
	}

	@Override
	public Operation toOperation(MethodContext context) {
		var descriptor = MethodDescriptor.of(ReferenceType.valueOf(hostClass), name, argsAndReturnType);

		return switch (opcode) {
			case INVOKEVIRTUAL, INVOKEINTERFACE -> new InvokeVirtualOperation(context, descriptor);
			case INVOKESPECIAL -> new InvokeSpecialOperation(context, descriptor);
			case INVOKESTATIC -> new InvokeStaticOperation(context, descriptor);

			default -> throw new UnknownOpcodeException(opcode);
		};
	}
}
