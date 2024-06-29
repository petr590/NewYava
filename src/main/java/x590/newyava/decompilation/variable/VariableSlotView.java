package x590.newyava.decompilation.variable;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public interface VariableSlotView {
	int getId();

	@UnmodifiableView List<VariableReference> getVarRefs();

	/** @return ссылку на переменную, найденную на индексе, или {@code null}, если ничего не найдено. */
	@Nullable VariableReference get(int index);

	/** @return ссылку на переменную, найденную на данных индексах, или созданную. */
	VariableReference getOrCreate(int start, int end);
}
