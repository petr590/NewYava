package x590.newyava.example.nested;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class NestedClassExample {

	@Test
	public void run() {
		Main.run(this);
	}

	public static class Another {}

	public static class Middle {
		public static class Inner {

			public Inner(String arg1, int arg2) {}

			public Class<?> getAnonClass() {
				int id = (int)(Math.random() * 1000);
				String strId = String.valueOf(id);

				return new Inner("abc", 5) {
					private int x = 1;

					public String toString() {
						return String.valueOf(id + x) + strId + Inner.this.toString();
					}
				}.getClass();
			}
		}
	}

	public static Class<?> getNestClassInMethod1() {
		class In$Method {}

		return In$Method.class;
	}

	public static Class<?> getNestClassInMethod2() {
		int x = 10;

		class In$Method {
			@Override
			public int hashCode() {
				return x;
			}
		}

		return new In$Method().getClass();
	}
}
