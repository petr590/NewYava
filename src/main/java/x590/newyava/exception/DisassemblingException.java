package x590.newyava.exception;

/**
 * Исключение при дизассемблировании байткода
 */
public class DisassemblingException extends RuntimeException {

	public DisassemblingException(String message) {
		super(message);
	}

	public DisassemblingException(Throwable cause) {
		super(cause);
	}

	public DisassemblingException(String message, Throwable cause) {
		super(message, cause);
	}
}
