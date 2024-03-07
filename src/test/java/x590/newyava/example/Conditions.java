package x590.newyava.example;

import org.junit.Test;
import x590.newyava.Config;
import x590.newyava.Main;

@SuppressWarnings("all")
public class Conditions {

	@Test
	public void run() {
		Main.run(this, Config.builder().alwaysWriteBrackets(true).build());
	}

	public void ifScope(boolean x) {
		if (x) {
			System.out.println("if");
		}

		System.out.println("after");
	}

	public void ifElseScope(boolean x) {
		if (x) {
			System.out.println("if");
		} else {
			System.out.println("else");
		}

		System.out.println("after");
	}

	public void nestedIfScope(boolean x, boolean y) {
		if (x) {
			if (y) {
				System.out.println("if");
			}
		}

		System.out.println("after");
	}

	public void andCondition(boolean x, boolean y) {
		if (x && y) {
			System.out.println("if");
		}

		System.out.println("after");
	}

	public void orCondition(boolean x, boolean y) {
		if (x || y) {
			System.out.println("if");
		}

		System.out.println("after");
	}

	public void nestedIfElseScope(boolean x, boolean y) {
		if (x) {
			System.out.println("if1");

			if (y) {
				System.out.println("if2");
			}
		} else {
			System.out.println("else");
		}
	}
}
