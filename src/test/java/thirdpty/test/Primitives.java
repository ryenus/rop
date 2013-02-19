package thirdpty.test;

import org.ryez.OptionParser.Command;
import org.ryez.OptionParser.Option;

@Command(name = "add", description = "")
class Primitives {
	@Option(description = "", opt = { "-b", "--boolean" })
	boolean b;

	@Option(description = "", opt = { "-B", "--byte" })
	byte bt;

	@Option(description = "", opt = { "-c", "--char" })
	char c;

	@Option(description = "", opt = { "-d", "--double" })
	double d;

	@Option(description = "", opt = { "-F", "--float" })
	float f;

	@Option(description = "", opt = { "-i", "--int" })
	int i;

	@Option(description = "", opt = { "-l", "--long" })
	long l;

	@Option(description = "", opt = { "-S", "--short" })
	short s;
}
