package thirdpty.test;

import org.ryez.OptionParser;
import org.ryez.OptionParser.Command;
import org.ryez.OptionParser.Option;

@Command(name = "run", description = "")
public class RunnableCommand {

	@Option(description = "", opt = { "-b", "--boolean" })
	boolean b;

	@Option(description = "", opt = { "-x", "--debug" })
	boolean x;

	@Option(description = "", opt = { "-i", "--int" })
	int i = 9;

	void run(OptionParser parser) {
		i = 10;
	}
}
