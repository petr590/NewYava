package x590.newyava.type;

import org.jetbrains.annotations.Nullable;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;

import java.util.Collections;
import java.util.List;

/**
 * Любой ссылочный тип. Этот тип имеет, например, {@code null}
 * ({@link x590.newyava.decompilation.operation.ConstNullOperation}).
 */
public enum AnyObjectType implements ReferenceType {
	INSTANCE;

	@Override
	public @Nullable ReferenceType getSuperType() {
		return null;
	}

	@Override
	public List<? extends ReferenceType> getInterfaces() {
		return Collections.emptyList();
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.record(ClassType.OBJECT, context);
	}


	@Override
	public String toString() {
		return "<any-object-type>";
	}
}
