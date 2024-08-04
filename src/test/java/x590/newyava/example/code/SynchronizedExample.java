package x590.newyava.example.code;

import org.junit.Test;
import x590.newyava.example.Main;

//@SuppressWarnings("all")
public class SynchronizedExample {
	@Test
	public void run() {
		Main.run(this);
	}

	private final Object
			mutex1 = new Object(),
			mutex2 = new Object();

	public void foo(Object obj, String s) {
		synchronized (mutex1) {
			System.out.println(obj);

			if (s != null) {
				synchronized (mutex2) {
					System.out.println(s);
				}
			}

			System.out.println(obj);
		}
	}
}
