package com.github.ryenus.optj;

import static com.github.ryenus.optj.OptionType.LONG;
import static com.github.ryenus.optj.OptionType.REVERSE;
import static com.github.ryenus.optj.OptionType.SHORT;

import java.io.File;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * optj - a lightweight command line option parser. It also supports level-two
 * sub-commands, as with {@code git add}.
 *
 * @author ryenus
 */
public class OptionParser {
	private final Map<Class<?>, Object> byType;
	private final Map<String, CommandUsage> byName;
	private CommandUsage top;
	private CommandUsage cci;

	/**
	 * Construct an OptionParse instance. It also accepts one or a group of,
	 * command classes or the corresponding instances to be registered with.
	 *
	 * @param commands one or more Command classes
	 *
	 * @see #register(Object)
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
	 *<p>The command registered first is treated as the top command, subsequently
	 * registered commands are all taken as level-two sub-commands, however,
	 * level-three sub-commands are not supported by design.
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
		CommandUsage existingCmd = byName.get(cmdName);
		if (existingCmd != null) {
			throw new RuntimeException(String.format("Unable to register '%s' command with %s, it's already registered by %s",
					cmdName, klass, existingCmd.command.getClass()));
		}

		byType.put(klass, instance);
		CommandUsage ci = new CommandUsage(instance, cmdAnno);
		if (top == null) {
			top = ci;
		}

		byName.put(cmdName, ci);
	}

	private static Object instantiate(Class<?> klass) {
		try {
			Constructor<?> constr = klass.getDeclaredConstructor();
			constr.setAccessible(true);
			return constr.newInstance();
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException
			| InvocationTargetException e) {
			throw new RuntimeException(String.format("Unable to instantiate %s. Please make sure the no-arg constructor exists and is accessible. For an inner class, make sure it's static", klass), e);
		}
	}

	/**
	 * Parse the command line args, but accept only the first sub-command, all
	 * other sub-command from the command line are treated as normal arguments.
	 *
	 * @param args
	 *            this should be the command line args passed to {@code main}
	 * @return a optsMap consists of the recognized command and their params
	 *
	 * @see #parse(String[], boolean)
	 */
	public Map<Object, String[]> parse(String[] args) {
		return parse(args, false);
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
	 * All the parsed commands would be collected in a {@link Map}, with each
	 * one's class as key, and it's parameters array as value.
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
	 * @param multi
	 *            whether to support multiple sub-commands, like with
	 *            {@literal `mvn clean test`}
	 * @return a optsMap consists of the recognized command and their params
	 */
	public Map<Object, String[]> parse(String[] args, boolean multi) {
		if (top == null) { // no command registered. nothing to do
			throw new RuntimeException("No Command registered");
		}

		Map<Object, String[]> cpm = new LinkedHashMap<>();
		List<String> params = new ArrayList<>();
		cci = top;

		ListIterator<String> lit = Arrays.asList(args).listIterator();
		while (lit.hasNext()) {
			String arg = lit.next();

			if (arg.equals("--help")) {
				showHelp();
				System.exit(0);
			}

			CommandUsage ci = byName.get(arg);
			if (ci != null) {
				if (ci == cci || cpm.containsKey(ci.command) || (!multi && cpm.size() > 0)) {
					params.add(arg);
				} else {
					stage(cpm, cci, params);
					params.clear();
					cci = ci;
				}

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
				if (cci.optsMap.containsKey(opt)) {
					parseOpt(opt, lit, type);
				} else {
					String[] opts = Utils.csplit(opt);
					parseOpts(opts, lit, type);
				}
			} else { // TODO: need 'real' unescaping logic
				params.add(arg.startsWith("\\") ? arg.substring(1) : arg);
			}
		}

		stage(cpm, cci, params);

		invokeRun(cpm); // call command.run(this)
		return cpm;
	}

	private static void stage(Map<Object, String[]> cpm, CommandUsage ci, List<String> params) {
		cpm.put(ci.command, params.toArray(new String[params.size()]));
		for (OptionUsage oi : new HashSet<>(ci.optsMap.values())) {
			if (oi.anno.required() && !oi.set) {
				throw new RuntimeException(String.format("Required option not found for field %s", oi.field));
			}
		}
	}

	private void parseOpts(String[] opts, ListIterator<String> liter, OptionType optionType) {
		for (String option : opts) {
			parseOpt(option, liter, optionType);
		}
	}

	private void parseOpt(String option, ListIterator<String> liter, OptionType optionType) {
		OptionUsage optionUsage = cci.optsMap.get(option);
		if (optionUsage == null) {
			throw new IllegalArgumentException(String.format("Unknown option '%s'", option));
		}

		Field field = optionUsage.field;
		field.setAccessible(true);
		Class<?> fieldType = field.getType();

		Object value = null;
		if (optionUsage.anno.secret()) {
			value = Utils.readSecret(optionUsage.anno.prompt());
		} else if (fieldType == boolean.class || fieldType == Boolean.class) {
			value = (optionType != REVERSE);
		} else { // TODO: support arity
			if (!liter.hasNext()) {
				throw new IllegalArgumentException(String.format("Argument missing for option '%s%s'", optionType.prefix, option));
			}
			value = parseValue(fieldType, liter.next());
		}

		try {
			field.set(cci.command, value);
			optionUsage.set = true;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static Object parseValue(Class<?> type, String value) {
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

	private void invokeRun(Map<Object, String[]> cpm) {
		for (Map.Entry<Object, String[]> entry : cpm.entrySet()) {
			invokeRun(entry.getKey(), entry.getValue());
		}
	}

	private Object invokeRun(Object cmd, String[] params) {
		try {
			try {
				Method run = cmd.getClass().getDeclaredMethod("run", OptionParser.class, String[].class);
				run.setAccessible(true);
				return run.invoke(cmd, this, params);
			} catch (NoSuchMethodException e) {
				try {
					Method run = cmd.getClass().getDeclaredMethod("run", String[].class, OptionParser.class);
					run.setAccessible(true);
					return run.invoke(cmd, params, this);
				} catch (NoSuchMethodException e1) {
					try {
						Method run = cmd.getClass().getDeclaredMethod("run", String[].class);
						run.setAccessible(true);
						return run.invoke(cmd, (Object) params); //bypass the var-args magic
					} catch (NoSuchMethodException e2) {
						try {
							Method run = cmd.getClass().getDeclaredMethod("run", OptionParser.class);
							run.setAccessible(true);
							return run.invoke(cmd, this);
						} catch (NoSuchMethodException e3) {
							try {
								Method run = cmd.getClass().getDeclaredMethod("run");
								run.setAccessible(true);
								return run.invoke(cmd);
							} catch (NoSuchMethodException e4) {
								return null;
							}
						}
					}
				}
			}
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Display the help information, which is constructed from all the registered
	 * Commands and their Options.
	 */
	public void showHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append(top.help(false));
		sb.append(String.format("\n      --help %20s display this help and exit", ""));

		List<CommandUsage> cmds = new ArrayList<>(byName.values());
		cmds.remove(top);
		Collections.sort(cmds, Utils.CMD_COMPARATOR);
		for (CommandUsage ci : cmds) {
			sb.append(String.format("\n\n[Command '%s']\n\n", ci.anno.name()));
			sb.append(ci.help(true));
		}

		sb.append(Utils.format(top.anno.notes(), true)).append('\n');
		System.out.print(sb.toString());
	}

	/**
	 * Get the instance of the provided Command class if it's registered.
	 *
	 * @param klass a registered Command class
	 * @param <T> the type of the Command class
	 *
	 * @return the instance of the given Command class, null if Command not
	 * registered
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> klass) {
		return (T) byType.get(klass);
	}

}
