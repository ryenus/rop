# About optj

[![Build Status](https://travis-ci.org/ryenus/optj.png?branch=master)](https://travis-ci.org/ryenus/optj)

A lightweight command line option parser written in Java.


## Introduction

optj is designed to be minimal meanwhile convenient, and to cover most usual command line parsing use cases listed below:

| Command Line App Examples | Classification / Description
|---------------------------|---------------------------------------------
| `mv` and `ls`             | simple commands that just do one thing
| `git add`, `git commit`   | sub-commands, but single invocation
| `mvn clean test`          | sub-commands supporting multiple invocations

All these types of command line applications can be built using optj.

More than that, optj endorses building command line option parsers the Java way. Instead of following the traditional [GetOpt](http://en.wikipedia.org/wiki/Getopt) way of building an option parser, optj follows an approache that is:

* Annotation based, and
* Object oriented

You can build an option parser by defining Command classes and their fields annotated with the corresponding Option switches.

Also, each Command can optionally have a `run()` method to define its behavior, which would be called automatically after parsing.


## Getting Started

optj is available as a Maven artifact `com.github.ryenus:optj`. Simply add this to the dependencies section in your pom.xml:

```xml
<dependency>
  <groupId>com.github.ryenus</groupId>
  <artifactId>optj</artifactId>
  <version>${optj.version}</version>
</dependency>
```

You can always get the latest source code from https://github.com/ryenus/optj.


### Usage Example

Here's a quick example to demonstrate how to use optj:

```java
import com.github.ryenus.optj.OptionParser;
import com.github.ryenus.optj.OptionParser.Command;
import com.github.ryenus.optj.OptionParser.Option;


// 1. Here we define the Command class
@Command(name = "foo", descriptions = "A simple command with a few options.")
public class FooCommand {

	@Option(opt = { "-V", "--verbose" }, description = "explain what is being done")
	boolean verbose;

	@Option(opt = { "-n", "--number" }, description = "certain number")
	int n = 3; // default to 3

	void run(OptionParser parser, String[] params) { // either or both args can be omitted
		if (verbose) { // 'verbose' is set to true by the parser
			System.out.println("opt arg: " + n); // => 4
			for (String param : params) {
				System.out.println("param: " + param); // a, b
			}
		}
	}

	public static void main(String[] args) { // assume this is called with 'java TheMain --verbose -n 4 a b'
		// 2. Create the OptionParser instance along with the Command class
		OptionParser parser = new OptionParser(FooCommand.class);

		// 3. Parse the args
		parser.parse(args);

		// Here FooCommand.run() would be called automatically, so you don't have to
	}
}

```

In this example, we've basically done 3 things:

1. define the Command class, with `@Command` and `@Option` annotations
2. instantiate the `OptionParser` class with the Command class as the arguement to have it registered
3. parse the command line args with OptionParser.parse()

This is common to all comand line applications built using optj, regardless how complex the application is.

For API details, please refer to the javadoc.


## Understanding optj

The most significant thing in optj is the `OptionParser` class, with which Commands are registered, then you call its `parse()` method to parse the command line arguments.


### The `@Command` Annotation

Any vanilla class can be turned to a valid Command with the `@Command` annotation, regardless it has Options or not.

Hence we call it a **Command class**.


### The `@Option` Annotation

In a Command class, those fields having the `@Option` annotation, are viable to be set from command line argments during parsing. The above example should make this pretty clear.


### Command Registration

The `OptionParser` class provides a `register()` method to allow a Command, i.e. a class annotated with `@Command`, or its instance, to be registered with the parser. The `register()` method is chainable, as it always returns the parser object.

A Command can also be registered by passing it directly to the constructor `OptionParser()`, so you don't have to explicitly call the `register()` method. Also, the constructor can be called with any number of Commands.


### Post Parsing Hook - Method `Command#run()`

A Command can have a little magic. If it has a `run()` method, the parser would call this method at the end of parsing if the Command appeared in the command line.

In the above example, `FooCommand.run()` would be called automatically, it would receive a reference to the parser itself, and an array which consists of all the remaining arguments.

If you're not interested in getting either the parser or the parameters, just omit any of them, or both.

Note that if there're more than one `run()` methods in a Command class, only the first would be called.


### Using Sub-commands

Typical commands, such as [`mv`](http://en.wikipedia.org/wiki/Mv) and [`ls`](http://en.wikipedia.org/wiki/Ls) in Unix, are quite simple, they're designed to [do one thing and do it well](http://en.wikipedia.org/wiki/Unix_philosophy). It needs just one Command class to build such a simple command line application. However, not everything is simple like this.

Often, your command line application needs to do more than one thing, naturally you'll need more than one Command classes.


#### The Parent Command

Please note that besides all the sub-commands, as with `git add`, and `git commit`, you need a *main* Command class as the parent.

The parent Command usually becomes a no-action Command, it's often used to handle global options, especially `--version`, and `--help`.


#### Allowing only One Sub-command Being Called

Certain command line applications, for example, [Git](http://git-scm.com/) provides a bunch of sub-commands to do many different things, such as `git add`, `git commit` and `git log`.

By default, optj only allows one sub-command to be called on the command line, which is the case with Git.

This is done by calling the 1-arg version of `OptionParser#parse()`, i.e.:

```java
parser.parse(args);
```

Though Git provides many sub-commands, as with the default parsing behavior, you can only use one sub-command at a time, for example, in `git add ... commit ...`, the argument 'commit' loses its magic and would be treated as something to be added with `git add`, not the sub-command `git commit`.

#### Allowing Multiple Sub-commands to Be Called Together

But for [Ant](http://ant.apache.org/) and [Maven](http://maven.apache.org/), it's a different story. As in `mvn clean test`, you can run multiple sub-commands together. Can we do that as well? Sure, it's actually very simple:

Rather than calling

```java
parser.parse(args);
```

instead, call

```java
parser.parse(args, true);
```

The extra boolean argument, when set to 'true', tells the parser to recognize all the sub-commands it detected. For each properly recognized sub-command, its `run()` method, if exists, would be called, in the order they appeared on the command line.

### Managed Instance Objects of Command Classes

Internally optj helps manage all the instance objects of registered Command classes, which makes it possible to get the instance object of any registered Command class, e.g.:

```java
parser = new OptionParser(FooCommand.class); // register with class FooCommand

parser.get(FooCommand.class); // this is how to get the instance object for FooCommand class
```

A typical usage of this is in your `Command#run()` method:

```java
run(OptionParser parser, String[] params) {
  FooCommand foo = parser.get(FooCommand.class);
  System.out.println(foo.bar);
}
```

This allows your Commands to be loosely decoupled and flexibly reused.


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

Any possible error would be thrown as a RuntimeException, or its subclass, provided with proper error massege. You might want to catch the exception, print the error message and/or the help information before exiting the program. This task is intentionally left to you so that you can control how your program behaves upon parsing errors before terminating.

## Contributing

If you'd like to help improve optj, clone the project with Git by running:

    $ git clone https://github.com/ryenus/optj

Work your magic and then submit a pull request. We love pull requests!

If you don't have the time to work on optj, but found something we should know about, please submit an issue.

## License

optj is released under the [MIT License](http://www.opensource.org/licenses/MIT).

## Related projects

* [java-getopt](https://github.com/arenn/java-getopt)
* [JOpt Simple](http://pholser.github.com/jopt-simple)
* [JCommander](https://github.com/cbeust/jcommander)
* [Commons CLI](http://commons.apache.org/proper/commons-cli/)
* [joptparse](https://code.google.com/p/joptparse)
