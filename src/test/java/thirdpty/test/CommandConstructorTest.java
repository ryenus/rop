package thirdpty.test;

import java.util.Arrays;

import org.junit.Test;

import thirdpty.test.negative.NoDefaultConstructor;

import com.github.ryenus.optj.OptionParser;

public class CommandConstructorTest {

	@SuppressWarnings("unused")
	private OptionParser parser;

	@Test
	public void privateConstructor() {
		parser = new OptionParser(PrivateConstructor.class);
		parser = new OptionParser(new Object[] { PrivateConstructor.class });
		parser = new OptionParser(Arrays.asList(new Object[] { PrivateConstructor.class }));
	}

	@Test(expected = RuntimeException.class)
	public void badConstructor() {
		parser = new OptionParser(NoDefaultConstructor.class);
	}
}
