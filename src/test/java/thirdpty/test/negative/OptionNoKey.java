package thirdpty.test.negative;

import com.github.ryenus.rop.OptionParser.Command;
import com.github.ryenus.rop.OptionParser.Option;

@Command(name = "foo", descriptions = "")
public class OptionNoKey {
	@Option(opt = {}, description = "")
	boolean verbose;
}