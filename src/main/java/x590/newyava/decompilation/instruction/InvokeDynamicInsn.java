package x590.newyava.decompilation.instruction;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;

public record InvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object[] bootstrapMethodArguments)
		implements Instruction {

	@Override
	public int getOpcode() {
		return Opcodes.INVOKEDYNAMIC;
	}

	@Override
	public Operation toOperation(MethodContext context) {
		return null; // TODO
	}
}
