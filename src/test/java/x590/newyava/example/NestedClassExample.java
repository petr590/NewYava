package x590.newyava.example;

import org.junit.Test;
import x590.newyava.Main;

import java.util.List;

@SuppressWarnings("all")
public class NestedClassExample {

	@Test
	public void run() {
		Main.run(this);
	}

	public static void recursion() {
		recursion();
	}

	public static class Middle {
		public static class Inner {
			public static Class<?> getAnonClass(List<?> justAList) {
				return new Object() {}.getClass();
			}
		}
	}

	public static Class<?> getNestClassInMethod1() {
		class In$Method {}

		return In$Method.class;
	}

	public static Class<?> getNestClassInMethod2() {
		class In$Method {}

		return In$Method.class;
	}
}
