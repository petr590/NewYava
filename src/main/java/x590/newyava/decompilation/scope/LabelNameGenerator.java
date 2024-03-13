package x590.newyava.decompilation.scope;

public class LabelNameGenerator {
	private int index;

	public String nextLabelName() {
		return "L" + ++index;
	}
}
