package org.ryez;

import static org.ryez.OptionType.LONG;
import static org.ryez.OptionType.REVERSE;
import static org.ryez.OptionType.SHORT;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * A small command line option parser. It also supports level-two
 * sub-commands, as with {@code git add}.
 *
 * @author ryenus
 */
public class OptionParser {
	private final Map<Class<?>, Object> byType;
	private final Map<String, CommandInfo> byName;
	private CommandInfo top;
	private CommandInfo cci;

	/**
	 * Construct an {@link OptionParse}. It also accepts one or a group of,
	 * command classes or the corresponding instances to be registered with.
	 */
	public OptionParser(Object... commands) {
		this.byType = new HashMap<>();
		this.byName = new HashMap<>();

		for (Object command : commands) {
			if (command instanceof Collection<?>) {
				for (Object object : (Collection<?>) command) {
					register(object);
				}
			} else {
				register(command);
			}
		}
	}

	/**
	 * Register a command class or its instance. For a class, an instance will
	 * be created internally and available via {@link #get(Class)}.
	 *
	 *<p>
	 * The command registered first is treated as the top command, subsequently
	 * registered commands are all taken as level-two sub-commands, however,
	 * level-three sub-commands are not supported by design.
	 *</p>
	 *
	 * @param command
	 *            a command class (or its instance) to be registered, the class
	 *            must be annotated with {@link Command}
	 *
	 * @return the {@link OptionParser} instance to support chained invocations
	 */
	public OptionParser register(Object command) {
		Class<?> klass;
		Object instance;
		if (command instanceof Class) {
			klass = (Class<?>) command;
			instance = instantiate(klass);
		} else {
			instance = command;
			klass = instance.getClass();
		}

		register(klass, instance);
		return this;
	}

	private void register(Class<?> klass, Object instance) {
		Command cmdAnno = klass.getAnnotation(Command.class);
		if (cmdAnno == null) {
			throw new RuntimeException(String.format("Annotation @Command missing on %s", klass.getName()));
		}

		String cmdName = cmdAnno.name();
		CommandInfo existingCmd = byName.get(cmdName);
		if (existingCmd != null) {
			throw new RuntimeException(String.format("A command named '%s' is already registered by class '%s'", cmdName, existingCmd.command.getClass().getName()));
		}

		byType.put(klass, instance);
		CommandInfo ci = new CommandInfo(instance, cmdAnno);
		if (top == null) {
			top = ci;
		}

		byName.put(cmdName, ci);
	}

	private Object instantiate(Class<?> klass) {
		try {
			Constructor<?> implicit = klass.getDeclaredConstructor();
			implicit.setAccessible(true);
			return implicit.newInstance();
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException
			| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * After registering all {@link Command} classes/objects, invoke this method
	 * to parse the command line args and populate the {@link Option} fields of
	 * the registered command objects.
	 *
	 * <p>
	 * If the built-in option {@literal "--help"} is found, the parser will
	 * generate and display the help information, then call
	 * {@code System.exit(0)}.
	 * </p>
	 *
	 * <p>
	 * All the parsed commands would be collected in a {@link Map},
	 * with each one's class as key, and it's parameters array as value.
	 * </p>
	 *
	 * <p>
	 * After parsing, for each parsed command if it has the method
	 * {@code run(OptionParser, String[])} defined, the method will be invoked
	 * automatically, with the OptionParser object and its parameters passed in.
	 * </p>
	 *
	 * @param args
	 *            this should be the command line args passed to {@code main}
	 * @return the rest of args that are not consumed by the parser
	 */
	public Map<Class<?>, String[]> parse(String[] args) {
		if (top == null) { // no command registered. nothing to do
			throw new RuntimeException("No Command registered");
		}

		Map<Class<?>, String[]> cpm = new LinkedHashMap<>();
		List<String> params = new ArrayList<>();
		cci = top;

		ListIterator<String> lit = Arrays.asList(args).listIterator();
		while (lit.hasNext()) {
			String arg = lit.next();

			if (arg.equals("--help")) {
				showHelp();
				System.exit(0);
			}

			CommandInfo ci = byName.get(arg);
			if (ci != null) {
				cpm.put(cci.command.getClass(), Utils.listToArray(params));
				cci = ci;
				params.clear(); // = new ArrayList<>();
				continue;
			}

			if (arg.equals("--")) { // treat everything else as parameters
				while (lit.hasNext()) {
					params.add(lit.next());
				}
			} else if (arg.startsWith(LONG.prefix)) {
				String opt = arg.substring(2);
				parseOpt(opt, lit, LONG);
			} else if (arg.startsWith(SHORT.prefix) || arg.startsWith(REVERSE.prefix)) {
				OptionType type = OptionType.get(arg.substring(0, 1));
				String opt = arg.substring(1);
				if (cci.map.containsKey(opt)) {
					parseOpt(opt, lit, type);
				} else {
					String[] opts = Utils.csplit(opt);
					parseOpts(opts, lit, type);
				}
			} else {
				// TODO: need 'real' unescaping logic
				params.add(arg.startsWith("\\-") ? arg.substring(1) : arg);
			}
		}

		// TODO: check required args
		String[] remains = Utils.listToArray(params);
		cpm.put(cci.command.getClass(), remains);

		invokeRun(cpm); // call command.run(this)
		return cpm;
	}

	private void invokeRun(Map<Class<?>, String[]> cpm) {
		Set<Class<?>> klasses = cpm.keySet();
		for (Class<?> klass : klasses) {
			invokeRun(klass, cpm.get(klass));
		}

	}

	private void parseOpts(String[] opts, ListIterator<String> liter, OptionType optionType) {
		for (String option : opts) {
			parseOpt(option, liter, optionType);
		}
	}

	private void parseOpt(String option, ListIterator<String> liter, OptionType optionType) {
		OptionInfo optionInfo = cci.map.get(option);
		if (optionInfo != null) {
			Field field = optionInfo.field;
			field.setAccessible(true);
			Class<?> fieldType = field.getType();

			Object value = null;
			if (fieldType == boolean.class || fieldType == Boolean.class) {
				value = (optionType != REVERSE);
			} else { // TODO: support arity and password input
				if (!liter.hasNext()) {
					throw new IllegalArgumentException(String.format("Argument missing for option '%s%s'", optionType.toString(), option));
				}
				value = parseValue(fieldType, liter.next());
			}

			try {
				field.set(cci.command, value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			optionInfo.set = true;
		} else {
			throw new IllegalArgumentException(String.format("Unknown option '%s", option));
		}
	}

	private Object parseValue(Class<?> type, String value) {
		if (type == String.class) {
			return value;
		} else if (type == int.class || type == Integer.class) {
			return Integer.decode(value);
		} else if (type == long.class || type == Long.class) {
			return Long.decode(value);
		} else if (type == byte.class || type == Byte.class) {
			return Byte.decode(value);
		} else if (type == short.class || type == Short.class) {
			return Short.decode(value);
		} else if (type == double.class || type == Double.class) {
			return Double.parseDouble(value);
		} else if (type == float.class || type == Float.class) {
			return Float.parseFloat(value);
		} else if (type == char.class || type == Character.class) {
			return value.charAt(0);
		} else if (type == File.class) {
			return new File(value);
		} else if (type == Path.class) {
			return Paths.get(value);
		}
		return value;
	}

	private void invokeRun(Class<?> klass, String[] params) {
		try {
			Method run = klass.getDeclaredMethod("run", OptionParser.class, String[].class);
			run.setAccessible(true);
			run.invoke(get(klass), this, params);
		} catch (NoSuchMethodException e) {
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	private void showHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append(top.help(false));
		sb.append(String.format("\n      --help %20s display this help and exit", ""));

		List<CommandInfo> cmds = new ArrayList<>(byName.values());
		cmds.remove(top);
		Collections.sort(cmds, Utils.CMD_COMPARATOR);
		for (CommandInfo ci : cmds) {
			sb.append(String.format("\n\n[Command '%s']\n\n", ci.anno.name()));
			sb.append(ci.help(true));
		}

		sb.append(Utils.format(top.anno.notes(), true));
		System.out.print(sb.toString());
	}

	/**
	 * After parsing, if given a registered Command class, this method will
	 * return its instance, with all the parsed option values.
	 *
	 * @param klass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> klass) {
		return (T) byType.get(klass);
	}

	/**
	 * Annotate a class as Command to use it with {@link OptionParser}.
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Command {
		String name();

		/**
		 * Command descriptions are automatically included in the help
		 * information before options list.
		 *
		 * <p>
		 * Using an array of words, each entry would have its own paragraph.
		 * Additionally, by prefixing an entry with an {@literal '\n'}
		 * character, a new line would be inserted above it accordingly to
		 * separate from the previous entry
		 * </p>
		 *
		 * <pre>
		 * descriptions              result
		 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		 * {"statement 1",           statement 1
		 *  "\nstatement 2"}
		 *                           statement 2
		 * --------------------------------------
		 * {"statement 1",           statement 1
		 *  " indented item A"        indented item A
		 *  " indented item B"}       indented item B
		 * </pre>
		 *
		 * If a line contains than 80 characters, the line would be
		 * automatically wrapped near the 80th column.
		 */
		String[] descriptions() default {};

		/**
		 * Command notes are automatically included in the help information
		 * after options list.
		 *
		 * <p>
		 * As with {@link #descriptions()}, the same trick can be used to
		 * separate paragraphs
		 */
		String[] notes() default {};
	}

	/**
	 * Annotate the {@link Command} fields with Option. A default option value
	 * can be directly set on the annotated field.
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Option {
		/**
		 * The option keys, like {@literal '-f'}, {@literal '--file'}
		 *
		 * Multiple option keys are supported, but the built-in help information
		 * would only display the first short option and the first long option
		 * if there're many.
		 */
		String[] opt();

		/**
		 * A not so long description of this option, also it would be wrapped
		 * correctly even with an indent of 2 spaces from the second line.
		 */
		String description();

		boolean required() default false;
	}
}
