package thirdpty.test.negative;

import com.github.ryenus.rop.OptParseException;
import com.github.ryenus.rop.OptionParser;
import com.github.ryenus.rop.OptionParser.Command;
import org.junit.Test;

public class NonStaticInnerClass {

	@Test(expected = OptParseException.class)
	public void test() {
		new OptionParser(NonStaticInnerClassCommand.class);
	}

	@Command(name = "")
	class NonStaticInnerClassCommand {
		// doesn't matter if option fields are defined.
	}
}
