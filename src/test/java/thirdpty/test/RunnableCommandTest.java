package thirdpty.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ryez.OptionParser;
import org.ryez.OptionParser.Command;
import org.ryez.OptionParser.Option;

import thirdpty.cmd.BareOption;

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

	@Test(expected = RuntimeException.class)
	public void duplicateName() {
		parser = new OptionParser(BareOption.class, AnotherRunCommand.class);
	}

	@Test
	public void callingMultipleRun() {
		AnotherRunCommand r0 = new AnotherRunCommand();
		RunnableCommand r1 = new RunnableCommand();
		RunnableCommand2 r2 = new RunnableCommand2();
		parser = new OptionParser(r0, r1, r2);
		parser.parse("a run1 -bx b run2 c".split("\\s+"));
		assertArrayEquals(new String[] {"a"}, r0.params);
		assertArrayEquals(new String[] {"b"}, r1.params);
		assertArrayEquals(new String[] {"c"}, r2.params);
	}
}

@Command(name = "run", descriptions = "")
class AnotherRunCommand {
	String[] params;

	void run(OptionParser parser, String[] remains) {
		params = remains;
	}
}

@Command(name = "run1", descriptions = "")
class RunnableCommand {

	String[] params;

	@Option(description = "", opt = { "-b", "--boolean" })
	boolean b;

	@Option(description = "", opt = { "-x", "--debug" })
	boolean x;

	@Option(description = "", opt = { "-i", "--int" })
	int i = 9;

	void run(OptionParser parser, String[] remains) {
		i = 10;
		params = remains;
	}
}


@Command(name = "run2", descriptions = "")
class RunnableCommand2 {

	String[] params;

	void run(OptionParser parser, String[] remains) {
		params = remains;
	}
}
