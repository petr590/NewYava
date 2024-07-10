package x590.newyava.decompilation.scope;

import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.decompilation.code.Chunk;
import x590.newyava.decompilation.operation.Operation;

import java.util.Deque;
import java.util.List;

public abstract sealed class IfElseScope extends Scope permits IfScope, ElseScope {

	/** Операция, оставшаяся на стеке в конце scope */
	@Getter(AccessLevel.PACKAGE)
	protected Deque<Operation> pushedOperations;

	public IfElseScope(@Unmodifiable List<Chunk> chunks, int startIndexOffset) {
		super(chunks, startIndexOffset);
	}

	/** Инициализирует поле {@link #pushedOperations} */
	@Override
	@MustBeInvokedByOverriders
	protected void onEnd() {
		pushedOperations = getEndChunk().getPushedOperations();
	}

	@Override
	protected boolean canOmitBrackets() {
		return true;
	}
}
