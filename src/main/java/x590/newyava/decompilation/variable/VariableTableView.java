package x590.newyava.decompilation.variable;

import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public interface VariableTableView {
	VariableSlotView get(int slotId);

	@UnmodifiableView List<? extends VariableSlotView> listView();
}
