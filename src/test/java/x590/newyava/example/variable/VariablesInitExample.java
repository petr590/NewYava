package x590.newyava.example.variable;

import org.junit.Test;
import x590.newyava.Config;
import x590.newyava.example.Main;

import java.util.List;
import java.util.function.BooleanSupplier;

@SuppressWarnings("all")
public class VariablesInitExample {
	@Test
	public void run() {
		Main.run(this, Config.builder().ignoreVariableTable(false).build());
	}

	private void f(int x) {}

	public void foo1(boolean x, boolean y) {
		String s;

		if (x) {
			int g = 10;
			f(g);

			String s2 = "gg";

			if (y) {
				s = "abc";
				System.out.println(s2);
			} else {
				s = "ABC";
			}
		} else {
			s = "def";
		}

		if (x) {
			int g = 11;
			f(g);
		}

		System.out.println(s);
	}

	public void foo2(double x, double y) {
		String s;

		if (x >= 0) {
			if (y >= 0)
				s = "++";
			else
				s = "+-";
		} else {
			if (y >= 0)
				s = "-+";
			else
				s = "--";
		}

		System.out.println(s);
	}

	public void foo3(boolean b) {
		if (b) {
			int x = 10;
			System.out.println(x);
		}

		String s = "abc";
		System.out.println(s);
	}

	public void foo4() {
		for (int i = 0; i < 10; i++) {
			System.out.println(i);
		}

		String s = "abc";
	}

	public void foo5(boolean x) {
		String s;

		if (x) {
			s = "gg";
		} else {
			return;
		}

		System.out.println(s);
	}

	private volatile boolean stopped = false;

	public void foo6() {
		String s;

		do {
			s = "abc";
		} while (!stopped);

		System.out.println(s);
	}

	public void foo7() {
		String s;

		while (!stopped) {
			s = "abc";
		}

		s = "def";

		System.out.println(s);
	}

	public void foo8(BooleanSupplier supplier) {
		while (!supplier.getAsBoolean()) {
			try {
				bar();
			} catch (Throwable ex) {
				ex.printStackTrace();
				return;
			}
		}

		int x = getX();
		System.out.println(x);
	}

	public void foo9(String[] arr, List<Integer> list) {
		for (String s : arr) {
			System.out.println(s);
		}

		while (list.isEmpty()) {
			float f = 1f;
		}
	}

	private void bar() {}

	private int getX() {
		return 123;
	}
}
