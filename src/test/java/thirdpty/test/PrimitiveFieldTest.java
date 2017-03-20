package thirdpty.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.github.ryenus.optj.OptionParser;

public class PrimitiveFieldTest {

	private OptionParser parser;

	@Test
	public void primitiveFields() {
		parser = new OptionParser(Primitives.class);
		Primitives p = parser.get(Primitives.class);

		assertFalse(p.b);
		assertFalse(p.bt == 1);
		assertFalse(p.c == 'a');
		assertFalse(p.d == 0.1d);
		assertFalse(p.f == 0.2f);
		assertFalse(p.i == 8);
		assertFalse(p.l == 15L);
		assertFalse(p.s == Short.MIN_VALUE);

		Map<Object, String[]> result = parser.parse("--boolean --byte 1 --char a --double 0.1 --float 0.2 --int 010 --long 0xf --short -32768 -- -x".split("\\s+"));
		String[] args = result.get(p);

		assertTrue(p.b);
		assertTrue(p.bt == 1);
		assertTrue(p.c == 'a');
		assertTrue(p.d == 0.1d);
		assertTrue(p.f == 0.2f);
		assertTrue(p.i == 8);
		assertTrue(p.l == 15L);
		assertTrue(p.s == Short.MIN_VALUE);
		assertEquals(1, args.length);
		assertEquals("-x", args[0]);
	}
}
