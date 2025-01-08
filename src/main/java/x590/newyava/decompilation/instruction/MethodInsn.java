package x590.newyava.decompilation.instruction;

import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.invoke.InvokeSpecialOperation;
import x590.newyava.decompilation.operation.invoke.InvokeStaticOperation;
import x590.newyava.decompilation.operation.invoke.InvokeVIOperation;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.exception.UnknownOpcodeException;
import x590.newyava.type.IClassArrayType;

import static org.objectweb.asm.Opcodes.*;

public record MethodInsn(int opcode, String hostClass, String name, String argsAndReturnType)
		implements Instruction {

	@Override
	public int getOpcode() {
		return opcode;
	}

	@Override
	public Operation toOperation(MethodContext context) {
		var descriptor = MethodDescriptor.of(IClassArrayType.valueOf(hostClass), name, argsAndReturnType);

		return switch (opcode) {
			case INVOKESPECIAL   -> InvokeSpecialOperation.valueOf(context, descriptor);
			case INVOKESTATIC    -> InvokeStaticOperation.valueOf(context, descriptor);
			case INVOKEVIRTUAL   -> InvokeVIOperation.invokeVirtual(context, descriptor);
			case INVOKEINTERFACE -> InvokeVIOperation.invokeInterface(context, descriptor);

			default -> throw new UnknownOpcodeException(opcode);
		};
	}
}
