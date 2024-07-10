package x590.newyava.example;

import org.junit.Test;

@SuppressWarnings("all")
public class IntAsCharExample {
	@Test
	public void run() {
		Main.run(this);
	}

	public void foo() {
		"abc".indexOf('c');
		"abc".indexOf('c', 1);
		"abc".lastIndexOf('c');
		"abc".lastIndexOf('c', 1);

		"abc".indexOf(0xFFFFFFFF);
	}
}
