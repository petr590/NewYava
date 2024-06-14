package x590.newyava.example;

import org.junit.Test;
import x590.newyava.Config;

@SuppressWarnings("all")
public class OtherExample {
	@Test
	public void run() {
		Main.run(this, Config.builder().ignoreVariableTable(true).build());
	}

	private void longVarNames(long x, long y, long z) {}

	private native void longVarNamesWithoutMethodBody(long x, long y, long z);

	private void argumentOrder() {
		longVarNames(1, 2, 3);
	}
}
