package x590.newyava.test.exploring;

import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import org.junit.Test;

public class FastUtilMapsTest {
	@Test
	public void run() {
		Float2ObjectMap<String> floatMap = new Float2ObjectOpenHashMap<>();
		Double2ObjectMap<String> doubleMap = new Double2ObjectOpenHashMap<>();

		floatMap.put(Float.NaN, "NaN");
		doubleMap.put(Double.NaN, "NaN");

		System.out.println(floatMap.get(Float.NaN));
		System.out.println(doubleMap.get(Double.NaN));
	}
}
