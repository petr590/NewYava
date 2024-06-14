package x590.newyava.example;

@SuppressWarnings("all")
public record RecordExample(int i, long l, float f, double d, String s) {
	private static final int CONSTANT = 1;

	public static void main(String[] args) {
		Main.run(RecordExample.class);
	}
}
