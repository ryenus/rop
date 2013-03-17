# Rop

[![Build Status](https://travis-ci.org/ryenus/rop.png?branch=master)](https://travis-ci.org/ryenus/rop)

Rop is a lightweight command line option parser written in Java.

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

@Command(name = "foo", description = "")
class FooCommand {
	@Option(description = "", opt = { "-b", "--boolean" })
	boolean b;

	@Option(description = "", opt = { "-i", "--int" })
	int i = 3; // default to 3

	void run(OptionParser parser, String[] params) {
		// This method would be called automatically

		// Both arguments, the parser and the params are optional
		for (String param : params) {
			System.out.println("\t" + param);
		}
	}
}

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

## Understanding Rop

The most significant thing in Rop is the `OptionParser` class, with which Commands are registered, then you call its `parse()` method to parse the command line arguments.

### The `@Command` Annotation

Any vanilla class can be turned to a valid Command with the `@Command` annotation.

### The `@Option` Annotation

In a Command class, those fields having the `@Option` annotation, are viable to be set from command line argments when the Command and Options are recognized during parsing. The above example should make it pretty clear.

### Command Registration

The parser, i.e, the `OptionParser` class, provides a `register()` method to allow a Command, i.e. a class annotated with `@Command` or its instance, to be registered with the parser. The `register()` method is chainable, as it always returns the parser object.

A Command can also be registered by passing it directly to the constructor `OptionParser(Object ...)`, so you don't have to explicitly call the `register()` method. As you can see, the constructor can be called with any number of Commands.

### Post Parsing Hook - Method `run`

A Command can have a little magic. If it has a method named `run`, such as `run(OptionParser parser, String[] params)`. The parser would call this method at the end of parsing.

If you're not interested in getting either the parser, or the parameters, just omit it, or even both of them.

Note that if there're more than one `run` method, only the first would be called.

In the above example,  `FooCommand.run()` would be called automatically, in which it can receive a reference to the parser itself, and an array which consists of all the remaining arguments.

### Supported Field Types and Default Values

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

As in the above example, a default option value can be directly set to its associated field. If not set, the option values default to their type default, as list above, according to Java Tutorial - [Primitive Data Types](http://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html).

### Sub Command

Typical commands, such as [`mv`](http://en.wikipedia.org/wiki/Mv) and [`ls`](http://en.wikipedia.org/wiki/Ls) in Unix, are quite simple, they're designed to [do one thing and do it well](http://en.wikipedia.org/wiki/Unix_philosophy).

However, not everything is simple. [Git](http://git-scm.com/) provides a bunch of sub-commands to do many different things, such as `git add`, `git commit` and `git log`.

With Rop, you can register just one Command to build a simple command like `mv`, you can also register multiple Commands to support sub-commands like Git.

Though Git provides many sub-commands, you can only use one sub-command at a time, in `git add ... commit ...`, the 'commit' is treated as something to be added with `git add`, not the sub-command `git commit`.

But for [Apache Maven](http://en.wikipedia.org/wiki/Apache_Maven), it's a different story, you can run multiple sub-commands together, e.g. `mvn clean test package` is totally ok in Maven. Can we do that as well? Sure, it's actually very simple:

Rather than calling

```java
parser.parse(args)
```

instead, call

```java
parser.parse(args, true)
```

The extra boolean argument, when set to 'true', tells the parser to recognize all the sub-commands appeared. For each recognized sub-command, its `run()` method, if found, would be called, in the order they appeared on the command line.

### Built-in Help

If option '--help' is present, the parser will:

1. display help information constructed from the Commands/Options annotations
2. call System.exit(0)

## Contributing

If you'd like to help improve Rop, clone the project with Git by running:

    $ git clone git://github.com/ryenus/rop

Work your magic and then submit a pull request. We love pull requests!

If you don't have the time to work on Rop, but found something we should know about, please submit an issue.

## License

Rop is released under the [MIT License](http://www.opensource.org/licenses/MIT).

## Related projects

* [JCommander](https://github.com/cbeust/jcommander)
* [joptparse](https://code.google.com/p/joptparse)
