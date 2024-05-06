package x590.newyava.exception;

import org.intellij.lang.annotations.PrintFormat;
import org.jetbrains.annotations.Nullable;
import x590.newyava.descriptor.MethodDescriptor;

public class DecompilationException extends RuntimeException {

	public DecompilationException() {
		super();
	}

	public DecompilationException(String message) {
		super(message);
	}

	public DecompilationException(@PrintFormat String format, Object... args) {
		super(String.format(format, args));
	}

	public DecompilationException(Throwable cause) {
		super(cause);
	}

	public DecompilationException(String message, Throwable cause) {
		super(message, cause);
	}

	public DecompilationException(Throwable cause, @Nullable MethodDescriptor descriptor) {
		super(cause);
		this.descriptor = descriptor;
	}

	private @Nullable MethodDescriptor descriptor;

	public void setMethodDescriptor(MethodDescriptor descriptor) {
		if (this.descriptor != null) {
			throw new IllegalStateException("Method descriptor already has been set");
		}

		this.descriptor = descriptor;
	}

	@Override
	public String getMessage() {
		return descriptor == null ? super.getMessage() :
				"In method " + descriptor + ": " + super.getMessage();
	}
}
