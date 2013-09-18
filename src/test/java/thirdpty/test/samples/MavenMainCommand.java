package thirdpty.test.samples;

import com.github.ryenus.rop.OptionParser;
import com.github.ryenus.rop.OptionParser.Command;
import com.github.ryenus.rop.OptionParser.Option;

@Command(name = "Maven", descriptions = "Maven is a build automation tool used primarily for Java projects")
public class MavenMainCommand { // this is the parent Command

	@Option(opt = { "-o", "--offline" }, description = "Work offline")
	boolean offline;

	public static void main(String[] args) {
		OptionParser parser = new OptionParser(MavenMainCommand.class, // first Command is the parent (level-1 Command)
				MavenCleanCommand.class, MavenTestCommand.class); // all others are level-2 sub-commands
		parser.parse(args, true); // with the additional boolean argument, multiple sub-commands can run together 
	}
}

@Command(name = "clean", descriptions = "cleans up artifacts created by prior builds")
class MavenCleanCommand {
	// There's no option here

	void run() {
		// Simply perform the cleanup
	}
}

@Command(name = "test", descriptions = "test the compiled source code using a suitable unit testing framework. These tests should not require the code be packaged or deployed")
class MavenTestCommand {
	void run(OptionParser parser) {
		MavenMainCommand mavenMainCommand = parser.get(MavenMainCommand.class);
		if (mavenMainCommand.offline) {
			// work offline, no network connection
		}
		// do the test
	}
}
