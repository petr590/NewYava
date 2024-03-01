package x590.newyava.example;

import org.junit.Test;
import x590.newyava.Main;

@SuppressWarnings("all")
public class Conditions {

	@Test
	public void run() {
		Main.run(this);
	}

	public void ifScope(boolean x) {
		if (x) {
			System.out.println("if");
		}

		System.out.println("always");
	}

	public void ifElseScope(boolean x) {
		if (x) {
			System.out.println("if");
		} else {
			System.out.println("else");
		}
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
