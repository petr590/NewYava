package x590.newyava.test.decompiler;

import org.junit.Test;
import x590.newyava.example.Main;

import java.io.FileOutputStream;
import java.io.IOException;

public class BugsTest {
	@Test
	public void run() {
		Main.run(this);
	}

	@Bug(State.FIXED)
	public boolean bug1(boolean b1, boolean b2) {
		return b1 ^ b2;
	}


	@Bug
	public void bug2(boolean x) {
		if (x) {
			float f = 1;
			System.out.println(f);
		}

		Object o = new Object();
		System.out.println(o);
	}

	@Bug(State.PARTIALLY_FIXED)
	public Runnable bug3() {
		return () -> {
			try (var out = new FileOutputStream("file")) {
				out.write(0);
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				System.out.println("finally");
			}
		};
	}
}
