package x590.newyava.example;

import org.junit.Test;

import java.io.Serializable;

@SuppressWarnings("all")
public class ExtendsImplementsExample extends SuperClass implements Serializable, InterfaceExample {
	@Test
	public void run() {
		Main.run(this);
	}

	@Override
	public int exampleMethod1(int x) {
		return 5;
	}
}
