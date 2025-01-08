package x590.newyava.example.fields;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import x590.newyava.example.Main;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class FieldGroupingExample {
	private final int x = 0;
	private final int y = 0;

	private static final String STRING_CONSTANT = "abc";

	private @Nullable List<?> list1;
	private final @NotNull List<?> list2 = new ArrayList<>();

	private @Nullable Object o1, o2, o3;

	@Test
	public void run() {
		Main.run(this);
	}
}
