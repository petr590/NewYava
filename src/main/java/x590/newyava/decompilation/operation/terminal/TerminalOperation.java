package x590.newyava.decompilation.operation.terminal;

import x590.newyava.decompilation.operation.Operation;

public interface TerminalOperation extends Operation {
	@Override
	default boolean isTerminal() {
		return true;
	}
}
