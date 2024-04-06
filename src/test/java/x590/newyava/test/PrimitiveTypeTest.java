package x590.newyava.test;

import org.junit.Assert;
import org.junit.Test;
import x590.newyava.type.IntMultiType;

import static x590.newyava.type.PrimitiveType.*;

public class PrimitiveTypeTest {
	@Test
	public void testLFD() {
		Assert.assertNull(assignQuiet(INT, LONG));
		Assert.assertNull(assignQuiet(INT, FLOAT));
		Assert.assertNull(assignQuiet(INT, DOUBLE));
		Assert.assertNull(assignQuiet(LONG, FLOAT));
		Assert.assertNull(assignQuiet(LONG, DOUBLE));
		Assert.assertNull(assignQuiet(FLOAT, DOUBLE));
	}

	@Test
	public void testIntMultiType() {
//		Assert.assertEquals(, assignQuiet(IntMultiType.valueOf(IntMultiType.INT_FLAG), INT));
	}
}
