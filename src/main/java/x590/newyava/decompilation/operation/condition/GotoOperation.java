package x590.newyava.decompilation.operation.condition;

import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Label;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.scope.Scope;
import x590.newyava.io.DecompilationWriter;

import java.util.Deque;

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
	public void initYield(Scope switchScope, Deque<Operation> pushedOperations) {
		if (!pushedOperations.isEmpty() &&
			getRole().isBreakOf(switchScope)) {

			changeRole(Role.yieldScope(switchScope, pushedOperations.pop()));
		}
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
