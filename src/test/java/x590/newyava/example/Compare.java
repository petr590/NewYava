package x590.newyava.example;

import org.junit.Test;
import x590.newyava.Main;

@SuppressWarnings("all")
public class Compare {
	@Test
	public void run() {
		Main.run(this);
	}

	public void compare(int a, int b, double d, boolean z, Object o1, Object o2) {
		if (o1 == o2) System.out.println("o1 == o2");
		if (o1 != o2) System.out.println("o1 != o2");

		if (a == 0) System.out.println("a == 0");
		if (a != 0) System.out.println("a != 0");
		if (a <  0) System.out.println("a < 0");
		if (a >  0) System.out.println("a > 0");
		if (a <= 0) System.out.println("a <= 0");
		if (a >= 0) System.out.println("a >= 0");

		if (a == b) System.out.println("a == b");
		if (a != b) System.out.println("a != b");
		if (a <  b) System.out.println("a < b");
		if (a >  b) System.out.println("a > b");
		if (a <= b) System.out.println("a <= b");
		if (a >= b) System.out.println("a >= b");

		if (d == 1) System.out.println("d == 1");
		if (d != 1) System.out.println("d != 1");
		if (d <  1) System.out.println("d < 1");
		if (d >  1) System.out.println("d > 1");
		if (d <= 1) System.out.println("d <= 1");
		if (d >= 1) System.out.println("d >= 1");

		if (z) System.out.println("z");
		if (!z) System.out.println("!z");
		if (z == true) System.out.println("z == true");
	}
}
