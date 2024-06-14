package x590.newyava.example.enums;

import x590.newyava.example.Main;

@SuppressWarnings("all")
public enum EnumWithNestedClassExample {
	A;

	private final Nested nested;

	EnumWithNestedClassExample() {
		this.nested = new Nested();
	}

	public static void main(String[] args) {
		Main.run(EnumWithNestedClassExample.class);
	}

	class Nested {
		public String nestedName() {
			return name();
		}


		class Nested2 {
			@Override
			public String toString() {
				return nestedName() + " " + name();
			}
		}
	}

	static class StaticNested {}
}
