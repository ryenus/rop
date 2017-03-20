package thirdpty.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.Permission;

import org.junit.Test;

import com.github.ryenus.optj.OptionParser;

public class OptionParserTest {
	private OptionParser parser;

	public static void main(String[] args) {
		new OptionParser(PasswordInput.class).parse(args.length == 0 ? new String[] { "-p" } : args);
	}

	@Test
	public void forcedArgs() {
		parser = new OptionParser().register(PrivateConstructor.class);
		PrivateConstructor p = parser.get(PrivateConstructor.class);
		assertFalse(p.b);
		assertEquals(0, p.i);

		String[] args = parser.parse("-b -- -i".split("\\s+")).get(p);

		assertTrue(p.b);
		assertEquals(1, args.length);
		assertEquals("-i", args[0]);
		assertEquals(0, p.i);
	}

	@Test
	public void escapedOption() {
		parser = new OptionParser(PrivateConstructor.class);
		String[] args = parser.parse("-b \\-i".split("\\s+")).get(parser.get(PrivateConstructor.class));
		assertEquals(1, args.length);
		assertEquals("-i", args[0]);
	}

	@Test
	public void showHelp() {
		parser = new OptionParser(Primitives.class, PrivateConstructor.class).register(new Wrappers());
		SecurityManager secMan = System.getSecurityManager();
		System.setSecurityManager(new SecurityManager() {
			@Override
			public void checkExit(int status) {
				throw new RuntimeException(String.valueOf(status));
			}

			@Override
			public void checkPermission(Permission perm) {
			}
		});

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream stdout = System.out;
		System.setOut(new PrintStream(baos));

		try {
			parser.parse("--help -b".split("\\s+"));
		} catch (RuntimeException e) {
			assertEquals("0", e.getMessage()); // caught exit code
		}

		System.setOut(stdout);
		System.setSecurityManager(secMan);
		System.err.println(baos.toString());

		try {
			URI uri = getClass().getClassLoader().getResource("help.out").toURI();
			byte[] bytes = Files.readAllBytes(new File(uri).toPath());
			assertArrayEquals(bytes, baos.toByteArray());
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void inputPassword() {
		InputStream stdin = System.in;
		try {
			PasswordInput pi = new PasswordInput();
			parser = new OptionParser(pi);
			System.setIn(new ByteArrayInputStream("topSecret".getBytes()));
			parser.parse(new String[] { "-p" });
			assertArrayEquals("topSecret".toCharArray(), pi.password);
		} finally {
			System.setIn(stdin);
		}
	}

	@Test
	public void dupCommandAsParam() {
		RunnableCommand r = new RunnableCommand();
		parser = new OptionParser(r);
		parser.parse("-b run1 -x -i 1".split("\\s+"));
		assertEquals(true, r.b);
		assertEquals(true, r.x);
		assertEquals(1, r.xc);
		assertArrayEquals(new String[] { "run1" }, r.params);
	}

	@Test
	public void bareOption() {
		parser = new OptionParser(BareOption.class);
	}
}
