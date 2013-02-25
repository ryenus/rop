package thirdpty.cmd;

import java.util.Calendar;

import org.ryez.OptionParser.Command;
import org.ryez.OptionParser.Option;

@Command(name = "con", description = "")
public class UnsupportedType {

	@Option(description = "", opt = { "-b", "--boolean" })
	boolean b;

	@Option(description = "", opt = { "-c", "--cal" })
	Calendar cal;
}
