package thirdpty.test.negative;

import com.github.ryenus.rop.OptParseException;
import com.github.ryenus.rop.OptionParser;
import org.junit.Test;
import thirdpty.test.PrivateConstructor;
import thirdpty.test.RunnableCommand;

import static org.junit.Assert.assertEquals;

public class NegativeCasesTest {
	private OptionParser parser;

	@Test(expected = OptParseException.class)
	public void noKeyOption() {
		parser = new OptionParser(OptionNoKey.class);
	}

	@Test(expected = OptParseException.class)
	public void unknownOption() {
		parser = new OptionParser(PrivateConstructor.class);
		String[] args = parser.parse("--unknown".split("\\s+")).get(PrivateConstructor.class);
		assertEquals(0, args.length);
	}

	@Test(expected = OptParseException.class)
	public void missingOptArg() {
		parser = new OptionParser(PrivateConstructor.class);
		parser.parse("--int".split("\\s+"));
	}

	@Test(expected = OptParseException.class)
	public void badOptArg() {
		parser = new OptionParser(PrivateConstructor.class);
		parser.parse("-i str".split("\\s+"));
	}

	@Test(expected = OptParseException.class)
	public void badOptType() {
		parser = new OptionParser(UnsupportedType.class);
		parser.parse("-c str".split("\\s+"));
	}

	@Test(expected = OptParseException.class)
	public void duplicateOptKey() {
		parser = new OptionParser(DuplicateOptionKeys.class);
	}

	@Test(expected = OptParseException.class)
	public void requiredOptNotSet() {
		parser = new OptionParser(RunnableCommand.class);
		parser.parse("-b -i 1".split("\\s+"));
	}

	@Test(expected = OptParseException.class)
	public void commandMissing() {
		parser = new OptionParser();
		parser.parse("-- -b".split("\\s+"));
	}

	@Test(expected = OptParseException.class)
	public void bareCommand() {
		parser = new OptionParser(BareCommand.class);
	}

	@Test(expected = OptParseException.class)
	public void hiddenRequired() {
		parser = new OptionParser(HiddenRequiredOption.class);
	}
}
