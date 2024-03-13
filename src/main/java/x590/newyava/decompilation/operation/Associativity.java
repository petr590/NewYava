package x590.newyava.decompilation.operation;

public enum Associativity {
	LEFT, RIGHT;

	public Associativity opposite() {
		return this == LEFT ? RIGHT : LEFT;
	}
}
