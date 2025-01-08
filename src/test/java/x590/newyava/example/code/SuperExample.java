package x590.newyava.example.code;

import org.junit.Test;
import x590.newyava.example.construction.InterfaceExample;
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
	public int interfaceMethod(int x) {
		return 0;
	}

	@Override
	public int defaultMethod(int x) {
		return InterfaceExample.super.defaultMethod(x);
	}
}
