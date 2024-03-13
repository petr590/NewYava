package x590.newyava.context;

import lombok.Getter;
import x590.newyava.decompilation.scope.MethodScope;
import x590.newyava.decompilation.scope.Scope;

/**
 * Предоставляет доступ к текущему {@link Scope}.
 * Используется при записи кода.
 */
public class WriteContext extends DelegatingContext {
	@Getter
	@Deprecated
	private Scope currentScope;

	public WriteContext(Context context, MethodScope methodScope) {
		super(context);
		this.currentScope = methodScope;
	}

	/**
	 * Входит в {@code scope}.
	 * @throws IllegalStateException при несовпадении {@link #currentScope} и {@code scope.getParent()}
	 */
	@Deprecated
	public void enterScope(Scope scope) {
		if (currentScope != scope.getParent()) {
			throw new IllegalStateException(String.format(
					"currentScope != scope.getParent(): currentScope = %s, scope.getParent() = %s",
					currentScope, scope.getParent()
			));
		}

		currentScope = scope;
	}

	/**
	 * Выходит из {@code scope}.
	 * @throws IllegalStateException при несовпадении {@link #currentScope} и {@code scope}
	 */
	@Deprecated
	public void exitScope(Scope scope) {
		if (currentScope != scope) {
			throw new IllegalStateException(String.format(
					"currentScope != scope: currentScope = %s, scope = %s",
					currentScope, scope
			));
		}

		currentScope = scope.getParent();
	}
}
