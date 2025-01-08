package x590.newyava.decompilation.operation.condition;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.UnmodifiableView;
import org.objectweb.asm.Label;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.List;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
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
	public void inferType(Type ignored) {
		condition.inferType(PrimitiveType.BOOLEAN);
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(condition);
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(condition);
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		out.record("if (").record(condition, context, Priority.ZERO).record(')')
				.incIndent().ln().indent();

		getRole().write(out, context);

		out.decIndent();
	}

	@Override
	public String toString() {
		return String.format("IfOperation(%s %s)", condition, label);
	}
}
