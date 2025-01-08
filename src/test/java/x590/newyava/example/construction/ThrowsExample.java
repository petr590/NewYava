package x590.newyava.example.construction;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class ThrowsExample {
	@Test
	public void run() {
		Main.run(this);
	}

	public native void method() throws ClassNotFoundException;
}
