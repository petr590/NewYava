package x590.newyava.example.generics;

import org.junit.Test;
import x590.newyava.example.Main;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public class GenericExample<T extends Serializable & Comparable<T>, F, U extends F>
		extends SuperClass<T> implements SuperInterface<T, GenericExample<T, F, U>> {

	@Test
	public void run() {
		Main.run(this);
	}

	private List<T> list;

	public Map<? extends T, ? super SuperClass<?>> map;

	public native <G, Ex extends RuntimeException> G method(T t, List<T> list) throws Ex, ClassNotFoundException;

	public native <G> GenericExample<T, F, U>.Middle.Inner<?, ?> get(SuperClass<G> genericExample);

	private class Middle {
		private class Inner<B, C> {}
	}
}
