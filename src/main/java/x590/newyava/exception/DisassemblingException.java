package x590.newyava.exception;

public class DisassemblingException extends RuntimeException {

	public DisassemblingException(String message) {
		super(message);
	}

	public DisassemblingException(String message, Throwable cause) {
		super(message, cause);
	}

	public DisassemblingException(Throwable cause) {
		super(cause);
	}

	protected DisassemblingException(String message, Throwable cause,
	                                 boolean enableSuppression, boolean writableStackTrace) {

		super(message, cause, enableSuppression, writableStackTrace);
	}
}
