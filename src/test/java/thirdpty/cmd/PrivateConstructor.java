package thirdpty.cmd;

import org.ryez.OptionParser.Command;
import org.ryez.OptionParser.Option;

@Command(name = "con", descriptions = " A command to demo that a Command with private no-arg constructor is ok, even if you cannot directly instantiate it yourself")
public class PrivateConstructor {

	@Option(description = "explain what is being done, this is for a command to demo that a Command with private no-arg constructor is ok, even if you cannot directly instantiate it yourself", opt = {
		"-b", "--boolean" })
	public boolean b;

	@Option(description = "certain number", opt = { "-i", "--int" })
	public int i;

	@Option(description = "certain flag", opt = { "--abcdefghijklmnopqrstuvwxyz0123456" })
	public boolean x;

	@Option(description = "another flag", opt = { "-m" })
	public boolean y;

	private PrivateConstructor() { // this is fine
	}
}
