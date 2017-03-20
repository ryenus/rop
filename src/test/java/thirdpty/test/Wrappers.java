package thirdpty.test;

import java.io.File;
import java.nio.file.Path;

import com.github.ryenus.optj.Command;
import com.github.ryenus.optj.Option;

@Command(name = "load", descriptions = "The command to demo all the supported wrapper types")
public class Wrappers {
	@Option(description = "", opt = { "-b", "--boolean" })
	Boolean b;

	@Option(description = "", opt = { "-B", "--byte" })
	Byte bt;

	@Option(description = "", opt = { "-c", "--char" })
	Character c;

	@Option(description = "", opt = { "-i", "--int" })
	Integer i;

	@Option(description = "", opt = { "-l", "--long" })
	Long l;

	@Option(description = "", opt = { "-F", "--float" })
	Float f;

	@Option(description = "", opt = { "-d", "--double" })
	Double d;

	@Option(description = "", opt = { "-S", "--short" })
	Short s;

	@Option(description = "", opt = { "-o", "--object" })
	Object o;

	@Option(description = "", opt = { "-s", "--string" })
	String str;

	@Option(description = "", opt = { "-f", "--file" })
	File file;

	@Option(description = "", opt = { "-p", "--path" })
	Path path;
}
