package x590.newyava.context;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import x590.newyava.RemoveIfNotUsed;
import x590.newyava.decompilation.scope.MethodScope;
import x590.newyava.decompilation.scope.Scope;
import x590.newyava.exception.DecompilationException;

@Getter
public class MethodWriteContext extends ContextProxy {

	private final @Nullable MethodScope methodScope;

	private @Nullable Scope currentScope;

	public MethodWriteContext(Context context) {
		this(context, null);
	}

	public MethodWriteContext(Context context, @Nullable MethodScope methodScope) {
		super(context);
		this.methodScope = methodScope;
		this.currentScope = methodScope;
	}


	/** @return {@code true}, если в методе есть переменная и указанным именем */
	public boolean hasVarWithName(String name) {
		return methodScope != null && methodScope.hasVarByNameInDepth(name);
	}


	@RemoveIfNotUsed
	public void enterScope(Scope scope) {
		if (scope.getParent() != currentScope) {
			throw new DecompilationException(String.format(
					"scope.getParent() != currentScope: %s, %s",
					scope.getParent(), currentScope
			));
		}

		currentScope = scope;
	}

	@RemoveIfNotUsed
	public void exitScope(Scope scope) {
		if (scope != currentScope) {
			throw new DecompilationException(String.format(
					"scope != currentScope: %s, %s",
					scope, currentScope
			));
		}

		currentScope = scope.getParent();
	}
}
