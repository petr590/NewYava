package x590.newyava.decompilation.variable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.exception.DecompilationException;
import x590.newyava.type.Types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Слот, который может содержать несколько переменных, находящихся в разных непересекающихся чанках.
 */
@ToString
@RequiredArgsConstructor
public class VariableSlot implements VariableSlotView {
	@Getter
	private final int id;

	private final List<VariableReference> refs = new ArrayList<>();

	private final @UnmodifiableView List<VariableReference> refsView = Collections.unmodifiableList(refs);

	public boolean isEmpty() {
		return refs.isEmpty();
	}

	@Override
	public @UnmodifiableView List<VariableReference> getVarRefs() {
		return refsView;
	}

	public void add(VariableReference ref) {
		if (get(ref.getStart()) != null || get(ref.getEnd() - 1) != null) {
			throw new DecompilationException(String.format(
					"Variable ref %s intersects other refs: %s",
					ref, refs
			));
		}

		refs.add(ref);
	}

	@Override
	public @Nullable VariableReference get(int index) {
		for (var ref : refs) {
			if (index >= ref.getStart() && index < ref.getEnd())
				return ref;
		}

		return null;
	}

	@Override
	public VariableReference getOrCreate(int start, int end) {
		assert start < end : String.format("start(%d) >= end(%d)", start, end);

		var ref = get(end - 1);

		if (ref != null) return ref;

		ref = new VariableReference(Types.ANY_TYPE, id, start, end);
		refs.add(ref);
		return ref;
	}
}
