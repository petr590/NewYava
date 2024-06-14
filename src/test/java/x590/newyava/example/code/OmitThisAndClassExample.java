package x590.newyava.example.code;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class OmitThisAndClassExample {
	@Test
	public void run() {
		Main.run(this);
	}

	private int x;
	private static int y;

	private void f(int x) {
		this.x += x;
	}

	private static void g(int y) {
		OmitThisAndClassExample.y += y;
	}

	private void test() {
		f(x);
		g(y);
	}
}
