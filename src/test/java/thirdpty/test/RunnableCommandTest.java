package thirdpty.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ryez.OptionParser;
import org.ryez.OptionParser.Command;
import org.ryez.OptionParser.Option;

public class RunnableCommandTest {

	private OptionParser parser;

	@Test
	public void callingRun() {
		RunnableCommand r = new RunnableCommand();
		parser = new OptionParser(r);

		assertEquals(9, r.i); // default value is 9
		assertEquals(false, r.b);
		assertEquals(false, r.x);

		parser.parse("-bx a b c".split("\\s+"));

		// r.run was automatically invoked, which set r.i to 10
		assertEquals(10, r.i);
		assertEquals(true, r.b);
		assertEquals(true, r.x);
		assertArrayEquals(new String[] { "a", "b", "c" }, r.params);
	}
}

@Command(name = "run", descriptions = "")
class RunnableCommand {

	String[] params;

	@Option(description = "", opt = { "-b", "--boolean" })
	boolean b;

	@Option(description = "", opt = { "-x", "--debug" })
	boolean x;

	@Option(description = "", opt = { "-i", "--int" })
	int i = 9;

	void run(OptionParser parser, String[] remainArgs) {
		i = 10;
		params = remainArgs;
	}
}
