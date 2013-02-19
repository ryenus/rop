package thirdpty.test;

import static org.junit.Assert.assertEquals;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.Permission;

import org.junit.Test;
import org.ryez.OptionParser;

import thirdpty.cmd.BareCommand;
import thirdpty.cmd.BareOption;
import thirdpty.cmd.PrivateConstructor;
import thirdpty.cmd.UnsupportedType;

public class OptionParserTest {

	private OptionParser parser;

	@Test
	public void commandMissing() {
		parser = new OptionParser();
		String[] args = parser.parse("-- -b".split("\\s+"));
		assertEquals(2, args.length);
		assertEquals("--", args[0]);
		assertEquals("-b", args[1]);
	}

	@Test(expected = RuntimeException.class)
	public void showHelp() {
		parser = new OptionParser(PrivateConstructor.class);

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
		System.setOut(new PrintStream(new BufferedOutputStream(baos)));

		try {
			parser.parse("--help -b".split("\\s+"));
		} catch (RuntimeException e) {
			assertEquals("0", e.getMessage());
			throw e;
		} finally {
			System.setSecurityManager(null);
			try {
				baos.flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			System.err.println(baos.toString());
		}
	}

	@Test(expected = RuntimeException.class)
	public void bareCommand() {
		parser = new OptionParser(BareCommand.class);
	}

	@Test(expected = RuntimeException.class)
	public void bareOption() {
		parser = new OptionParser(BareOption.class);
	}

	@Test(expected = RuntimeException.class)
	public void unknownOption() {
		parser = new OptionParser(PrivateConstructor.class);
		String[] args = parser.parse("--unknown".split("\\s+"));
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
}
