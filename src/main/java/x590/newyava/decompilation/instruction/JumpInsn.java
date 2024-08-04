package x590.newyava.decompilation.instruction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.condition.CompareType;
import x590.newyava.decompilation.operation.condition.GotoOperation;
import x590.newyava.decompilation.operation.condition.IfOperation;
import x590.newyava.exception.UnknownOpcodeException;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public record JumpInsn(int opcode, Label label) implements FlowControlInsn {
	@Override
	public @Unmodifiable List<Label> getLabels() {
		return List.of(label);
	}

	@Override
	public boolean canStay() {
		return opcode != Opcodes.GOTO;
	}

	@Override
	public int getOpcode() {
		return opcode;
	}

	@Override
	public @NotNull Operation toOperation(MethodContext context) {
		return switch (opcode) {
			case IFEQ -> IfOperation.cmp(context, label, CompareType.EQUALS);
			case IFNE -> IfOperation.cmp(context, label, CompareType.NOT_EQUALS);
			case IFLT -> IfOperation.cmp(context, label, CompareType.LESS);
			case IFGE -> IfOperation.cmp(context, label, CompareType.GREATER_OR_EQUAL);
			case IFGT -> IfOperation.cmp(context, label, CompareType.GREATER);
			case IFLE -> IfOperation.cmp(context, label, CompareType.LESS_OR_EQUAL);

			case IF_ICMPEQ -> IfOperation.icmp(context, label, CompareType.EQUALS);
			case IF_ICMPNE -> IfOperation.icmp(context, label, CompareType.NOT_EQUALS);
			case IF_ICMPLT -> IfOperation.icmp(context, label, CompareType.LESS);
			case IF_ICMPGE -> IfOperation.icmp(context, label, CompareType.GREATER_OR_EQUAL);
			case IF_ICMPGT -> IfOperation.icmp(context, label, CompareType.GREATER);
			case IF_ICMPLE -> IfOperation.icmp(context, label, CompareType.LESS_OR_EQUAL);

			case IF_ACMPEQ -> IfOperation.acmp(context, label, CompareType.EQUALS);
			case IF_ACMPNE -> IfOperation.acmp(context, label, CompareType.NOT_EQUALS);

			case IFNULL    -> IfOperation.acmpNull(context, label, CompareType.EQUALS);
			case IFNONNULL -> IfOperation.acmpNull(context, label, CompareType.NOT_EQUALS);

			case GOTO -> new GotoOperation(label);

			default -> throw new UnknownOpcodeException(opcode);
		};
	}
}
