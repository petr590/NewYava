package x590.newyava.example.nested;

import org.junit.Test;
import x590.newyava.Config;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class NestedClassesExample {

	@Test
	public void run() {
		Main.run(this, Config.builder().ignoreVariableTable(true).build());
	}

	public static class Another {}

	public static class Middle {
		public static class Inner {

			public Inner(String arg1, long arg2) {}

			public Class<?> getAnonClass() {
				int id = (int)(Math.random() * 1000);
				String strId = String.valueOf(id);

				var inner = new Inner("abc", 5) {
					private static int x = 1;

					public static void foo() {}

					public String toString() {
						return String.valueOf(id + x) + strId + Inner.this.toString();
					}
				};

				inner.foo();

				return inner.x == 1 ? Inner.class : inner.getClass();
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
