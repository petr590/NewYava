package x590.newyava;

import org.objectweb.asm.*;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;

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

		Class<?>[] srcClasses = exampleClass.getNestMembers();

		Map<ClassType, DecompilingClass> classMap = Arrays.stream(srcClasses)
				.map(clazz -> {
					try {
						return new DecompilingClass(new ClassReader(getResource(clazz)));
					} catch (IOException ex) {
						throw new UncheckedIOException(ex);
					}
				}).collect(Collectors.toMap(DecompilingClass::getThisType, clazz -> clazz));

		Collection<DecompilingClass> classes = classMap.values();


		classes.forEach(clazz -> clazz.initNested(classMap));

		classes.forEach(DecompilingClass::decompile);
		classes.forEach(DecompilingClass::initVariables);

		classes.forEach(DecompilingClass::addImports);
		classes.forEach(DecompilingClass::computeImports);

		var writer = new DecompilationWriter(new OutputStreamWriter(System.out));

		classes.stream().filter(DecompilingClass::keep).forEach(clazz -> {
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