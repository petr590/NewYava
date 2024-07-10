package x590.newyava;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Map;

@UtilityClass
public final class Util {
	/** Добавляет элемент в коллекцию и возвращает его.
	 * Удобно использовать для более короткой записи кода. */
	public static <T> T addAndGetBack(Collection<? super T> collection, T element) {
		collection.add(element);
		return element;
	}

	/** Добавляет элемент в карту и возвращает его.
	 * Удобно использовать для более короткой записи кода. */
	public static <K, V> V putAndGetBack(Map<? super K, ? super V> map, K key, V value) {
		map.put(key, value);
		return value;
	}
}
