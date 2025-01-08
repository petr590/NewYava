package x590.newyava.example.code;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class CompareExample {
	@Test
	public void run() {
		Main.run(this);
	}

	public void compare(int a, int b, double d, boolean z, Object o1, Object o2) {
		if (o1 == o2) System.out.println("o1 == o2");
		if (o1 != o2) System.out.println("o1 != o2");

		System.out.println("-------------------------");

		if (a == 0) System.out.println("a == 0");
		if (a != 0) System.out.println("a != 0");
		if (a <  0) System.out.println("a < 0");
		if (a >  0) System.out.println("a > 0");
		if (a <= 0) System.out.println("a <= 0");
		if (a >= 0) System.out.println("a >= 0");

		System.out.println("-------------------------");

		if (a == b) System.out.println("a == b");
		if (a != b) System.out.println("a != b");
		if (a <  b) System.out.println("a < b");
		if (a >  b) System.out.println("a > b");
		if (a <= b) System.out.println("a <= b");
		if (a >= b) System.out.println("a >= b");

		System.out.println("-------------------------");

		if (d == 1) System.out.println("d == 1");
		if (d != 1) System.out.println("d != 1");
		if (d <  1) System.out.println("d < 1");
		if (d >  1) System.out.println("d > 1");
		if (d <= 1) System.out.println("d <= 1");
		if (d >= 1) System.out.println("d >= 1");

		System.out.println("-------------------------");

		if (z) System.out.println("z");
		if (!z) System.out.println("!z");
		if (z == true) System.out.println("z == true");
	}

	public void compare(byte b, short s) {
		if (b == 5) System.out.println("b == 5");
		if (b != 5) System.out.println("b != 5");
		if (b <  5) System.out.println("b < 5");
		if (b >  5) System.out.println("b > 5");
		if (b <= 5) System.out.println("b <= 5");
		if (b >= 5) System.out.println("b >= 5");

		System.out.println("-------------------------");

		if (s == 5) System.out.println("s == 5");
		if (s != 5) System.out.println("s != 5");
		if (s <  5) System.out.println("s < 5");
		if (s >  5) System.out.println("s > 5");
		if (s <= 5) System.out.println("s <= 5");
		if (s >= 5) System.out.println("s >= 5");

		System.out.println("-------------------------");

		if (b == s) System.out.println("b == s");
		if (b != s) System.out.println("b != s");
		if (b <  s) System.out.println("b < s");
		if (b >  s) System.out.println("b > s");
		if (b <= s) System.out.println("b <= s");
		if (b >= s) System.out.println("b >= s");
	}
}
