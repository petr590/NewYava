package x590.newyava.test;

import org.junit.Assert;
import org.junit.Test;
import x590.newyava.annotation.DecompilingAnnotation;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.io.SignatureReader;
import x590.newyava.type.*;

import java.util.List;
import java.util.function.Supplier;

public class EqualsTest {

	private static <T> void checkEquals(Supplier<T> creator) {
		T t1 = creator.get();
		T t2 = creator.get();
		Assert.assertEquals(t1, t2);
		Assert.assertEquals(t1.hashCode(), t2.hashCode());
	}

	@Test
	public void testDescriptors() {
		checkEquals(() -> new MethodDescriptor(ClassType.OBJECT, "method", ArrayType.forType(PrimitiveType.INT, 2)));
		checkEquals(() -> new FieldDescriptor(
				IClassType.parse(new SignatureReader("net/minecraft/advancements/AdvancementRequirements;")),
				"requirements",
				Type.parse(new SignatureReader("[[Ljava/lang/String;"))
		));
	}

	@Test
	public void testType() {
		checkEquals(() -> ArrayType.forType(PrimitiveType.INT, 2));
		checkEquals(() -> ClassType.valueOf("java/util/List"));
		checkEquals(() -> IntMultiType.valueOf(IntMultiType.BYTE_FLAG | IntMultiType.SHORT_FLAG));
	}

	@Test
	public void testAnnotations() {
		List<DecompilingAnnotation>
				a1 = List.of(new DecompilingAnnotation("Lorg/jetbrains/annotations/Nullable")),
				a2 = List.of(new DecompilingAnnotation("Lorg/jetbrains/annotations/Nullable"));

		a1.get(0).visit("value", 1);
		a2.get(0).visit("value", 1);

		Assert.assertEquals(a1, a2);

		a1.get(0).visit("val2", new int[] { 2 });

		Assert.assertNotEquals(a1, a2);

		a2.get(0).visit("val2", new int[] { 2 });

		Assert.assertEquals(a1, a2);
	}
}
