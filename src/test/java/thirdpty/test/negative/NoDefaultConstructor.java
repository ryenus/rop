package thirdpty.test.negative;

import com.github.ryenus.optj.Command;
import com.github.ryenus.optj.Option;

@Command(name = "con", descriptions = "")
public class NoDefaultConstructor {

	@Option(description = "", opt = { "-b", "--boolean" })
	boolean b;

	private NoDefaultConstructor(boolean bool) {
	}
}
