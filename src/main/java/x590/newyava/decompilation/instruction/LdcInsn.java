package x590.newyava.decompilation.instruction;

import org.objectweb.asm.Opcodes;
import x590.newyava.constant.Constant;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.other.LdcOperation;
import x590.newyava.decompilation.operation.Operation;

public record LdcInsn(Object value) implements Instruction {

	@Override
	public int getOpcode() {
		return Opcodes.LDC;
	}

	@Override
	public Operation toOperation(MethodContext context) {
		return new LdcOperation(Constant.fromObject(value));
	}

	@Override
	public String toString() {
		return String.format("LdcInsn(%s)", value);
	}
}
