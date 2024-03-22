package x590.newyava.example;

import org.junit.Test;
import x590.newyava.Config;
import x590.newyava.Main;

import java.util.function.Function;
import java.util.function.IntFunction;

@SuppressWarnings("all")
public class LambdaExample {
	@Test
	public void run() {
		Main.run(this, Config.builder().ignoreVariableTable(true).build());
	}

	public Function<Integer, String> lambdaReference() {
		return String::valueOf;
	}

	public IntFunction<String> lambdaWithCode() {
		return i -> Integer.toHexString(i) + "h";
	}

	public IntFunction<String> lambdaWithCapturedVars(int x, long l) {
		return i -> Integer.toHexString(i) + "h (" + x + ", " + l + ")";
	}

	public Function<String, String> lambdaNew() {
		return String::new;
	}

	public Function<Integer, int[]> lambdaNewArray() {
		return int[]::new;
	}

	public Function<Integer, int[][]> lambdaNewMatrix() {
		return int[][]::new;
	}
}
