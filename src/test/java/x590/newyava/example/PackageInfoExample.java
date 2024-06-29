package x590.newyava.example;

import org.junit.Test;
import x590.newyava.Config;
import x590.newyava.Decompiler;

import java.util.stream.Stream;

@SuppressWarnings("all")
public class PackageInfoExample {
	@Test
	public void run() {
		new Decompiler(Config.defaultConfig()).run(
				Stream.of("package-info.class"),
				name -> this.getClass().getResourceAsStream(name)
		);
	}
}
