package x590.newyava.example.construction.sealed;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public sealed class SealedClassExample permits Subclass1, Subclass2 {

	@Test
	public void run() {
		Main.run(SealedClassExample.class, Subclass1.class, Subclass2.class);
	}
}
