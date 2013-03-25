package thirdpty.test.negative;

import com.github.ryenus.rop.OptionParser.Command;
import com.github.ryenus.rop.OptionParser.Option;

@Command(name = "")
public class HiddenRequiredOption {

	@Option(description = "", opt = { "-x" }, hidden = true, required = true)
	boolean invisible;
}
