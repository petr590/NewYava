package x590.newyava.example.code;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class VarargsExample {
	@Test
	public void run() {
		Main.run(this);
	}

	public void foo(String s, int i, boolean b) {
		System.out.printf("%s, %d, %b\n", s, i, b);
		System.out.printf("\n");
		varargsMethod("", 1, 2, 3);
		varargsMethod(1, 2, 3);
		varargsMethod();
	}

	private void varargsMethod(int... other) {}

	private void varargsMethod(String first, int... other) {}
}
