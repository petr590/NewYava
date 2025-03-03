package x590.newyava.example.code.scope;

import org.junit.Test;
import x590.newyava.Config;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class LoopsExample {

	@Test
	public void run() {
		Main.run(this, Config.builder().ignoreVariableTable(false).build());
	}


	public static volatile boolean stopped;

	public void doWhile() {
		do {
			System.out.println("loop1");

			if (Math.random() % 2 == 0) {
				System.out.println("gg");
			}

			System.out.println("loop2");

		} while (!stopped);
	}

	public void loopScope() {
		while (!stopped)
			System.out.println("ex");
	}

	public void loopBreak() {
		while (!stopped) {
			System.out.println("ex1");

			if (Math.random() > 0.5)
				break;

			System.out.println("ex2");
		}
	}

	public void loopContinue() {
		while (!stopped) {
			System.out.println("ex1");

			if (Math.random() > 0.5)
				continue;

			System.out.println("ex2");
		}
	}

	public void infiniteLoop() {
		while (true)
			System.out.println("ex");
	}

	public void complex() {
		System.out.println("before loop");

		C: while (true != stopped) {
			while (Math.random() > 0.5) {
				System.out.println("ex1");

				if (Math.random() > 0.3)
					break C;

				System.out.println("ex2");
			}

			System.out.println("ex3");
		}
	}

	private void forLoop() {
		for (int i = 0; i < 10; i++) {
			System.out.println(i);
		}
	}

	public void rangeFor() {
		var arr = new int[10];

		for (int x : arr) {
			System.out.println(x);
		}
	}
}
