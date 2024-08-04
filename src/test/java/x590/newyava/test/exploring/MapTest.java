package x590.newyava.test.exploring;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MapTest {
	@Test
	public void testComputeIfAbsent() {
		Map<String, Integer> map = new HashMap<>();

		map.computeIfAbsent("abc", key -> null);
		map.computeIfAbsent("abc", key -> {
			System.out.println("gg");
			return null;
		});

		System.out.println(map.containsKey("abc"));
	}
}
