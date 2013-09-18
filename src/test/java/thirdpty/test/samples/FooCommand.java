package thirdpty.test.samples;
import com.github.ryenus.rop.OptionParser;
import com.github.ryenus.rop.OptionParser.Command;
import com.github.ryenus.rop.OptionParser.Option;


// 1. Here we define the Command class
@Command(name = "foo", descriptions = "A simple command with a few options.")
public class FooCommand {

	@Option(opt = { "-V", "--verbose" }, description = "explain what is being done")
	boolean verbose;

	@Option(opt = { "-n", "--number" }, description = "certain number")
	int n = 3; // default to 3

	// This method would be called automatically
	// Both arguments of the run() methods can be omitted
	void run(OptionParser parser, String[] params) {
		if (verbose) { // 'verbose' is set to true by the parser
			System.out.println("opt arg: " + n); // => 4
			for (String param : params) {
				System.out.println("param: " + param); // a, b
			}
		}
	}

	// assume this is called with 'java TheMain --verbose -n 4 a b'
	public static void main(String[] args) {
		// 2. Create the OptionParser instance along with the Command class
		OptionParser parser = new OptionParser(FooCommand.class);

		// 3. Parse the args
		parser.parse(args);

		// Here FooCommand.run() would be called automatically, so you don't have to
	}
}
