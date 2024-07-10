package x590.newyava.test.exploring;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.junit.Test;

public class Int2ObjectMapTest {

	@Test
	public void run() {
		Int2ObjectMap<String> m = new Int2ObjectOpenHashMap<>();

		m.put(1, "a");
		m.put(5, "b");
		m.put(3, "b");
		m.put(2, "e");

		for (var e : m.int2ObjectEntrySet()) {
			System.out.println(e);
		}
	}
}
