package x590.newyava.example.code.scope;

import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class SynchronizedExample {
	@Test
	public void run() {
		Main.run(this);
	}

	private final Object
			mutex1 = new Object(),
			mutex2 = new Object();

	public void foo(Object obj, @Nullable String s) {
		synchronized (mutex1) {
			System.out.println(obj);

			if (s != null) {
				synchronized (mutex2) {
					System.out.println(s);
				}

				synchronized ((Object) null) {
					System.out.println((String) null);
				}
			}

			System.out.println(obj);
		}
	}
}
