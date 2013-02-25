package thirdpty.test;

import org.ryez.OptionParser.Command;
import org.ryez.OptionParser.Option;

@Command(name = "add", description = "The sub-command to demo all the supported primitive data types")
class Primitives {
	@Option(description = "a bool flag", opt = { "-b", "--boolean" })
	boolean b;

	@Option(description = "[-128, 127]", opt = { "-B", "--byte" })
	byte bt;

	@Option(description = "[\\u0000, \\u65535]", opt = { "-c", "--char" })
	char c;

	@Option(description = "+[4.9e-324, 1.7976931348623157e+308]", opt = { "-d", "--double" })
	double d;

	@Option(description = "+[1.4e-45f, 3.4028235e+38f]", opt = { "-F", "--float" })
	float f;

	@Option(description = "[0x80000000, 0x7fffffff]", opt = { "-i", "--int" })
	int i;

	@Option(description = "[0x8000000000000000L, 0x7fffffffffffffffL] // -2^63 ~ 2^63 - 1", opt = { "-l", "--long" })
	long l;

	@Option(description = "[-32768, 32767]", opt = { "-S", "--short" })
	short s;
}
