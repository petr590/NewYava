package x590.newyava.decompilation.scope;

/**
 * Генератор названий лейблов
 */
public class LabelNameGenerator {
	private int index;

	/** Не заморачиваемся и просто называем лейблы по порядку: L1, L2, L3, ... */
	public String nextLabelName() {
		return "L" + (index += 1);
	}
}
