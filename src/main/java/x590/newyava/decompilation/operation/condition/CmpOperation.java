package x590.newyava.decompilation.operation.condition;

import lombok.Getter;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.other.SpecialOperation;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.List;

@Getter
public class CmpOperation implements SpecialOperation {

	private final Operation operand1, operand2;
	private final Type requiredType;

	public CmpOperation(MethodContext context, Type requiredType) {
		this.operand2 = context.popAs(requiredType);
		this.operand1 = context.popAs(requiredType);
		this.requiredType = requiredType;
	}

	@Override
	public Type getReturnType() {
		return PrimitiveType.INT;
	}

	@Override
	public void inferType(Type ignored) {
		operand1.inferType(requiredType);
		operand2.inferType(requiredType);
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(operand1, operand2);
	}

	@Override
	public String toString() {
		return String.format("CmpOperation %08x(%s %s %s)", hashCode(), operand1, operand2, requiredType);
	}
}
