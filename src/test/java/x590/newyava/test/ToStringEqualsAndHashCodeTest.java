package x590.newyava.test;

import org.junit.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ToStringEqualsAndHashCodeTest {
	@Test
	public void test() throws NoSuchMethodException {
		loadAllClasses(ClassLoader.getSystemClassLoader(), new File("target/classes/x590/newyava"));

		Method  objectToString = Object.class.getMethod("toString"),
				objectHashCode = Object.class.getMethod("hashCode"),
				objectEquals = Object.class.getMethod("equals", Object.class);

		for (Class<?> clazz : classes) {
			if (clazz.isInterface() || Throwable.class.isAssignableFrom(clazz))
				continue;

			Method  toString = clazz.getMethod("toString"),
					hashCode = clazz.getMethod("hashCode"),
					equals = clazz.getMethod("equals", Object.class);

			if (toString.equals(objectToString) ||
				hashCode.equals(objectHashCode) ||
				equals.equals(objectEquals)) {

				@SuppressWarnings("all")
				var str = new StringBuilder()
						.append(toString.equals(objectToString) ? 'S' : ' ')
						.append(hashCode.equals(objectHashCode) ? 'H' : ' ')
						.append(equals.equals(objectEquals) ? 'E' : ' ')
						.append(' ').append(clazz.getName()).append('(')
						.append(Objects.requireNonNullElse(clazz.getEnclosingClass(), clazz).getSimpleName())
						.append(".java:1)");

				System.out.println(str);
			}
		}

		throw new IllegalArgumentException();
	}

	private final List<Class<?>> classes = new ArrayList<>();

	private void loadAllClasses(ClassLoader classLoader, File directory) {
		File[] files = directory.listFiles();

		if (files == null) return;

		for (File file : files) {
			if (file.isDirectory()) {
				loadAllClasses(classLoader, file);

			} else if (file.isFile() && file.getName().endsWith(".class")) {
				String path = file.getPath();
				String className = path.substring("target/classes/".length(), path.length() - ".class".length())
						.replace('/', '.');

				try {
					classes.add(classLoader.loadClass(className));
				} catch (ClassNotFoundException ex) {
					System.err.println("Cannot load class \"" + className + "\"");
				}
			}
		}
	}
}
