package x590.newyava.io;

import lombok.RequiredArgsConstructor;
import x590.newyava.exception.InvalidTypeException;

import java.io.EOFException;
import java.io.Reader;
import java.io.UncheckedIOException;

@RequiredArgsConstructor
public class SignatureReader extends Reader {

	private final String src;
	private int pos;

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

	public char next() {
		if (pos >= src.length()) {
			throw new UncheckedIOException(new EOFException());
		}

		return src.charAt(pos++);
	}

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

	public SignatureReader dec() {
		pos--;
		return this;
	}

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
