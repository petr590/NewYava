package x590.newyava.example.sealed;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public sealed class SealedNestedClassExample {
	@Test
	public void run() {
		Main.run(this);
	}

	private static sealed class X extends SealedNestedClassExample {
		private static sealed class Y extends X {
			private static non-sealed class Z extends Y {}
		}
	}

	private static final class W extends SealedNestedClassExample {}
}
