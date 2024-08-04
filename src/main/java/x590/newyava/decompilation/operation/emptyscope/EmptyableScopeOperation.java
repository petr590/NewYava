package x590.newyava.decompilation.operation.emptyscope;

import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.scope.Scope;

/**
 * Представляет scope или операцию пустого scope,
 * нужен для более удобного контроля типов.
 */
public sealed interface EmptyableScopeOperation extends Operation permits Scope, EmptyScopeOperation {
	@Override
	default boolean isScopeLike() {
		return true;
	}
}
