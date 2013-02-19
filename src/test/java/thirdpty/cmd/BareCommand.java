package thirdpty.cmd;

import org.ryez.OptionParser.Option;

// A bad Command, as it doesn't have the @Command annotation.
public class BareCommand {
	@Option(description = "", opt = { "-i", "--int" })
	int i;
}
