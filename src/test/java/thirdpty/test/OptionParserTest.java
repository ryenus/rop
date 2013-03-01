package thirdpty.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.Permission;

import org.junit.Test;
import org.ryez.OptionParser;

import thirdpty.cmd.BareCommand;
import thirdpty.cmd.BareOption;
import thirdpty.cmd.DuplicateOptionKeys;
import thirdpty.cmd.PrivateConstructor;
import thirdpty.cmd.UnsupportedType;

public class OptionParserTest {

	private OptionParser parser;

	@Test
	public void forcedArgs() {
		parser = new OptionParser(PrivateConstructor.class);
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
		parser = new OptionParser(Primitives.class, PrivateConstructor.class, Wrappers.class);
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
		System.setOut(new PrintStream(baos));

		try {
			parser.parse("--help -b".split("\\s+"));
		} catch (RuntimeException e) {
			assertEquals("0", e.getMessage()); // caught exit code
		} finally {
			System.setSecurityManager(secMan);
			try {
				URI uri = this.getClass().getClassLoader().getResource("help.out").toURI();
				byte[] bytes = Files.readAllBytes(new File(uri).toPath());
				assertArrayEquals(bytes, baos.toByteArray());
			} catch (IOException | URISyntaxException e) {
				throw new RuntimeException(e);
			}
			System.err.println(baos.toString());
		}
	}

	@Test
	public void bareOption() {
		parser = new OptionParser(BareOption.class);
	}

	@Test(expected = RuntimeException.class)
	public void commandMissing() {
		parser = new OptionParser();
		parser.parse("-- -b".split("\\s+"));
	}

	@Test(expected = RuntimeException.class)
	public void bareCommand() {
		parser = new OptionParser(BareCommand.class);
	}

	@Test(expected = RuntimeException.class)
	public void unknownOption() {
		parser = new OptionParser(PrivateConstructor.class);
		String[] args = parser.parse("--unknown".split("\\s+")).get(PrivateConstructor.class);
		assertEquals(0, args.length);
	}

	@Test(expected = RuntimeException.class)
	public void missingOptArg() {
		parser = new OptionParser(PrivateConstructor.class);
		parser.parse("--int".split("\\s+"));
	}

	@Test(expected = RuntimeException.class)
	public void badOptArg() {
		parser = new OptionParser(PrivateConstructor.class);
		parser.parse("-i str".split("\\s+"));
	}

	@Test(expected = RuntimeException.class)
	public void badOptType() {
		parser = new OptionParser(UnsupportedType.class);
		parser.parse("-c str".split("\\s+"));
	}

	@Test(expected = RuntimeException.class)
	public void duplicateOptKey() {
		parser = new OptionParser(DuplicateOptionKeys.class);
	}
}
