package x590.newyava.decompilation.operation.condition;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.constant.IntConstant;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.*;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;
import x590.newyava.type.Types;

import java.util.List;

@RequiredArgsConstructor
public class CompareCondition implements Condition {

	private final CompareType compareType;

	// Именно такой порядок, так как сначала со стека снимается второй операнд, затем первый
	private final Operation operand2, operand1;

	public CompareCondition(MethodContext context, CompareType compareType) {
		this.compareType = compareType;

		var operand = context.popAs(PrimitiveType.INTEGRAL);

		if (operand instanceof CmpOperation cmp) {
			this.operand1 = cmp.getOperand1();
			this.operand2 = cmp.getOperand2();
		} else {
			this.operand1 = operand;
			this.operand2 = new LdcOperation(IntConstant.ZERO);
		}
	}

	public static CompareCondition icmp(MethodContext context, CompareType compareType) {
		return new CompareCondition(compareType,
				context.popAs(PrimitiveType.INTEGRAL),
				context.popAs(PrimitiveType.INTEGRAL));
	}

	public static CompareCondition acmp(MethodContext context, CompareType compareType) {
		return new CompareCondition(compareType,
				context.popAs(Types.ANY_OBJECT_TYPE),
				context.popAs(Types.ANY_OBJECT_TYPE));
	}

	public static CompareCondition acmpNull(MethodContext context, CompareType compareType) {
		return new CompareCondition(compareType,
				ConstNullOperation.INSTANCE,
				context.popAs(Types.ANY_OBJECT_TYPE));
	}


	private @Nullable CompareCondition opposite;

	@Override
	public Condition opposite() {
		if (opposite != null)
			return opposite;

		var opposite = new CompareCondition(compareType.getOpposite(), operand2, operand1);
		opposite.opposite = this;

		return this.opposite = opposite;
	}

	@Override
	public void inferType(Type ignored) {
		operand1.inferType(operand2.getReturnType());
		operand2.inferType(operand1.getReturnType());
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(operand1, operand2);
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(operand1).addImportsFor(operand2);
	}

	@Override
	public Priority getPriority() {
		return compareType.getPriority();
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		if (operand1.getReturnType() == PrimitiveType.BOOLEAN) {

			if ((compareType == CompareType.EQUALS || compareType == CompareType.NOT_EQUALS) &&
				operand2 instanceof LdcOperation ldc && ldc.getValue() == IntConstant.ZERO) {

				if (compareType == CompareType.EQUALS)
					out.record('!');

				out.record(operand1, context, compareType.getPriority(), Associativity.LEFT);
				return;
			}
		}

		out .record(operand1, context, compareType.getPriority(), Associativity.LEFT)
			.recordSp().recordSp(compareType.getOperator())
			.record(operand2, context, compareType.getPriority(), Associativity.RIGHT);
	}

	@Override
	public String toString() {
		return String.format("CompareOperation %08x(%s %s %s)",
				hashCode(), operand1, compareType.getOperator(), operand2);
	}
}
