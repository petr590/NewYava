package x590.newyava.decompilation.variable;

import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Таблица, которая содержит список {@link VariableSlot}.
 */
public class VariableTable {

	private final List<VariableSlot> slots = new ArrayList<>();

	public void extendTo(int size) {
		var slots = this.slots;

		while (slots.size() <= size) {
			slots.add(new VariableSlot(slots.size()));
		}
	}

	public void add(int slotId, VariableReference ref) {
		extendTo(slotId);
		slots.get(slotId).add(ref);
	}

	public VariableSlot get(int slotId) {
		return slots.get(slotId);
	}


	private final @UnmodifiableView List<VariableSlot> unmodifiableSlots = Collections.unmodifiableList(slots);

	public @UnmodifiableView List<VariableSlot> listView() {
		return unmodifiableSlots;
	}
}
