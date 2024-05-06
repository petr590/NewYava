package x590.newyava.test;

import org.junit.Assert;
import org.junit.Test;

import static x590.newyava.type.Type.*;
import static x590.newyava.type.PrimitiveType.*;
import static x590.newyava.type.IntMultiType.*;

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
		Assert.assertNull(assignUpQuiet(BOOLEAN, INT));
		Assert.assertNull(assignUpQuiet(BYTE, INT));
		Assert.assertNull(assignUpQuiet(SHORT, INT));
		Assert.assertNull(assignUpQuiet(CHAR, INT));


		Assert.assertEquals(INT,                                        assignUpQuiet(INTEGRAL, INT));
		Assert.assertEquals(valueOf(INT_FLAG | SHORT_FLAG),             assignUpQuiet(INTEGRAL, SHORT));
		Assert.assertEquals(valueOf(INT_FLAG | SHORT_FLAG | BYTE_FLAG), assignUpQuiet(INTEGRAL, BYTE));
		Assert.assertEquals(valueOf(INT_FLAG | CHAR_FLAG),              assignUpQuiet(INTEGRAL, CHAR));
		Assert.assertEquals(BOOLEAN,                                    assignUpQuiet(INTEGRAL, BOOLEAN));


		Assert.assertEquals(NUMERIC,                         assignDownQuiet(INTEGRAL, INT));
		Assert.assertEquals(valueOf(BYTE_FLAG | SHORT_FLAG), assignDownQuiet(INTEGRAL, SHORT));
		Assert.assertEquals(BYTE,                            assignDownQuiet(INTEGRAL, BYTE));
		Assert.assertEquals(CHAR,                            assignDownQuiet(INTEGRAL, CHAR));
		Assert.assertEquals(BOOLEAN,                         assignDownQuiet(INTEGRAL, BOOLEAN));

		Assert.assertEquals(INT,     assignUpQuiet(NUMERIC, INT));
		Assert.assertEquals(NUMERIC, assignDownQuiet(NUMERIC, INT));
	}
}
