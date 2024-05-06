package x590.newyava.decompilation.operation.invokedynamic;

import lombok.RequiredArgsConstructor;
import x590.newyava.decompilation.operation.SpecialOperation;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

@RequiredArgsConstructor
public enum RecordInvokedynamicOperation implements SpecialOperation {
	HASH_CODE(PrimitiveType.INT),
	EQUALS(PrimitiveType.BOOLEAN),
	TO_STRING(ClassType.STRING);

	private final Type returnType;

	@Override
	public Type getReturnType() {
		return returnType;
	}


	@Override
	public String toString() {
		return "RecordInvokedynamicOperation." + name();
	}
}
