package x590.newyava.test;

import org.junit.Assert;
import org.junit.Test;
import x590.newyava.annotation.DecompilingAnnotation;

import java.util.List;

public class AnnotationsEqualsTest {
	@Test
	public void test() {
		List<DecompilingAnnotation>
				a1 = List.of(new DecompilingAnnotation("Lorg/jetbrains/annotations/Nullable")),
				a2 = List.of(new DecompilingAnnotation("Lorg/jetbrains/annotations/Nullable"));

		a1.get(0).visit("value", 1);
		a2.get(0).visit("value", 1);

		Assert.assertEquals(a1, a2);

		a1.get(0).visit("val2", "abc");

		Assert.assertNotEquals(a1, a2);
	}
}
