package x590.newyava.test;

import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Test;
import x590.newyava.Config;
import x590.newyava.Decompiler;
import x590.newyava.DecompilingClass;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

@SuppressWarnings("unused")
public class OperationsEqualsTest {
	@Test
	public void test() {
		var decompiler = new Decompiler(Config.defaultConfig());
		decompiler.run(OperationsEqualsTest.class);

		var classType = ClassType.valueOf(OperationsEqualsTest.class);

		var clazz = decompiler.findClass(classType).orElseThrow();

		compareMethods(clazz, "f1", "f2", PrimitiveType.INT);
		compareMethods(clazz, "f3", "f4", PrimitiveType.VOID);
	}

	private void compareMethods(DecompilingClass clazz, String name1, String name2, Type returnType) {
		var f1 = clazz.findMethod(new MethodDescriptor(clazz.getThisType(), name1, returnType)).orElseThrow();
		var f2 = clazz.findMethod(new MethodDescriptor(clazz.getThisType(), name2, returnType)).orElseThrow();

		var ops1 = f1.getCode().getMethodScope().getOperations();
		var ops2 = f2.getCode().getMethodScope().getOperations();

		Assert.assertEquals(ops1.size(), ops2.size());

		for (int i = 0, s = ops1.size(); i < s; i++) {
			Assert.assertEquals(ops1.get(i), ops2.get(i));
		}
	}

	private int y;
	private boolean b;

	private @Nullable String str;
	private @Nullable Object obj;
	private Object @Nullable[] arr1;

	@SuppressWarnings("all")
	private float f1() {
		int x = 0;
		x++;
		y++;
		x = ~-x;
		b = x == 0;
		b |= new Integer(1) instanceof Object;
		obj = "abc=" + new Object();
		str = (String) obj;
		s(1, 2, 4);
		super.toString();
		arr1 = new Object[] { "a", 1, new Object() };
		System.out.println(arr1[0]);
		System.out.println(arr1[1] = 2);
		System.out.println(arr1.length);
		return x;
	}

	@SuppressWarnings("all")
	private float f2() {
		int x = 0;
		x++;
		y++;
		x = ~-x;
		b = x == 0;
		b |= new Integer(1) instanceof Object;
		obj = "abc=" + new Object();
		str = (String) obj;
		s(1, 2, 4);
		super.toString();
		arr1 = new Object[] { "a", 1, new Object() };
		System.out.println(arr1[0]);
		System.out.println(arr1[1] = 2);
		System.out.println(arr1.length);
		return x;
	}

	@SuppressWarnings("all")
	private void f3() {
		throw null;
	}

	@SuppressWarnings("all")
	private void f4() {
		throw null;
	}

	@SuppressWarnings("all")
	private static long s(long a, float b, double c) {
		return 1;
	}
}
