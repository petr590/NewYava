package x590.newyava.exception;

import org.intellij.lang.annotations.PrintFormat;
import org.jetbrains.annotations.Nullable;
import x590.newyava.descriptor.MethodDescriptor;

import java.util.Objects;

/**
 * Исключение при декомпиляции кода
 */
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

	/**
	 * Инициализирует поле {@link #descriptor}.
	 * @throws IllegalStateException если поле уже инициализировано.
	 * @throws NullPointerException если параметр равен {@code null}.
	 */
	public void setMethodDescriptor(MethodDescriptor descriptor) {
		if (this.descriptor != null) {
			throw new IllegalStateException("Method descriptor already has been set");
		}

		this.descriptor = Objects.requireNonNull(descriptor);
	}

	/** Добавляет к исходному сообщению об ошибке {@link #descriptor}, если он инициализирован. */
	@Override
	public String getMessage() {
		if (descriptor == null) {
			return super.getMessage();
		}

		var message = super.getMessage();

		return message != null ?
				"In method " + descriptor + ": " + message :
				"In method " + descriptor;
	}
}
