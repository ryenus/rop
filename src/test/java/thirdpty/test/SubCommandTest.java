package thirdpty.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.github.ryenus.optj.OptionParser;


public class SubCommandTest {

	private OptionParser parser;

	@Test
	public void subcommands() {
		Wrappers o = new Wrappers();
		parser = new OptionParser(Primitives.class, o, PrivateConstructor.class);
		Primitives p = parser.get(Primitives.class);
		PrivateConstructor c = parser.get(PrivateConstructor.class);

		assertFalse(p.b);
		assertFalse(p.bt == 1);
		assertFalse(p.c == 'a');

		assertEquals(null, o.d);
		assertEquals(null, o.f);
		assertEquals(null, o.i);
		assertEquals(null, o.l);
		assertEquals(null, o.s);

		String[] osArgs = "-b -B 1 -c a load -d 0.1 -F 0.2 -i -010 -l 0xf -S -32768 con".split("\\s+");
		Map<Object, String[]> result = parser.parse(osArgs, true);
		String[] args = result.get(c);

		assertTrue(p.b);
		assertTrue(p.bt == 1);
		assertTrue(p.c == 'a');

		assertTrue(o.d == 0.1d);
		assertTrue(o.f == 0.2f);
		assertTrue(o.i == -8);
		assertTrue(o.l == 15L);
		assertTrue(o.s == Short.MIN_VALUE);

		assertEquals(0, args.length);
	}

}
