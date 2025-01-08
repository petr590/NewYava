package x590.newyava.example.generics;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class GenericNestedExample<T> {
	@Test
	public void run() {
		Main.run(this);
	}

	private static void foo() {
		var ge = new GenericNestedExample<>();
		var mid = ge.new Middle<>();
		var inn = mid.new Inner();

		new GenericNestedExample<>().new Middle<>().new Inner();
	}

	private void bar() {
		var mid = this.new Middle<>();
	}

	private class Middle<U> {
		private class Inner {
			public native U u();

			@Override
			public String toString() {
				return GenericNestedExample.this.toString() + "." + Middle.this.toString();
			}
		}
	}
}
