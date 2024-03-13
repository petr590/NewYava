package x590.newyava.decompilation.instruction;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.condition.SwitchOperation;
import x590.newyava.type.PrimitiveType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SwitchInsn implements FlowControlInsn {
	private final Int2ObjectMap<Label> table;

	private final Label defaultLabel;

	private final @Unmodifiable List<Label> labels;

	private @Unmodifiable List<Label> createList(Label[] labels, Label defaultLabel) {
		List<Label> labelsList = new ArrayList<>(labels.length + 1);
		Collections.addAll(labelsList, labels);
		labelsList.add(defaultLabel);

		return Collections.unmodifiableList(labelsList);
	}

	public SwitchInsn(int min, int max, Label defaultLabel, Label[] labels) {
		if (labels.length != max - min + 1) {
			throw new IllegalArgumentException(String.format(
					"labels.length != max - min + 1; labels.length = %d, max = %d, min = %d",
					labels.length, max, min));
		}

		this.table = new Int2ObjectArrayMap<>(labels.length);

		for (int i = 0, key = min; key <= max; i++, key++) {
			table.put(key, labels[i]);
		}

		this.defaultLabel = defaultLabel;

		this.labels = createList(labels, defaultLabel);
	}

	public SwitchInsn(Label defaultLabel, int[] keys, Label[] labels) {
		if (keys.length != labels.length) {
			throw new IllegalArgumentException(String.format(
					"keys.length != labels.length; keys.length = %d, labels.length = %d",
					keys.length, labels.length));
		}

		this.table = new Int2ObjectArrayMap<>(keys, labels);
		this.defaultLabel = defaultLabel;
		this.labels = createList(labels, defaultLabel);
	}

	@Override
	public @Unmodifiable List<Label> getLabels() {
		return labels;
	}

	@Override
	public boolean canStay() {
		return false;
	}

	@Override
	public int getOpcode() {
		return Opcodes.TABLESWITCH;
	}


	@Override
	public @NotNull Operation toOperation(MethodContext context) {
		return new SwitchOperation(context.popAs(PrimitiveType.INT), table, defaultLabel);
	}
}
