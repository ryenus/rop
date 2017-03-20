package thirdpty.test.negative;

import com.github.ryenus.optj.Command;
import com.github.ryenus.optj.Option;

@Command(name = "")
public class HiddenRequiredOption {

	@Option(description = "", opt = { "-x" }, hidden = true, required = true)
	boolean invisible;
}
