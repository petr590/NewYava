package x590.newyava.decompilation.scope;

import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.decompilation.Chunk;
import x590.newyava.decompilation.operation.ProxyOperation;

import java.util.List;

public abstract sealed class IfElseScope extends Scope permits IfScope, ElseScope {

	/** Операция, оставшаяся на стеке в конце scope */
	@Getter(AccessLevel.PACKAGE)
	protected @Nullable ProxyOperation leftOperation;

	public IfElseScope(@Unmodifiable List<Chunk> chunks, int startIndexOffset) {
		super(chunks, startIndexOffset);
	}

	/** Инициализирует поле {@link #leftOperation} */
	@Override
	@MustBeInvokedByOverriders
	protected void onEnd() {
		var leftOperations = getEndChunk().getLeftOperations();
		if (!leftOperations.isEmpty()) {
			leftOperation = leftOperations.get(0);
		}
	}

	@Override
	protected boolean canOmitBrackets() {
		return true;
	}
}
