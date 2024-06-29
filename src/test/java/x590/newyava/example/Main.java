package x590.newyava.example;

import x590.newyava.Config;
import x590.newyava.Decompiler;

public class Main {
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

	public static void run(Class<?>... classes) {
		new Decompiler(Config.defaultConfig()).run(classes);
	}

	public static void run(Object exampleObj, Config config) {
		run(exampleObj.getClass(), config);
	}

	public static void run(Class<?> exampleClass) {
		run(exampleClass, Config.defaultConfig());
	}

	public static void run(Class<?> exampleClass, Config config) {
		new Decompiler(config).run(exampleClass.getNestMembers());
	}


	public static void run(String className) {
		new Decompiler(Config.defaultConfig()).run(ClassLoader.getSystemClassLoader(), className);
	}
}