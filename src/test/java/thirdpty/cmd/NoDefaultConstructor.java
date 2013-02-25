package thirdpty.cmd;

import org.ryez.OptionParser.Command;
import org.ryez.OptionParser.Option;

@Command(name = "con", descriptions = "")
public class NoDefaultConstructor {

	@Option(description = "", opt = { "-b", "--boolean" })
	boolean b;

	private NoDefaultConstructor(boolean bool) {
	}
}
