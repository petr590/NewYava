package x590.newyava.example.construction;

import org.junit.Test;
import x590.newyava.example.Main;

import java.io.Serializable;

@SuppressWarnings("all")
public class ExtendsImplementsExample extends SuperClass implements Serializable, InterfaceExample {
	@Test
	public void run() {
		Main.run(this);
	}

	@Override
	public int interfaceMethod(int x) {
		return 5;
	}
}
