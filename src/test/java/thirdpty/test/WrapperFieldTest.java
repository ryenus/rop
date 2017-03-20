package thirdpty.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.Test;

import com.github.ryenus.optj.OptionParser;


public class WrapperFieldTest {

	private OptionParser parser;

	@Test
	public void objectFields() {
		Wrappers w = new Wrappers();
		parser = new OptionParser(w);

		assertEquals(null, w.b);
		assertEquals(null, w.bt);
		assertEquals(null, w.c);
		assertEquals(null, w.d);
		assertEquals(null, w.f);
		assertEquals(null, w.i);
		assertEquals(null, w.l);
		assertEquals(null, w.s);
		assertEquals(null, w.o);
		assertEquals(null, w.str);
		assertEquals(null, w.file);
		assertEquals(null, w.path);

		Map<Object, String[]> result;
		String[] args = "+b -B 1 -c a -d 0.1 -F 0.2 -i 010 -l 0xf -S -32768 -o obj -s abc -f src -p src/main".split("\\s+");
		result = parser.parse(args);
		String[] params = result.get(w);

		assertTrue(w.b == Boolean.FALSE);
		assertTrue(w.bt == 1);
		assertTrue(w.c == 'a');
		assertTrue(w.d == 0.1d);
		assertTrue(w.f == 0.2f);
		assertTrue(w.i == 8);
		assertTrue(w.l == 15L);
		assertTrue(w.s == Short.MIN_VALUE);
		assertEquals("obj", w.o);
		assertEquals("abc", w.str);
		assertEquals(new File("src"), w.file);
		assertEquals(Paths.get("src", "main"), w.path);

		assertEquals(0, params.length);
	}
}
