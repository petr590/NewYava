package x590.newyava.decompilation.operation.condition;

import lombok.Getter;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.SpecialOperation;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

@Getter
public class CmpOperation implements SpecialOperation {

	private final Operation operand1, operand2;

	public CmpOperation(MethodContext context, Type requiredType) {
		this.operand2 = context.popAs(requiredType);
		this.operand1 = context.popAs(requiredType);
	}

	@Override
	public Type getReturnType() {
		return PrimitiveType.INT;
	}
}
