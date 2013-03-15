# Rop

[![Build Status](https://travis-ci.org/ryenus/rop.png?branch=master)](https://travis-ci.org/ryenus/rop)

Rop is a small command line option parser written in Java.

## Introduction

### What is Rop?

Rop is designed to be minimal meanwhile convenient, to cover usual command line parsing use cases. It supports level-two sub-commands, like with `git add`.

### How to start?

Rop would be available as a Maven artifact `com.github.ryenus:rop`.

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

	void run(OptionParser parser, String[] params) {
		System.out.println("The run() method, if defined, would be invoked automatically.");
		System.out.println("And the run() method would receive remaining args, as:");
		for (String param : params) {
			System.out.println("\t" + param);
		}
	}
}
```

```java
// assume this is called with 'java TheMain -b'
public static void main(String[] args) {
	FooCommand foo = new FooCommand();
	OptionParser parser = new OptionParser(foo);
	parser.parse(args);
	if (b) { // b is set to true by the parser
		System.out.println(foo.i); // => 3
	}
}
```

During the end of parsing, if a method named `run()` is found, it would be called automatically, another benefit is that the options and the behaviour are both put inside the Command itself. If there're multiple `run()` methods in a command class, only the first found will be called.

In the above example,  `FooCommand.run()` would be called automatically, in which it can receive a reference to the parser itself, and all the remaining arguments in the params array. However, either argument or both can be omitted.

## Supported Field Types and Default Values

* String, and all primitive type and their wrapper types are directly supported.
* File, Path are supported as well, but not Date/Time yet.
* There might be a customizable type binder available in the future.

<pre>
Types                   Default Values
boolean                 false
byte                    0
short                   0
int                     0
long                    0L
float                   0.0f
double                  0.0d
char                    '\u0000'
String/Wrapper/Object   null
</pre>

As in the above example, a default option value can be directly set with the field. If not set, the option values default to their type default, as specified in http://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html

## Sub-commands

Some commands are quite simple, they just do one thing and do it well, such as commonly used unix command `cp` and `ls`. But other commands like `cvs` and `git` are quite different, Git provids a bunch of sub-commands to do many different things, such as `git add`, `git commit`, etc.

With OptionParser, you can register just one Command class to build simple commands like `cp`, you can also register multiple Command classes to build commands that support multiple sub-commands like `git`.

By default, the parser only handles no more than one sub-command, like with Git, in `git add ... commit ...`, the 'commit' is treated as an argument, not a sub-command.

If you have used Maven, it's a different story, you can run multiple sub-commands together, e.g. `mvn clean test package` is totally ok in Maven. Can we do that as well? Sure, but how? it's actually simple:

Rather than

```java
parser.parse(args)
```

instead, use

```java
parser.parse(args, true)
```

For each the recoganized commands, all the `run()` methods, if found, would be called, in the order they appear on the command line.


## Built-in Help

If option '--help' is present, the parser will:

1. display help information constructed from the Commands/Options annotations
2. call System.exit(0)

## Contributing

If you'd like to help improve Rop, clone the project with Git by running:

    $ git clone git://github.com/ryenus/rop

Work your magic and then submit a pull request. We love pull requests!

If you don't have the time to work on Rop, but found something we should know about, please submit an issue.

## License

Rop is released under the [MIT license](http://www.opensource.org/licenses/MIT).

## Related projects
