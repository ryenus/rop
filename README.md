# Rop

[![Build Status](https://travis-ci.org/ryenus/rop.png?branch=master)](https://travis-ci.org/ryenus/rop)

Rop is a lightweight command line option parser written in Java.

## Introduction

Rop is designed to be minimal meanwhile convenient, to cover most usual command line parsing use cases. You can use Rop to build command line programs like:

* `mv` and `ls`

    - simple commands that just do one thing

* `git add`, `git commit`

    - sub-commands, but single invocation

* `mvn clean test`

    - sub-commands with multiple invocations

More importantly, Rop endorses building command line option parsers the Java way. Instead of following the traditional [GetOpt](http://en.wikipedia.org/wiki/Getopt) way of building an option parser, Rop follows an approache that is:

* Annotation based, and
* Object-oriented

You can build an option parser by defining Command classes and their fields annotated with the corresponding Option switches.

Also, each Command can optionally have a `run()` method to define its behavior, which would be called by the parser automatically.

### How to start?

Rop is available as a Maven artifact `com.github.ryenus:rop`. Simply add this to the dependencies section in your pom.xml:

```xml
<dependency>
    <groupId>com.github.ryenus</groupId>
    <artifactId>rop</artifactId>
    <version>1.0</version>
</dependency>
```

Note that '1.0' might not be the latest version when you're reading this.

You can always get the latest source code from https://github.com/ryenus/rop.

### Usage example

Here's a quick example to demonstrate how to use Rop:

```java
import com.github.ryenus.rop.OptionParser;
import com.github.ryenus.rop.OptionParser.Command;
import com.github.ryenus.rop.OptionParser.Option;

@Command(name = "foo", description = "A simple command with a few options.")
class FooCommand {
	@Option(description = "explain what is being done", opt = { "-V", "--verbose" })
	boolean verbose;

	@Option(description = "certain number", opt = { "-n", "--number" })
	int n = 3; // default to 3

	// This method would be called automatically
	// Both arguments of the run() methods can be omitted
	void run(OptionParser parser, String[] params) {
		if (verbose) { // 'verbose' is set to true by the parser
			System.out.println(n); // => 4
			for (String param : params) {
				System.out.println(param);
			}
		}
	}
}

// assume this is called with 'java TheMain --verbose -n 4 a b'
public static void main(String[] args) {
	OptionParser parser = new OptionParser(FooCommand.class);
	parser.parse(args);
}
```

## Understanding Rop

The most significant thing in Rop is the `OptionParser` class, with which Commands are registered, then you call its `parse()` method to parse the command line arguments.

### The `@Command` Annotation

Any vanilla class can be turned to a valid Command with the `@Command` annotation.

### The `@Option` Annotation

In a Command class, those fields having the `@Option` annotation, are viable to be set from command line argments during parsing. The above example should make it pretty clear.

### Command Registration

The `OptionParser` class provides a `register()` method to allow a Command, i.e. a class annotated with `@Command`, or its instance, to be registered with the parser. The `register()` method is chainable, as it always returns the parser object.

A Command can also be registered by passing it directly to the constructor `OptionParser()`, so you don't have to explicitly call the `register()` method. Also, the constructor can be called with any number of Commands.

### Post Parsing Hook - Method `run`

A Command can have a little magic. If it has a `run()` method, the parser would call this method at the end of parsing if the Command appeared in the command line.

In the above example,  `FooCommand.run()` would be called automatically, it would receive a reference to the parser itself, and an array which consists of all the remaining arguments.

If you're not interested in getting either the parser or the parameters, just omit any of them, or both.

Note that if there're more than one `run()` method, only the first would be called.

### Sub Command

Typical commands, such as [`mv`](http://en.wikipedia.org/wiki/Mv) and [`ls`](http://en.wikipedia.org/wiki/Ls) in Unix, are quite simple, they're designed to [do one thing and do it well](http://en.wikipedia.org/wiki/Unix_philosophy).

However, not everything is simple. [Git](http://git-scm.com/) provides a bunch of sub-commands to do many different things, such as `git add`, `git commit` and `git log`.

With Rop, you can register just one Command to build a simple command like `mv`, you can also register multiple Commands to support sub-commands like Git.

Though Git provides many sub-commands, you can only use one sub-command at a time, for example, in `git add ... commit ...`, the argument 'commit' loses its magic and would be treated as something to be added with `git add`, not the sub-command `git commit`.

But for [Ant](http://ant.apache.org/) and [Maven](http://maven.apache.org/), it's a different story. As in `mvn clean test`, you can run multiple sub-commands together. Can we do that as well? Sure, it's actually very simple:

Rather than calling

```java
parser.parse(args)
```

instead, call

```java
parser.parse(args, true)
```

The extra boolean argument, when set to 'true', tells the parser to recognize all the sub-commands it detected. For each properly recognized sub-command, its `run()` method, if exists, would be called, in the order they appeared on the command line.

### Supported Field Types and Default Values

* String, and all primitive type and their wrapper types are directly supported.
* File, Path are supported as well, but not Date/Time yet.
* There might be a customizable type binder available in the future.

As in the above example, a default option value can be directly set to its associated field. If not set, the option values default to their type default, as list above, according to Java Tutorial - [Primitive Data Types](http://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html).

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

### Built-in Help

If option '--help' is present, the parser will:

1. display help information constructed from the Commands/Options annotations
2. call System.exit(0)

### Error Handling

Any possible error would be thrown as a RuntimeException, or its subclass, provided with proper error massege. You might want to catch the exceptions, print the error message and/or the help information before exiting the program. This is intentionally left to you so that you can control how your program behaves upon parsing errors before terminating.

## Contributing

If you'd like to help improve Rop, clone the project with Git by running:

    $ git clone https://github.com/ryenus/rop

Work your magic and then submit a pull request. We love pull requests!

If you don't have the time to work on Rop, but found something we should know about, please submit an issue.

## License

Rop is released under the [MIT License](http://www.opensource.org/licenses/MIT).

## Related projects

* [java-getopt](https://github.com/arenn/java-getopt)
* [JOpt Simple](http://pholser.github.com/jopt-simple)
* [JCommander](https://github.com/cbeust/jcommander)
* [Commons CLI](http://commons.apache.org/proper/commons-cli/)
* [joptparse](https://code.google.com/p/joptparse)
