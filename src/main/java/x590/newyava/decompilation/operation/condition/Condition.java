package x590.newyava.decompilation.operation.condition;

import x590.newyava.decompilation.operation.Operation;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

public interface Condition extends Operation {

	/** @return противоположное условие. Не должен изменять и возвращать {@code this} */
	Condition opposite();

	@Override
	default Type getReturnType() {
		return PrimitiveType.BOOLEAN;
	}
}
