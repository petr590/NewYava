package x590.newyava.decompilation.operation.condition;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.constant.IntConstant;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.*;
import x590.newyava.decompilation.operation.other.ConstNullOperation;
import x590.newyava.decompilation.operation.other.LdcOperation;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;
import x590.newyava.type.Types;

import java.util.List;

@EqualsAndHashCode
@RequiredArgsConstructor
public class CompareCondition implements Condition {

	@Getter
	private final CompareType compareType;

	private final Type requiredType;

	// Именно такой порядок, так как сначала со стека снимается второй операнд, затем первый
	@Getter
	private final Operation operand2, operand1;

	public CompareCondition(MethodContext context, CompareType compareType) {
		this.compareType = compareType;

		var operand = context.popAs(PrimitiveType.INTEGRAL);

		if (operand instanceof CmpOperation cmp) {
			this.operand1 = cmp.getOperand1();
			this.operand2 = cmp.getOperand2();
			this.requiredType = cmp.getRequiredType();
		} else {
			this.operand1 = operand;
			this.operand2 = new LdcOperation(IntConstant.ZERO);
			this.requiredType = PrimitiveType.INTEGRAL;
		}
	}

	public static CompareCondition icmp(MethodContext context, CompareType compareType) {
		return new CompareCondition(
				compareType, PrimitiveType.INTEGRAL,
				context.popAs(PrimitiveType.INTEGRAL),
				context.popAs(PrimitiveType.INTEGRAL)
		);
	}

	public static CompareCondition acmp(MethodContext context, CompareType compareType) {
		return new CompareCondition(
				compareType, Types.ANY_OBJECT_TYPE,
				context.popAs(Types.ANY_OBJECT_TYPE),
				context.popAs(Types.ANY_OBJECT_TYPE)
		);
	}

	public static CompareCondition acmpNull(MethodContext context, CompareType compareType) {
		return new CompareCondition(
				compareType, Types.ANY_OBJECT_TYPE,
				ConstNullOperation.INSTANCE,
				context.popAs(Types.ANY_OBJECT_TYPE)
		);
	}


	@EqualsAndHashCode.Exclude
	private @Nullable CompareCondition opposite;

	@Override
	public Condition opposite() {
		if (opposite != null)
			return opposite;

		var opposite = new CompareCondition(compareType.getOpposite(), requiredType, operand2, operand1);
		opposite.opposite = this;
		return this.opposite = opposite;
	}

	@Override
	public void inferType(Type ignored) {
		Type reqType = (operand1.getReturnType() == PrimitiveType.BOOLEAN ||
				        operand2.getReturnType() == PrimitiveType.BOOLEAN) ?
						PrimitiveType.BOOLEAN : requiredType;

		operand1.inferType(reqType);
		operand2.inferType(reqType);

		if (operand2.getImplicitType().equals(reqType)) {
			operand1.allowImplicitCast();
		} else {
			operand2.allowImplicitCast();
		}
	}

	/** @return {@code true}, если операция является простой проверкой {@code boolean} на
	 * {@code true} или {@code false}, т.е. имеет вид {@code x} или {@code !x} */
	public boolean isPlainBoolean() {
		return  operand1.getReturnType() == PrimitiveType.BOOLEAN &&
				(compareType == CompareType.EQUALS || compareType == CompareType.NOT_EQUALS) &&
				(operand2 == ConstCondition.FALSE ||
						operand2 instanceof LdcOperation ldc && ldc.getValue().equals(IntConstant.ZERO));
	}

	public boolean isNot() {
		return isPlainBoolean() && compareType == CompareType.EQUALS;
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
		return isPlainBoolean() ?
				compareType == CompareType.EQUALS ? Priority.UNARY : operand1.getPriority() :
				compareType.getPriority();
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		if (isPlainBoolean()) {
			if (compareType == CompareType.EQUALS) {
				out.record('!').record(operand1, context, Priority.UNARY, Associativity.LEFT);
			} else {
				out.record(operand1, context, compareType.getPriority(), Associativity.LEFT);
			}

		} else {
			out .record(operand1, context, compareType.getPriority(), Associativity.LEFT)
				.wrapSpaces(compareType.getOperator())
				.record(operand2, context, compareType.getPriority(), Associativity.RIGHT);
		}
	}

	@Override
	public String toString() {
		return String.format("CompareCondition(%s %s %s)", operand1, compareType.getOperator(), operand2);
	}
}
