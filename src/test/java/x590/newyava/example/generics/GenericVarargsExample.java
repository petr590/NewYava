package x590.newyava.example.generics;

import org.junit.Test;
import x590.newyava.example.Main;

import java.util.List;

@SuppressWarnings("all")
public class GenericVarargsExample {
	@Test
	public void run() {
		Main.run(this);
	}

	@SafeVarargs
	public static <T> void varargsMethod(T... args) {}

	@SafeVarargs
	public static <T> void varargsMethod(List<T>... lists) {}
}
