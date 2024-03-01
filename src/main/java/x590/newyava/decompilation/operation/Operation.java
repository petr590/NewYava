package x590.newyava.decompilation.operation;

import x590.newyava.Importable;
import x590.newyava.context.ClassContext;
import x590.newyava.decompilation.scope.Scope;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

public interface Operation extends Importable {

	/** Возвращаемый тип операции, может быть void */
	Type getReturnType();

	@Override
	default void addImports(ClassContext context) {}

	default void updateReturnType(Type newType) {}

	default boolean isThisRef() {
		return false;
	}

	default boolean isDefaultConstructor() {
		return false;
	}

	/**
	 * @return {@code true}, если операция внешне является scope-ом.
	 * Это не гарантирует, что операция - экземпляр класса {@link Scope}
	 */
	default boolean isScopeLike() {
		return false;
	}

	default Priority getPriority() {
		return Priority.DEFAULT;
	}

	void write(DecompilationWriter out, ClassContext context);
}
