package thirdpty.test;

import com.github.ryenus.rop.OptionParser.Command;
import com.github.ryenus.rop.OptionParser.Option;

@Command(name = "passwd", descriptions = "test password input")
class PasswordInput {
	@Option(opt = "-p", secret = true, description = "safety check")
	char[] password;
}