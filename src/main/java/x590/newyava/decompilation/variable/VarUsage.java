package x590.newyava.decompilation.variable;

/**
 * Описывает, как переменная используется в операциях и scope-ах.
 */
public enum VarUsage {
	NONE, LOAD, STORE, MAYBE_STORE;

	/** @return {@link #MAYBE_STORE} для {@link #STORE}, для остальных - {@code this} */
	public VarUsage maybe() {
		return this == STORE ? MAYBE_STORE : this;
	}
}