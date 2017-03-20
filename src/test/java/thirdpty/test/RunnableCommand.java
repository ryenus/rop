package thirdpty.test;

import com.github.ryenus.optj.OptionParser;
import com.github.ryenus.optj.Command;
import com.github.ryenus.optj.Option;

@Command(name = "run1", descriptions = "")
public class RunnableCommand {
	OptionParser parser;
	String[] params;

	@Option(description = "", opt = { "-b", "--boolean" }, required = true)
	boolean b;

	@Option(description = "", opt = { "-x", "--debug" }, required = true)
	boolean x;

	@Option(description = "", opt = { "-i", "--int" })
	int i = 9;

	int xc = 0;


	void run(OptionParser parser, String[] params) {
		i = 10;
		this.parser = parser;
		this.params = params;
		xc++;
	}
}