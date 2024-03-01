package x590.newyava.decompilation.instruction;

import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;

/**
 * Инструкция в байткоде Java
 */
public interface Instruction {

	int getOpcode();

	Operation toOperation(MethodContext context);

	/**
	 * @return {@code true} если поток выполнения может
	 * продолжиться (а может и не продолжиться) после этой инструкции.
	 * После некоторых инструкций, таких как {@code goto} и {@code return}, поток прерывается
	 */
	default boolean canStay() {
		return true;
	}
}
