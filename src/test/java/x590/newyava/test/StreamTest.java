package x590.newyava.test;

import org.junit.Test;

import java.util.List;

public class StreamTest {
	@Test
	public void test() {
		var list = List.of(1, 2, 3, 4, 5);
		list.stream().peek(System.out::println).forEach(System.out::println);
	}
}
