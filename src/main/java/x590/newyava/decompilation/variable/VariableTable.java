package x590.newyava.decompilation.variable;

import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Таблица, которая содержит список {@link VariableSlot}.
 */
public class VariableTable implements VariableTableView {
	private final List<VariableSlot> slots = new ArrayList<>();

	public void extendTo(int size) {
		for (var slots = this.slots; slots.size() <= size; ) {
			slots.add(new VariableSlot(slots.size()));
		}
	}

	public void add(int slotId, VariableReference ref) {
		extendTo(slotId);
		slots.get(slotId).add(ref);
	}

	@Override
	public VariableSlot get(int slotId) {
		return slots.get(slotId);
	}


	private final @UnmodifiableView List<VariableSlot> unmodifiableSlots = Collections.unmodifiableList(slots);

	@Override
	public @UnmodifiableView List<? extends VariableSlotView> listView() {
		return unmodifiableSlots;
	}
}
