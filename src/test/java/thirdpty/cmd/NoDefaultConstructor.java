package thirdpty.cmd;

import com.github.ryenus.rop.OptionParser.Command;
import com.github.ryenus.rop.OptionParser.Option;

@Command(name = "con", descriptions = "")
public class NoDefaultConstructor {

	@Option(description = "", opt = { "-b", "--boolean" })
	boolean b;

	private NoDefaultConstructor(boolean bool) {
	}
}
