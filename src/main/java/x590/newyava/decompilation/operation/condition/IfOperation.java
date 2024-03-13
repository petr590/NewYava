package x590.newyava.decompilation.operation.condition;

import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Label;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.WriteContext;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.io.DecompilationWriter;

@RequiredArgsConstructor
public class IfOperation extends JumpOperation {

	private final Label label;
	private final CompareCondition condition;

	public static IfOperation cmp(MethodContext context, Label label, CompareType compareType) {
		return new IfOperation(label, new CompareCondition(context, compareType));
	}

	public static IfOperation icmp(MethodContext context, Label label, CompareType compareType) {
		return new IfOperation(label, CompareCondition.icmp(context, compareType));
	}

	public static IfOperation acmp(MethodContext context, Label label, CompareType compareType) {
		return new IfOperation(label, CompareCondition.acmp(context, compareType));
	}

	public static IfOperation acmpNull(MethodContext context, Label label, CompareType compareType) {
		return new IfOperation(label, CompareCondition.acmpNull(context, compareType));
	}

	@Override
	public Condition getCondition() {
		return condition;
	}

	@Override
	public Label getLabel() {
		return label;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(condition);
	}

	@Override
	public void write(DecompilationWriter out, WriteContext context) {
		out.record("if (").record(condition, context, Priority.ZERO).record(')')
				.incIndent().ln().indent();

		getRole().write(out, context);

		out.decIndent();
	}
}
