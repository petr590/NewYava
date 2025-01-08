package x590.newyava.example.feature;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class OverrideExample implements OverrideExampleInterface<String> {
	@Test
	public void run() {
		Main.run(this);
	}

	@Override
	public String foo() {
		return "foo";
	}

	@Override
	public String toString() {
		return "gg";
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof OverrideExample;
	}

	@Override
	public int hashCode() {
		return 12345;
	}


	public interface Interface extends OverrideExampleInterface<Integer> {
		@Override
		Integer foo();
	}
}
