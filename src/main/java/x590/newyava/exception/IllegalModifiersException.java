package x590.newyava.exception;

import x590.newyava.modifiers.EntryType;

public class IllegalModifiersException extends DecompilationException {

	public IllegalModifiersException(int modifiers, EntryType entryType) {
		super(entryType.modifiersToString(modifiers));
	}

	public IllegalModifiersException(String message, int modifiers, EntryType entryType) {
		super(message + entryType.modifiersToString(modifiers));
	}
}
