package thirdpty.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ryez.OptionParser;
import org.ryez.OptionParser.Command;

import thirdpty.cmd.PrivateConstructor;

public class SubCommandTest {

	private OptionParser parser;

	@Test
	public void subcommands() {
		parser = new OptionParser(Primitives.class, Wrappers.class, PrivateConstructor.class);
		Primitives p = parser.get(Primitives.class);
		Wrappers o = parser.get(Wrappers.class);
		PrivateConstructor c = parser.get(PrivateConstructor.class);

		assertFalse(p.b);
		assertFalse(p.bt == 1);
		assertFalse(p.c == 'a');

		assertEquals(null, o.d);
		assertEquals(null, o.f);
		assertEquals(null, o.i);
		assertEquals(null, o.l);
		assertEquals(null, o.s);

		String[] args = parser.parse("-b -B 1 -c a load -d 0.1 -F 0.2 con -i -010 -l 0xf -S -32768".split("\\s+"));

		assertTrue(p.b);
		assertTrue(p.bt == 1);
		assertTrue(p.c == 'a');

		assertTrue(o.d == 0.1d);
		assertTrue(o.f == 0.2f);
		assertTrue(o.i == -8);
		assertTrue(o.l == 15L);
		assertTrue(o.s == Short.MIN_VALUE);

		// command c is taken as a plain parameter, as it comes at 3rd in the
		// args
		assertEquals(1, args.length);
		assertEquals(c.getClass().getAnnotation(Command.class).name(), args[0]);
	}

}
