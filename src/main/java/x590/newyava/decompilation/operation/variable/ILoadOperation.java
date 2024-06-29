package x590.newyava.decompilation.operation.variable;

import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.variable.VariableReference;

/**
 * Операция загрузки переменной.
 * Любая операция может реализовать этот интерфейс.
 */
public interface ILoadOperation extends Operation {
	default int getSlotId() {
		return getVarRef().getSlotId();
	}

	VariableReference getVarRef();

	@Override
	boolean isThisRef();
}
