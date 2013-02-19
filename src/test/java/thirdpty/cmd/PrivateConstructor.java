package thirdpty.cmd;

import org.ryez.OptionParser.Command;
import org.ryez.OptionParser.Option;

@Command(name = "con", description = "A command to demo that a Command with private no-arg constructor is ok")
public class PrivateConstructor {

	@Option(description = "", opt = { "-b", "--boolean" })
	boolean b;

	@Option(description = "", opt = { "-i", "--int" })
	int i;

	private PrivateConstructor() { // this is fine
	}
}
