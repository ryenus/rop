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
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

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
	private CommandInfo sub;
	private CommandInfo current;

	/**
	 * Construct an {@link OptionParse}. It also accepts one or a group of,
	 * command class(es) or the corresponding instance(s) to be registered with.
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
	 * The command registered first is taken as the top command, subsequently
	 * registered commands are taken as level-two sub-commands.
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

		byType.put(klass, instance);
		CommandInfo ci = new CommandInfo(instance, cmdAnno);
		if (top == null) {
			top = ci;
		} else {
			byName.put(cmdAnno.name(), ci);
		}
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
	 * FIXME
	 *
	 * @param args
	 * @return
	 */
	public String[] parse(String[] args) {
		if (top == null) { // no command registered. nothing to do
			throw new RuntimeException("Command not registered");
		}

		List<String> rest = new ArrayList<>();
		current = top;

		ListIterator<String> lit = Arrays.asList(args).listIterator();
		while (lit.hasNext()) {
			String arg = lit.next();

			if (arg.equals("--help")) {
				showHelp();
				System.exit(0);
			}

			if (sub == null) {
				CommandInfo ci = byName.get(arg);
				if (ci != null) {
					current = sub = ci;
					continue;
				}
			}

			if (arg.equals("--")) { // treat everything else as parameters
				while (lit.hasNext()) {
					rest.add(lit.next());
				}
			} else if (arg.startsWith(LONG.prefix)) {
				String opt = arg.substring(2);
				parseOpt(opt, lit, LONG);
			} else if (arg.startsWith(SHORT.prefix) || arg.startsWith(REVERSE.prefix)) {
				OptionType type = OptionType.get(arg.substring(0, 1));
				String opt = arg.substring(1);
				if (current.map.containsKey(opt)) {
					parseOpt(opt, lit, type);
				} else {
					String[] opts = Utils.csplit(opt);
					parseOpts(opts, lit, type);
				}
			} else {
				// TODO: need 'real' unescaping logic
				rest.add(arg.startsWith("\\-") ? arg.substring(1) : arg);
			}
		}

		invokeRun(current); // call command.run(this)

		return rest.toArray(new String[rest.size()]);
	}

	private void parseOpts(String[] opts, ListIterator<String> liter, OptionType optionType) {
		for (String option : opts) {
			parseOpt(option, liter, optionType);
		}
	}

	private void parseOpt(String option, ListIterator<String> liter, OptionType optionType) {
		OptionInfo optionInfo = current.map.get(option);
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
				field.set(current.command, value);
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

	private void invokeRun(CommandInfo ci) {
		try {
			Method run = ci.command.getClass().getDeclaredMethod("run", OptionParser.class);
			run.setAccessible(true);
			run.invoke(ci.command, this);
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
		Collections.sort(cmds, Utils.CMD_COMPARATOR);
		for (CommandInfo ci : cmds) {
			sb.append(String.format("\n\n[Command '%s']\n\n", ci.anno.name()));
			sb.append(ci.help(true));
		}

		sb.append(Utils.format(top.anno.notes(), true));
		System.out.print(sb.toString());
	}

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

		String[] descriptions() default {};

		String[] notes() default {};
	}

	/**
	 * Annotate the {@link Command} fields with Option. A default option value
	 * can be directly set on the annotated field.
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Option {
		String[] opt();

		String description();

		boolean required() default false;
	}
}
