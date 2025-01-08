package x590.newyava.example.code;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class WrapperExample {
	@Test
	public void run() {
		Main.run(this);
	}

	public boolean f(Object b) {
		return b != null;
	}

	public void foo() {
		f(false);
		f(true);

		if (f(true)) {
			System.out.println("if");
		}
	}
}
