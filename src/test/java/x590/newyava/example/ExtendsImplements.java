package x590.newyava.example;

import org.junit.Test;
import x590.newyava.Main;

import java.io.Serializable;

@SuppressWarnings("all")
public class ExtendsImplements extends SuperClass implements Serializable, Interface {
	@Test
	public void run() {
		Main.run(this);
	}

	@Override
	public int exampleMethod1(int x) {
		return 5;
	}
}
