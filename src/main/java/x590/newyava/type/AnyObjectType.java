package x590.newyava.type;

import org.jetbrains.annotations.Nullable;
import x590.newyava.context.ClassContext;
import x590.newyava.io.DecompilationWriter;

import java.util.Collections;
import java.util.List;

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
	public void write(DecompilationWriter out, ClassContext context) {
		out.record(ClassType.OBJECT, context);
	}


	@Override
	public String toString() {
		return "AnyObjectType";
	}
}
