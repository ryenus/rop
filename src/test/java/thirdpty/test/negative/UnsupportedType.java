package thirdpty.test.negative;

import java.util.Calendar;

import com.github.ryenus.rop.OptionParser.Command;
import com.github.ryenus.rop.OptionParser.Option;

@Command(name = "con", descriptions = "")
public class UnsupportedType {

	@Option(description = "", opt = { "-b", "--boolean" })
	boolean b;

	@Option(description = "", opt = { "-c", "--cal" })
	Calendar cal;
}
