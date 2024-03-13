package x590.newyava.example;

import x590.newyava.Main;

@SuppressWarnings("all")
public record RecordExample(int i, long l, float f, double d, String s) {
	public static void main(String[] args) {
		Main.run(RecordExample.class);
	}
}
