package x590.newyava.exception;

public class DecompilationException extends RuntimeException {

	public DecompilationException() {
		super();
	}

	public DecompilationException(String message) {
		super(message);
	}

	public DecompilationException(Throwable cause) {
		super(cause);
	}

	public DecompilationException(String message, Throwable cause) {
		super(message, cause);
	}
}
