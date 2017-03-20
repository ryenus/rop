package thirdpty.test;

import com.github.ryenus.optj.Command;
import com.github.ryenus.optj.Option;

@Command(name = "passwd", descriptions = "test password input")
class PasswordInput {
	@Option(opt = "-p", secret = true, description = "safety check")
	char[] password;
}