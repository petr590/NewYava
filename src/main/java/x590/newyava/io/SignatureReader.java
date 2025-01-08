package x590.newyava.io;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import x590.newyava.exception.InvalidTypeException;

import java.io.EOFException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.function.Function;

/**
 * Позволяет читать строку посимвольно, с возможностью возврата.
 */
@Getter
@RequiredArgsConstructor
public class SignatureReader extends Reader {

	private final String src;

	private int pos;

	/** Парсит строку с использованием переданной функции.
	 * Проверяет конец строки после парсинга.
	 * @return результат, который вернул {@code parser} */
	public static <T> T parse(String src, Function<? super SignatureReader, T> parser) {
		var reader = new SignatureReader(src);
		T result = parser.apply(reader);
		reader.checkEndForType();
		return result;
	}

	@Override
	public int read(char[] buffer, int off, int len) {
		int pos = this.pos;
		int length = src.length();

		if (pos >= length) return -1;

		for (int i = off, end = off + len; i < end && pos < length; i++, pos++) {
			buffer[i] = src.charAt(pos);
		}

		int res = pos - this.pos;
		this.pos = pos;
		return res;
	}

	@Override
	public int read() {
		return pos >= src.length() ? -1 : src.charAt(pos++);
	}

	public boolean isEnd() {
		return pos >= src.length();
	}

	/**
	 * @return следующий символ.
	 * @throws UncheckedIOException если строка закончилась.
	 */
	public char next() {
		if (pos >= src.length()) {
			throw new UncheckedIOException(new EOFException());
		}

		return src.charAt(pos++);
	}

	/**
	 * Если следующий символ равен указанному, увеличивает позицию на 1 и возвращает {@code true},
	 * иначе возвращает {@code false}.
	 * @throws UncheckedIOException если строка закончилась
	 */
	public boolean eat(char ch) {
		if (pos >= src.length()) {
			throw new UncheckedIOException(new EOFException());
		}

		if (src.charAt(pos) == ch) {
			pos++;
			return true;
		}

		return false;
	}

	/**
	 * Уменьшает позицию на 1.
	 * @throws IllegalStateException если позиция меньше 0.
	 */
	public SignatureReader dec() {
		return dec(1);
	}

	/**
	 * Уменьшает позицию на {@code offset}.
	 * @throws IllegalStateException если позиция меньше 0.
	 */
	public SignatureReader dec(int offset) {
		pos -= offset;

		if (pos < 0) {
			throw new IllegalStateException("Negative position");
		}

		return this;
	}

	/**
	 * @return всю непрочитанную часть строки. Если строка закончилась, возвращает пустую строку.
	 */
	public String nextAll() {
		int pos = this.pos;
		if (pos >= src.length()) return "";

		this.pos = src.length();
		return src.substring(pos);
	}

	@Override
	public void close() {
		pos = src.length();
	}

	public void checkEndForType() {
		if (pos < src.length()) {
			throw new InvalidTypeException(src);
		}
	}
}
