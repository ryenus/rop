package thirdpty.test;

import com.github.ryenus.rop.OptParseException;
import com.github.ryenus.rop.OptionParser;
import org.junit.Test;
import thirdpty.test.negative.NoDefaultConstructor;

import java.util.Arrays;

public class CommandConstructorTest {

	@SuppressWarnings("unused")
	private OptionParser parser;

	@Test
	public void privateConstructor() {
		parser = new OptionParser(PrivateConstructor.class);
		parser = new OptionParser(new Object[] { PrivateConstructor.class });
		parser = new OptionParser(Arrays.asList(new Object[] { PrivateConstructor.class }));
	}

	@Test(expected = OptParseException.class)
	public void badConstructor() {
		parser = new OptionParser(NoDefaultConstructor.class);
	}
}
