package x590.newyava.test.exploring;

import org.junit.Test;

import java.util.Deque;
import java.util.LinkedList;

public class StreamTest {
	@Test
	public void test() {
		Deque<String> list = new LinkedList<>();

		list.push("a");
		list.push("b");
		list.push("c");

		var stream = list.stream();

		list.push("d");
//		list.pop();

		stream.forEach(System.out::println);
	}
}
