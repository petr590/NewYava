package x590.newyava.example.construction.sealed;

import x590.newyava.example.Main;

public sealed interface SealedInterfaceExample permits SubInterface {
	static void main(String[] args) {
		Main.run(SealedInterfaceExample.class, Nested.class, SubInterface.class);
	}

	sealed interface Nested permits SubInterface {}
}
