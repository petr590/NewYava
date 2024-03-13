package x590.newyava.decompilation.operation.condition;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Label;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.SpecialOperation;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

public record SwitchOperation(Operation value, Int2ObjectMap<Label> table, Label defaultLabel)
		implements SpecialOperation {
	@Override
	public Type getReturnType() {
		return PrimitiveType.VOID;
	}
}
