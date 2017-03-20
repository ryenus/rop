package thirdpty.test.negative;

import com.github.ryenus.optj.Command;
import com.github.ryenus.optj.Option;

@Command(name = "foo", descriptions = "")
public class OptionNoKey {
	@Option(opt = {}, description = "")
	boolean verbose;
}