package x590.newyava.decompilation.operation.condition;

import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Label;
import x590.newyava.context.Context;
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
	public void write(DecompilationWriter out, Context context) {
		getRole().write(out, context);
	}
}
