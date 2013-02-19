package thirdpty.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ryez.OptionParser;

public class RunnableCommandTest {

	private OptionParser parser;

	@Test
	public void callingRun() {
		RunnableCommand r = new RunnableCommand();
		parser = new OptionParser(r);

		assertEquals(9, r.i); // default value is 9
		assertEquals(false, r.b);
		assertEquals(false, r.x);

		parser.parse("-bx".split("\\s+"));

		// r.run was automatically invoked, which set r.i to 10
		assertEquals(10, r.i);
		assertEquals(true, r.b);
		assertEquals(true, r.x);

	}
}
