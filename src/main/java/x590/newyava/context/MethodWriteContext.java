package x590.newyava.context;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import x590.newyava.decompilation.scope.MethodScope;

public class MethodWriteContext extends ContextProxy {

	@Getter
	private final @Nullable MethodScope methodScope;

	public MethodWriteContext(Context context) {
		this(context, null);
	}

	public MethodWriteContext(Context context, @Nullable MethodScope methodScope) {
		super(context);
		this.methodScope = methodScope;
	}

	public boolean isConstructor() {
		return methodScope != null && methodScope.getMethodContext().getDescriptor().isConstructor();
	}


	/** @return {@code true}, если в методе есть переменная с указанным именем */
	public boolean hasVarWithName(String name) {
		return methodScope != null && methodScope.hasVarByNameInDepth(name);
	}
}
