package thirdpty.cmd;

import org.ryez.OptionParser.Command;
import org.ryez.OptionParser.Option;

@Command(name = "con", description = "A command to demo that a Command with private no-arg constructor is ok")
public class PrivateConstructor {

	@Option(description = "", opt = { "-b", "--boolean" })
	public boolean b;

	@Option(description = "", opt = { "-i", "--int" })
	public int i;

	private PrivateConstructor() { // this is fine
	}
}
