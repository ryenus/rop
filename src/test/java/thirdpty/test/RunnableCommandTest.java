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
		parser = new OptionParser(BareOption.class, SimpleRun.class);
	}

	@Test
	public void runMultipleSub() {
		SimpleRun r0 = new SimpleRun();
		RunnableCommand r1 = new RunnableCommand();
		RunnableCommand2 r2 = new RunnableCommand2();
		RunnableCommand3 r3 = new RunnableCommand3();
		RunnableCommand4 r4 = new RunnableCommand4();
		parser = new OptionParser(r0, r1, r2, r3, r4);

		String[] osArgs = "run a run1 -bx b run2 c run3 run4 -i 1".split("\\s+");
		parser.parse(osArgs, true);
		assertArrayEquals(new String[] { "run", "a" }, r0.params);
		assertArrayEquals(new String[] {"b"}, r1.params);
		assertArrayEquals(new String[] {"c"}, r2.params);
		assertEquals(parser, r3.parser);
		assertEquals(true, r4.set);
	}

	@Test
	public void runOnlyOneSub() {
		SimpleRun r0 = new SimpleRun();
		RunnableCommand r1 = new RunnableCommand();
		RunnableCommand2 r2 = new RunnableCommand2();
		RunnableCommand3 r3 = new RunnableCommand3();
		RunnableCommand4 r4 = new RunnableCommand4();
		parser = new OptionParser(r0, r1, r2, r3, r4);

		String[] osArgs = "run a run1 -bx b run2 c run3 run4 -i 1".split("\\s+");
		parser.parse(osArgs);
		assertArrayEquals(new String[] { "run", "a" }, r0.params);
		assertEquals(true, r1.b);
		assertEquals(true, r1.x);
		assertEquals(10, r1.i);
		assertArrayEquals(new String[] { "b", "run2", "c", "run3", "run4" }, r1.params);
		assertArrayEquals(null, r2.params);
		assertEquals(null, r3.parser);
		assertEquals(false, r4.set);
	}

	@Test
	public void runOnlyOneSub2() {
		SimpleRun r0 = new SimpleRun();
		RunnableCommand r1 = new RunnableCommand();
		RunnableCommand2 r2 = new RunnableCommand2();
		RunnableCommand3 r3 = new RunnableCommand3();
		RunnableCommand4 r4 = new RunnableCommand4();
		parser = new OptionParser(r0, r1, r2, r3, r4);

		String[] osArgs = "run a run1 -bx b run2 c run3 run4 -i 1".split("\\s+");
		parser.parse(osArgs, false);
		assertArrayEquals(new String[] { "run", "a" }, r0.params);
		assertEquals(true, r1.b);
		assertEquals(true, r1.x);
		assertEquals(10, r1.i);
		assertArrayEquals(new String[] { "b", "run2", "c", "run3", "run4" }, r1.params);
		assertArrayEquals(null, r2.params);
		assertEquals(null, r3.parser);
		assertEquals(false, r4.set);
	}
}

@Command(name = "run", descriptions = "")
class SimpleRun {
	String[] params;

	void run(OptionParser parser, String[] params) {
		this.params = params;
	}
}

@Command(name = "run1", descriptions = "")
class RunnableCommand {
	String[] params;

	@Option(description = "", opt = { "-b", "--boolean" }, required = true)
	boolean b;

	@Option(description = "", opt = { "-x", "--debug" }, required = true)
	boolean x;

	@Option(description = "", opt = { "-i", "--int" })
	int i = 9;

	int xc = 0;

	void run(OptionParser parser, String[] params) {
		i = 10;
		this.params = params;
		xc++;
	}
}

@Command(name = "run2", descriptions = "")
class RunnableCommand2 {
	String[] params;

	void run(String[] params) {
		this.params = params;
	}
}

@Command(name = "run3", descriptions = "")
class RunnableCommand3 {
	OptionParser parser;

	void run(OptionParser parser) {
		this.parser = parser;
	}
}

@Command(name = "run4", descriptions = "")
class RunnableCommand4 {
	boolean set;

	@Option(description = "", opt = { "-i", "--int" }, required = true)
	int i = 9;

	void run() {
		set = true;
	}
}
