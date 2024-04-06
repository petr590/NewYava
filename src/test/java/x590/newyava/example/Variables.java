package x590.newyava.example;

import org.junit.Test;
import x590.newyava.Config;
import x590.newyava.Main;

@SuppressWarnings("all")
public class Variables {

	@Test
	public void run() {
		Main.run(this, Config.builder().ignoreVariableTable(false).build());
	}

	public int add(int a, int b) {
		int e = a + b;
		return e;
	}

	public int min(int a, int b) {
		int e = a;

		if (b < a)
			e = b;

		return e;
	}

	public void foo(int a, int b) {
		if (a > 3) {
			int x = 10;
			System.out.println(x);
		} else {
			int z = 5;
			System.out.println(z);
		}
	}
}
