package x590.newyava.example.feature;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class NullCastExample {
	@Test
	public void run() {
		Main.run(this);
	}

	public int foo() {
		return ((Object) null).hashCode();
	}
}
