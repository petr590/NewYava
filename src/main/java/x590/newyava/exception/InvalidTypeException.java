package x590.newyava.exception;

import x590.newyava.io.SignatureReader;

public class InvalidTypeException extends DecompilationException {

	public InvalidTypeException(String message) {
		super(message);
	}

	public InvalidTypeException(SignatureReader reader) {
		super("At pos " + reader.getPos() + ": " + reader.nextAll() + "\nWhole string: " + reader.getSrc());
	}
}
