package x590.newyava.decompilation.instruction;

import org.jetbrains.annotations.Nullable;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;

/**
 * Инструкция в байткоде Java
 */
public interface Instruction {

	// Remove if not used
	int getOpcode();

	/** Преобразует инструкцию в операцию */
	@Nullable Operation toOperation(MethodContext context);

	/** Дополнительный метод, который преобразует текущую и следующую инструкции в одну операцию.
	 * Если их нельзя объединить, то возвращает {@code null}. */
	default @Nullable Operation toOperation(MethodContext context, Instruction next) {
		return null;
	}

	/**
	 * @return {@code true} если поток выполнения может
	 * продолжиться (а может и не продолжиться) после этой инструкции.
	 * После некоторых инструкций, таких как {@code goto} и {@code return}, поток прерывается
	 */
	default boolean canStay() {
		return true;
	}
}
