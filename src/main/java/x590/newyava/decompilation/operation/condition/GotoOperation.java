package x590.newyava.decompilation.operation.condition;

import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Label;
import x590.newyava.context.ClassContext;
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
	public void write(DecompilationWriter out, ClassContext context) {
		switch (getRole()) {
			case BREAK -> out.record("break");
			case CONTINUE -> out.record("continue");
			default -> super.write(out, context);
		}
	}
}
