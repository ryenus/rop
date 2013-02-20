# Rop

Rop is a small command line option parser.

## Introduction

### What is Rop?

Rop is designed to be minimal meanwhile convenient, to cover usual command line parsing use cases. It supports level-two sub-commands, like with `git add`.

### How to start?

Rop would be available as a Maven artifact `org.ryez:rop`.

You can always get the latest source code from https://github.com/ryenus/rop.

### Usage example

Here's a quick example to demonstrate how to use Rop:

```java
import org.ryez.OptionParser;
import org.ryez.OptionParser.Command;
import org.ryez.OptionParser.Option;

@Command(name = "run", description = "")
class FooCommand {
	@Option(description = "", opt = { "-b", "--boolean" })
	boolean b;

	@Option(description = "", opt = { "-i", "--int" })
	int i = 3; // default to 3
}
```

```java
// assume this is called with 'java TheMain -b'
public static void main(String[] args) {
	FooCommand foo = new FooCommand();
	OptionParser parser = new OptionParser(foo);
	String[] rest = parser.parse(args);
	if (b) { // b is set to true by the parser
		System.out.println(foo.i); // => 3
	}
}
```

## Supported Field Types

String, and all primitive type and their wrapper types are directly supported.

File, Path are supported as well, but not Date/Time yet.

There might be a customizable type binder available in the future.

<pre>
Primitive Types    Wrappers and Others    Default Values
boolean            Boolean                false
byte               Byte                   0
short	             Short                  0
int                Integer                0
long               Long                   0L
float              Float                  0.0f
double             Double                 0.0d
char               Character              '\u0000'
                   String (or any object) null
</pre>

## Built-in Help

If option '--help' is present, the parser will
1. display help information constructed from the Commands/Options annotations
2. call System.exit(0)

## Contributing

If you'd like to help improve Rop, clone the project with Git by running:

    $ git clone git://github.com/ryenus/rop

Work your magic and then submit a pull request. We love pull requests!

If you don't have the time to work on Rop, but found something we should know about, please submit an issue.

## License

Rop is released under the [MIT license](http://www.opensource.org/licenses/MIT).

## Authors
## Discuss

## Related projects
