package x590.newyava;

import org.objectweb.asm.*;
import x590.newyava.io.DecompilationWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Main {

	public static void main(String[] args) {}

	public static void run(Object exampleObj) {
		run(exampleObj.getClass(), Config.builder().build());
	}

	public static void run(Object exampleObj, Config config) {
		run(exampleObj.getClass(), config);
	}

	public static void run(Class<?> exampleClass) {
		run(exampleClass, Config.builder().build());
	}

	public static void run(Class<?> exampleClass, Config config) {
		Config.init(config);

		Class<?>[] classes = exampleClass.getNestMembers();

		List<DecompilingClass> decompilingClasses = Arrays.stream(classes)
				.map(clazz -> {
					try {
						return new DecompilingClass(new ClassReader(getResource(clazz)));
					} catch (IOException ex) {
						throw new UncheckedIOException(ex);
					}
				}).toList();

		decompilingClasses.forEach(DecompilingClass::decompile);

		var writer = new DecompilationWriter(new OutputStreamWriter(System.out));

		decompilingClasses.forEach(DecompilingClass::addImports);
		decompilingClasses.forEach(DecompilingClass::computeImports);
		decompilingClasses.forEach(clazz -> {
			writer.record("\n\n----------------------------------------------------------------\n\n");
			clazz.write(writer);
		});

		try {
			writer.flush();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private static InputStream getResource(Class<?> clazz) {
		return Objects.requireNonNull(clazz.getResourceAsStream(
				clazz.getName().substring(clazz.getPackageName().length() + 1) + ".class"
		));
	}
}