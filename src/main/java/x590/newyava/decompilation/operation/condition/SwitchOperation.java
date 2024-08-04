package x590.newyava.decompilation.operation.condition;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.UnmodifiableView;
import org.objectweb.asm.Label;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.other.SpecialOperation;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.List;

public record SwitchOperation(Operation value, Int2ObjectMap<Label> table, Label defaultLabel)
		implements SpecialOperation {
	@Override
	public Type getReturnType() {
		return PrimitiveType.VOID;
	}

	@Override
	public void inferType(Type ignored) {
		value.inferType(PrimitiveType.INT);
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(value);
	}

	@Override
	public String toString() {
		return String.format("SwitchOperation %08x(table: %s, default: %s)", hashCode(), table, defaultLabel);
	}
}
