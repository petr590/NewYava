package x590.newyava.example.nested;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class NonstaticNestedClassExample {

	@Test
	public void run() {
		Main.run(this);
	}

	class Middle {
		@Override
		public String toString() {
			return NonstaticNestedClassExample.this.toString();
		}

		class Inner {
			@Override
			public String toString() {
				return NonstaticNestedClassExample.this + " " + Middle.this;
			}
		}
	}
}
