package thirdpty.test;

import org.ryez.OptionParser.Command;
import org.ryez.OptionParser.Option;

@Command(name = "", description = "")
public class UglyCommand {
	@Option(description = "", opt = { "-int" })
	int i;

	@Option(description = "", opt = { "--long-value" })
	long l;
}