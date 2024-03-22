package x590.newyava.decompilation.instruction;

import org.objectweb.asm.Opcodes;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.array.NewArrayOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.type.ArrayType;
import x590.newyava.type.PrimitiveType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record MultiANewArrayInsn(String descriptor, int dimensions) implements Instruction {

	@Override
	public int getOpcode() {
		return Opcodes.MULTIANEWARRAY;
	}

	@Override
	public Operation toOperation(MethodContext context) {
		int dimensions = this.dimensions;

		List<Operation> sizes = new ArrayList<>(dimensions);

		for (int i = 0; i < dimensions; i++) {
			sizes.add(context.popAs(PrimitiveType.INT));
		}

		Collections.reverse(sizes);

		return new NewArrayOperation(ArrayType.valueOf(descriptor), sizes);
	}
}
