package x590.newyava.decompilation.instruction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Label;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.ReturnOperation;
import x590.newyava.decompilation.operation.ReturnVoidOperation;
import x590.newyava.exception.UnknownOpcodeException;
import x590.newyava.type.AnyObjectType;
import x590.newyava.type.PrimitiveType;

import java.util.Collections;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public record ReturnInsn(int opcode) implements FlowControlInsn {
	@Override
	public @Unmodifiable List<Label> getLabels() {
		return Collections.emptyList();
	}

	@Override
	public boolean canStay() {
		return false;
	}

	@Override
	public int getOpcode() {
		return opcode;
	}

	@Override
	public @NotNull Operation toOperation(MethodContext context) {
		return switch (opcode) {
			case IRETURN -> new ReturnOperation(context, PrimitiveType.INTEGRAL);
			case LRETURN -> new ReturnOperation(context, PrimitiveType.LONG);
			case FRETURN -> new ReturnOperation(context, PrimitiveType.FLOAT);
			case DRETURN -> new ReturnOperation(context, PrimitiveType.DOUBLE);
			case ARETURN -> new ReturnOperation(context, AnyObjectType.INSTANCE);
			case RETURN -> ReturnVoidOperation.INSTANCE;
			default -> throw new UnknownOpcodeException(opcode);
		};
	}
}
