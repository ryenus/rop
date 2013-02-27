package thirdpty.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ryez.OptionParser;
import org.ryez.OptionParser.Command;
import org.ryez.OptionParser.Option;

public class UglyCommandTest {

	private OptionParser parser;

	@Test
	public void uglyOpt() {
		UglyCommand u = new UglyCommand();
		parser = new OptionParser(u);
		assertEquals(0, u.i);
		assertEquals(0, u.l);
		parser.parse("-int 8 --long-value 2".split("\\s+"));
		assertEquals(8, u.i);
		assertEquals(2, u.l);
	}
}

@Command(name = "", descriptions = "")
class UglyCommand {
	@Option(description = "", opt = { "-int" })
	int i;

	@Option(description = "", opt = { "--long-value" })
	long l;
}
