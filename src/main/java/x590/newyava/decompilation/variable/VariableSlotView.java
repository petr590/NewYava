package x590.newyava.decompilation.variable;

import org.jetbrains.annotations.Nullable;

public interface VariableSlotView {
	int getId();

	@Nullable VariableReference get(int start);

	VariableReference getOrCreate(int start, int end);
}
