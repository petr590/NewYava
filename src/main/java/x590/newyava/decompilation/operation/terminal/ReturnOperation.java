package x590.newyava.decompilation.operation.terminal;

public interface ReturnOperation extends TerminalOperation {
	@Override
	default boolean isReturn() {
		return true;
	}
}
