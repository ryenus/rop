package thirdpty.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.ryenus.optj.OptionParser;
import com.github.ryenus.optj.Command;
import com.github.ryenus.optj.Option;


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
		RunnableCommand5 r5 = new RunnableCommand5();
		parser = new OptionParser(r0, r1, r2, r3, r4, r5);

		String[] osArgs = "run a run1 -bx b run2 c run3 run4 -i 1 run5".split("\\s+");
		parser.parse(osArgs, false);
		assertEquals(parser, r1.parser);
		assertEquals(true, r1.b);
		assertEquals(true, r1.x);
		assertEquals(10, r1.i);
		assertArrayEquals(new String[] { "run", "a" }, r0.params);
		assertArrayEquals(new String[] { "b", "run2", "c", "run3", "run4", "run5" }, r1.params);
		assertArrayEquals(null, r2.params);
		assertEquals(null, r3.parser);
		assertEquals(false, r4.set);
		assertEquals(false, r5.set);

		parser.parse(osArgs, true);
		assertArrayEquals(new String[] { "run", "a" }, r0.params);
		assertEquals(true, r1.b);
		assertEquals(true, r1.x);
		assertEquals(10, r1.i);
		assertArrayEquals(new String[] { "b" }, r1.params);
		assertArrayEquals(new String[] { "c" }, r2.params);
		assertEquals(parser, r3.parser);
		assertEquals(true, r4.set);
		assertArrayEquals(new String[] {}, r5.params);
		assertEquals(parser, r5.parser);
		assertEquals(true, r5.set);
	}
}

@Command(name = "run", descriptions = "")
class SimpleRun {
	String[] params;

	void run(OptionParser parser, String[] params) {
		this.params = params;
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

@Command(name = "run5", descriptions = "")
class RunnableCommand5 {
	boolean set;
	String[] params;
	OptionParser parser;

	@Option(description = "", opt = { "-i", "--int" })
	int i = 9;


	void run(String[] params, OptionParser parser) {
		this.params = params;
		this.parser = parser;
		set = true;
	}
}
