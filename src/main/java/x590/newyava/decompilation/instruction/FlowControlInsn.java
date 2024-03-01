package x590.newyava.decompilation.instruction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Label;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;

import java.util.List;

public interface FlowControlInsn extends Instruction {
	@Unmodifiable List<Label> getLabels();

	@Override
	boolean canStay();

	@Override
	@NotNull Operation toOperation(MethodContext context);
}
