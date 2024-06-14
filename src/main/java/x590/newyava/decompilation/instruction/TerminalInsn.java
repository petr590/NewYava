package x590.newyava.decompilation.instruction;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.terminal.ReturnValueOperation;
import x590.newyava.decompilation.operation.terminal.ReturnVoidOperation;
import x590.newyava.decompilation.operation.terminal.ThrowOperation;
import x590.newyava.type.AnyObjectType;
import x590.newyava.type.PrimitiveType;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public enum TerminalInsn implements FlowControlInsn {
	IRETURN(Opcodes.IRETURN),
	LRETURN(Opcodes.LRETURN),
	FRETURN(Opcodes.FRETURN),
	DRETURN(Opcodes.DRETURN),
	ARETURN(Opcodes.ARETURN),
	RETURN(Opcodes.RETURN),
	ATHROW(Opcodes.ATHROW);

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
			case IRETURN -> new ReturnValueOperation(context, PrimitiveType.INTEGRAL);
			case LRETURN -> new ReturnValueOperation(context, PrimitiveType.LONG);
			case FRETURN -> new ReturnValueOperation(context, PrimitiveType.FLOAT);
			case DRETURN -> new ReturnValueOperation(context, PrimitiveType.DOUBLE);
			case ARETURN -> new ReturnValueOperation(context, AnyObjectType.INSTANCE);
			case RETURN  -> ReturnVoidOperation.INSTANCE;
			case ATHROW  -> new ThrowOperation(context);
		};
	}

	@Override
	public String toString() {
		return String.format("TerminalInsn(%s)", InsnUtil.opcodeToString(opcode));
	}
}
