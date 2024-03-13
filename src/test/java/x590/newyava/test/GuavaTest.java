package x590.newyava.test;

import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import org.junit.Test;

public class GuavaTest {
	@Test
	public void test() {
		SortedSetMultimap<Integer, String> table = TreeMultimap.create();

		table.put(1, "b");
		table.put(1, "a");
		table.put(1, "e");
		table.put(3, "g");
		table.put(3, "f");

		System.out.println(table.asMap());
	}
}
