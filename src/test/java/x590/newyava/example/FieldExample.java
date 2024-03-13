package x590.newyava.example;

import lombok.Getter;
import lombok.Setter;
import x590.newyava.Main;

@SuppressWarnings("all")
public class FieldExample {
	public static void main(String[] args) {
		Main.run(FieldExample.class);
	}

	@Getter @Setter
	private int x;

	@Getter @Setter
	private static int y;
}
