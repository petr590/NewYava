package x590.newyava.decompilation.operation.condition;

import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Label;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.io.DecompilationWriter;

@RequiredArgsConstructor
public class GotoOperation extends JumpOperation {

	private final Label label;

	@Override
	public Condition getCondition() {
		return ConstCondition.TRUE;
	}

	@Override
	public Label getLabel() {
		return label;
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		getRole().write(out, context);
	}

	@Override
	public String toString() {
		return String.format("GotoOperation %08x(%s)", hashCode(), label);
	}
}
