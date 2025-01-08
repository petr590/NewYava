package x590.newyava.example.code.scope;

import org.junit.Test;
import x590.newyava.Config;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class ConditionsExample {

	@Test
	public void run() {
		Main.run(this, Config.builder().alwaysWriteBrackets(true).build());
	}


	public void emptyIf(boolean x) {
		if (x) {}
	}

	public void ifScope(boolean x) {
		System.out.println("before");

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
			System.out.println("if");

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

	public void andCondition(boolean x, boolean y, boolean z, boolean w) {
		if (x && y && z && w) {
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

	public void orCondition(boolean x, boolean y, boolean z, boolean w) {
		if (x || y || z || w) {
			System.out.println("if");
		}

		System.out.println("after");
	}

	public void complexConditions(boolean x, boolean y, boolean z, boolean w) {
		if (x && y || z) {
			System.out.println("if1");
		}

		if ((x || y) && z) {
			System.out.println("if2");
		}

		if (x && (y || z)) {
			System.out.println("if3");
		}

		if (x || y && z) {
			System.out.println("if4");
		}

		if ((x || y) && (z || w)) {
			System.out.println("if5");
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
