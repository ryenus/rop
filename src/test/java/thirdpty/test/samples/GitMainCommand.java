package thirdpty.test.samples;

import com.github.ryenus.rop.OptionParser;
import com.github.ryenus.rop.OptionParser.Command;
import com.github.ryenus.rop.OptionParser.Option;

@Command(name = "Git", descriptions = "Git - A fast, scalable, distributed revision control system")
public class GitMainCommand { // this is the parent Command

	@Option(opt = "--version", description = "Print the version information and exit")
	boolean version; // whether to print version

	void run() {
		if (version) {
			System.out.println("git cli simulator version 0.1");
			System.exit(0);
		}
	}
	
	public static void main(String[] args) {
		OptionParser parser = new OptionParser(GitMainCommand.class, // first Command is the parent (level-1 Command)
				GitAddCommand.class, GitLogCommand.class); // all others are level-2 sub-commands
		parser.parse(args); // without the optional boolean argument, only 1 sub-command can run at a time 
	}
}

@Command(name = "add", descriptions = "add file contents to the index")
class GitAddCommand {

	@Option(opt = { "-f", "--force" }, description = "Allow adding otherwise ignored files" )
	boolean force;

	void run(String[] params) {
		for (String file : params) {
			if (force || !file.startsWith(".")) { // need to consult .gitignore and check
				// add file to index
			}
		}
	}
}

@Command(name = "log", descriptions = "show commit logs")
class GitLogCommand {

	@Option(opt = { "-p", "-u", "--patch" }, description = "Generate patch" )
	boolean patch;
	
	@Option(opt = {"-n", "--max-count"}, description = "Limit the number of commits to output")
	int n;

	void run(String[] params) {
		int count = 0;
		while (count++ < n) {
			// print commit header
			if (patch) {
				// print commit diff
			}
		}
	}
}
