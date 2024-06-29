package x590.newyava.example.code;

import org.junit.Test;
import x590.newyava.example.InterfaceExample;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class SuperExample implements InterfaceExample {
	@Test
	public void run() {
		Main.run(this);
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public int exampleMethod1(int x) {
		return 0;
	}

	@Override
	public int exampleMethod2(int x) {
		return InterfaceExample.super.exampleMethod2(x);
	}
}
