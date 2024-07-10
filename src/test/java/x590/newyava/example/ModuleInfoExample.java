package x590.newyava.example;

import org.junit.Test;
import x590.newyava.Config;
import x590.newyava.Decompiler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.Stream;

@SuppressWarnings("all")
public class ModuleInfoExample {
	@Test
	public void run() throws FileNotFoundException, IOException {
		new Decompiler(Config.defaultConfig()).run(
				Stream.of("module-info"),
				Decompiler.fileResourceGetter("target/module-example-classes/")
		);
	}
}
