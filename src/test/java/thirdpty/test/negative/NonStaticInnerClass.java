package thirdpty.test.negative;

import org.junit.Test;

import com.github.ryenus.optj.OptionParser;
import com.github.ryenus.optj.Command;

public class NonStaticInnerClass {

	@Test(expected = RuntimeException.class)
	public void test() {
		new OptionParser(NonStaticInnerClassCommand.class);
	}

	@Command(name = "")
	class NonStaticInnerClassCommand {
		// doesn't matter if option fields are defined.
	}
}
