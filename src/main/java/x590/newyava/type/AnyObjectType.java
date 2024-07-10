package x590.newyava.type;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;

import java.util.Collections;
import java.util.List;

/**
 * @deprecated используйте {@link Types#ANY_OBJECT_TYPE}.
 */
@Deprecated(since = "0.8", forRemoval = true)
public enum AnyObjectType implements ReferenceType {
	INSTANCE;

	@Override
	public @Nullable ReferenceType getSuperType() {
		return null;
	}

	@Override
	public @Unmodifiable List<? extends ReferenceType> getInterfaces() {
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
