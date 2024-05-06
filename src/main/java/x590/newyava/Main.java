package x590.newyava;

import org.apache.commons.lang3.ArrayUtils;

public class Main {

	public static void main(String[] args) {}

	public static void runForCaller() {
		try {
			run(Class.forName(Thread.currentThread().getStackTrace()[2].getClassName()));
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void run(Object exampleObj) {
		run(exampleObj.getClass(), Config.defaultConfig());
	}
	public static void run(Object exampleObj, Class<?>... classes) {
		run(exampleObj.getClass(), Config.defaultConfig(), classes);
	}

	public static void run(Object exampleObj, Config config) {
		run(exampleObj.getClass(), config);
	}

	public static void run(String className) {
		run(className, Config.defaultConfig());
	}

	public static void run(String className, Config config) {
		new Decompiler(config).run(className);
	}

	public static void run(Class<?> exampleClass) {
		run(exampleClass, Config.defaultConfig());
	}

	public static void run(Class<?> exampleClass, Config config) {
		new Decompiler(config).run(exampleClass.getNestMembers());
	}

	public static void run(Class<?> exampleClass, Config config, Class<?>... classes) {
		new Decompiler(config).run(ArrayUtils.addAll(exampleClass.getNestMembers(), classes));
	}
}