package x590.newyava.example.code;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class EscapeExample {
	@Test
	public void run() {
		Main.run(this);
	}

	public void chars() {
		char[] chars = { 'a', '\0', '\377', '\n', '\'', '"', '\uFFFF' };
	}

	public void string() {
		String str = "abc\0\1\2\3\uFFFF\n\t\"\uD800\uDC00\u0000111";
	}
}
