package x590.newyava.example;

import org.junit.Test;

@SuppressWarnings("all")
public class ThrowsExample {
	@Test
	public void run() {
		Main.run(this);
	}

	public native void method() throws ClassNotFoundException;
}
