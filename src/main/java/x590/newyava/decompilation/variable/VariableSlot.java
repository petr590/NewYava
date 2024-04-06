package x590.newyava.decompilation.variable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;
import x590.newyava.exception.DecompilationException;
import x590.newyava.type.Types;

import java.util.ArrayList;
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

	public boolean isEmpty() {
		return refs.isEmpty();
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
	public @Nullable VariableReference get(int start) {
		for (var ref : refs) {
			if (start >= ref.getStart() && start < ref.getEnd())
				return ref;
		}

		return null;
	}

	@Override
	public VariableReference getOrCreate(int start, int end) {
		var ref = get(start);

		if (ref != null) return ref;

		ref = new VariableReference(Types.ANY_TYPE, start, end);
		refs.add(ref);
		return ref;
	}
}
