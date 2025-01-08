package x590.newyava.type;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;

import java.util.Collections;
import java.util.List;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WildcardType implements ReferenceType {
	public static WildcardType extendsFrom(ReferenceType type) {
		return new WildcardType("extends", type);
	}

	public static WildcardType superOf(ReferenceType type) {
		return new WildcardType("super", type);
	}


	private final String relation;

	private final ReferenceType bound;

	@Override
	public @Nullable ReferenceType getSuperType() {
		return relation.equals("extends") ? bound : null;
	}

	@Override
	public @Unmodifiable List<? extends ReferenceType> getInterfaces() {
		return Collections.emptyList();
	}

	@Override
	public String getVarName() {
		return bound.getVarName();
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.record("? ").record(relation).space().record(bound, context);
	}

	@Override
	public String toString() {
		return "? " + relation + " " + bound;
	}
}
