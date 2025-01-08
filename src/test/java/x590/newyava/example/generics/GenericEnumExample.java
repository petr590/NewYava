package x590.newyava.example.generics;

import x590.newyava.example.Main;

import java.util.List;

@SuppressWarnings("unused")
public enum GenericEnumExample {
	INSTANCE(List.of("a", "b", "c"));

	GenericEnumExample(List<String> list) {}

	public static void main(String[] args) {
		Main.runForCaller();
	}
}
