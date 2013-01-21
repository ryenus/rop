package thirdpty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Test;
import org.ryez.OptionParser;
import org.ryez.OptionParser.Command;
import org.ryez.OptionParser.Option;

public class OptionParserTest {
	private OptionParser parser;

	@Test(expected = RuntimeException.class)
	public void annotationMissing() {
		parser = new OptionParser(AnnotationMissing.class);
	}

	@Test
	public void privateConstructor() {
		parser = new OptionParser(PrivateConstructor.class);
		parser = new OptionParser(new Object[] { PrivateConstructor.class });
		parser = new OptionParser(Arrays.asList(new Object[] { PrivateConstructor.class }));
	}

	@Test
	public void primitiveFields() {
		parser = new OptionParser(PrimitiveFields.class);
		PrimitiveFields p = parser.get(PrimitiveFields.class);

		assertFalse(p.b);
		assertFalse(p.bt == 1);
		assertFalse(p.c == 'a');
		assertFalse(p.d == 0.1d);
		assertFalse(p.f == 0.2f);
		assertFalse(p.i == 8);
		assertFalse(p.l == 15L);
		assertFalse(p.s == 32767);

		String[] args = parser.parse("--boolean --byte 1 --char a --double 0.1 --float 0.2 --int 010 --long 0xf --short -32768".split("\\s+"));

		assertTrue(p.b);
		assertTrue(p.bt == 1);
		assertTrue(p.c == 'a');
		assertTrue(p.d == 0.1d);
		assertTrue(p.f == 0.2f);
		assertTrue(p.i == 8);
		assertTrue(p.l == 15L);
		assertTrue(p.s == Short.MIN_VALUE);
		assertEquals(0, args.length);
	}

	@Test
	public void objectFields() {
		ObjectFields o = new ObjectFields();
		parser = new OptionParser(o);

		assertEquals(null, o.b);
		assertEquals(null, o.bt);
		assertEquals(null, o.c);
		assertEquals(null, o.d);
		assertEquals(null, o.f);
		assertEquals(null, o.i);
		assertEquals(null, o.l);
		assertEquals(null, o.s);
		assertEquals(null, o.str);
		assertEquals(null, o.file);
		assertEquals(null, o.path);

		String[] args = parser.parse("-b -B 1 -c a -d 0.1 -F 0.2 -i 010 -l 0xf -S -32768 -s abc -f src -p src/main".split("\\s+"));

		assertTrue(o.b);
		assertTrue(o.bt == 1);
		assertTrue(o.c == 'a');
		assertTrue(o.d == 0.1d);
		assertTrue(o.f == 0.2f);
		assertTrue(o.i == 8);
		assertTrue(o.l == 15L);
		assertEquals("abc", o.str);
		assertEquals(new File("src"), o.file);
		assertEquals(Paths.get("src", "main"), o.path);

		assertEquals(0, args.length);
	}
}

class AnnotationMissing { // don't do this
}

@Command(description = "")
class PrivateConstructor {
	private PrivateConstructor() { // this is fine
	}
}

@Command(description = "")
class PrimitiveFields {
	@Option(description = "", opt = { "-b", "--boolean" })
	boolean b;
	@Option(description = "", opt = { "-B", "--byte" })
	byte bt;
	@Option(description = "", opt = { "-c", "--char" })
	char c;
	@Option(description = "", opt = { "-d", "--double" })
	double d;
	@Option(description = "", opt = { "-F", "--float" })
	float f;
	@Option(description = "", opt = { "-i", "--int" })
	int i;
	@Option(description = "", opt = { "-l", "--long" })
	long l;
	@Option(description = "", opt = { "-S", "--short" })
	short s;
}

@Command(description = "")
class ObjectFields {
	@Option(description = "", opt = { "-b", "--boolean" })
	Boolean b;
	@Option(description = "", opt = { "-B", "--byte" })
	Byte bt;
	@Option(description = "", opt = { "-c", "--char" })
	Character c;
	@Option(description = "", opt = { "-i", "--int" })
	Integer i;
	@Option(description = "", opt = { "-l", "--long" })
	Long l;
	@Option(description = "", opt = { "-F", "--float" })
	Float f;
	@Option(description = "", opt = { "-d", "--double" })
	Double d;
	@Option(description = "", opt = { "-S", "--short" })
	Short s;
	@Option(description = "", opt = { "-s", "--string" })
	String str;
	@Option(description = "", opt = { "-f", "--file" })
	File file;
	@Option(description = "", opt = { "-p", "--path" })
	Path path;
}
