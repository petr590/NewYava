package x590.newyava.example.variable;

import org.junit.Test;
import x590.newyava.Config;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class VariablesInitExample {
	@Test
	public void run() {
		Main.run(this, Config.builder().ignoreVariableTable(false).build());
	}

	private void f(int x) {}

	public void foo(boolean x, boolean y) {
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
}
