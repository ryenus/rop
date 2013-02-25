package thirdpty.cmd;

import org.ryez.OptionParser.Command;
import org.ryez.OptionParser.Option;

@Command(descriptions = "A bad command with duplicated opt keys", name = "")
public class DuplicateOptionKeys {
	@Option(opt = "-k", description = "A field using option key '-k'")
	boolean opt1;
	@Option(opt = "-k", description = "Another field using option key '-k' again")
	boolean opt2;
}
