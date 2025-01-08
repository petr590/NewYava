package x590.newyava.decompilation.operation.operator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.constant.IntConstant;
import x590.newyava.constant.LongConstant;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.*;
import x590.newyava.decompilation.operation.other.CastOperation;
import x590.newyava.decompilation.operation.other.LdcOperation;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.List;

@EqualsAndHashCode
public class BinaryOperator implements Operation {
	@Getter
	private final Operation operand1, operand2;

	@Getter
	private final OperatorType operatorType;

	@EqualsAndHashCode.Exclude
	private final Type requiredType1, requiredType2, returnType;

	public BinaryOperator(MethodContext context, OperatorType operatorType, Type requiredType) {
		this(context, operatorType, requiredType, requiredType);
	}

	public BinaryOperator(MethodContext context, OperatorType operatorType, Type requiredType1, Type requiredType2) {
		var operand2 = context.popAs(requiredType2);
		var operand1 = context.popAs(requiredType1);

		if (operatorType == OperatorType.XOR && operand2 instanceof LdcOperation ldc &&
			(ldc.getValue().equals(IntConstant.MINUS_ONE) ||
			ldc.getValue().equals(LongConstant.MINUS_ONE))) {

			operatorType = OperatorType.NOT;

		} else if (operatorType.isShift() &&
				operand2 instanceof CastOperation cast1 &&
				cast1.getReturnType() == PrimitiveType.INT) {

			operand2 = cast1.getOperand();
			requiredType2 = cast1.getRequiredType();

			if (operand2 instanceof CastOperation cast2 &&
				cast2.getReturnType() == PrimitiveType.LONG) {

				operand2 = cast2.getOperand();
				requiredType2 = cast2.getRequiredType();
			}
		}

		this.operand1 = operand1;
		this.operand2 = operand2;
		this.operatorType = operatorType;
		this.requiredType1 = requiredType1;
		this.requiredType2 = requiredType2;
		this.returnType = requiredType1;
	}


	@Override
	public Type getReturnType() {
		return returnType;
	}

	@Override
	public void inferType(Type ignored) {
		if (operand1.getReturnType() == PrimitiveType.BOOLEAN ||
			operand2.getReturnType() == PrimitiveType.BOOLEAN) {

			operand1.inferType(PrimitiveType.BOOLEAN);
			operand2.inferType(PrimitiveType.BOOLEAN);
		} else {
			operand1.inferType(requiredType1);
			operand2.inferType(requiredType2);
		}

		if (requiredType1 == requiredType2) {
			if (operand2.getImplicitType().equals(requiredType2)) {
				operand1.allowImplicitCast();
			} else {
				operand2.allowImplicitCast();
			}
		}
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(operand1, operand2);
	}

	@Override
	public Priority getPriority() {
		return operatorType.getPriority();
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(operand1).addImportsFor(operand2);
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		if (operatorType.isUnary()) {
			out.record(operatorType.getValue()).record(operand1, context, getPriority(), Associativity.RIGHT);

		} else {
			TriConsumer<Operation, DecompilationWriter, MethodWriteContext> writer =
					operatorType.isHexBitwise() ? Operation::writeHex : Operation::write;

			out .record(operand1, context, getPriority(), Associativity.LEFT, writer)
				.wrapSpaces(operatorType.getValue())
				.record(operand2, context, getPriority(), Associativity.RIGHT, writer);
		}
	}

	@Override
	public String toString() {
		return String.format("BinaryOperation(%s %s %s)", operand1, operatorType.getValue(), operand2);
	}
}
