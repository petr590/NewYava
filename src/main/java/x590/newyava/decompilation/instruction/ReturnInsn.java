package x590.newyava.decompilation.instruction;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.ReturnOperation;
import x590.newyava.decompilation.operation.ReturnVoidOperation;
import x590.newyava.type.AnyObjectType;
import x590.newyava.type.PrimitiveType;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public enum ReturnInsn implements FlowControlInsn {
	IRETURN(Opcodes.IRETURN),
	LRETURN(Opcodes.LRETURN),
	FRETURN(Opcodes.FRETURN),
	DRETURN(Opcodes.DRETURN),
	ARETURN(Opcodes.ARETURN),
	RETURN(Opcodes.RETURN);

	private final int opcode;

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
		return switch (this) {
			case IRETURN -> new ReturnOperation(context, PrimitiveType.INTEGRAL);
			case LRETURN -> new ReturnOperation(context, PrimitiveType.LONG);
			case FRETURN -> new ReturnOperation(context, PrimitiveType.FLOAT);
			case DRETURN -> new ReturnOperation(context, PrimitiveType.DOUBLE);
			case ARETURN -> new ReturnOperation(context, AnyObjectType.INSTANCE);
			case RETURN  -> ReturnVoidOperation.INSTANCE;
		};
	}
}
