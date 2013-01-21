package org.ryez;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * A minimal but useful command line option parser. It also supports level-two
 * sub-commands, as with {@code git add}.
 * 
 * @author ryenus
 */
public class OptionParser {
	private final Map<Class<?>, Object> pool;
	private final Map<String, CommandInfo> subs;
	private final List<String> rest;
	private CommandInfo top;
	private CommandInfo current; // current command holder

	/**
	 * Create an {@link OptionParse} instance. It also accepts one or a group
	 * of, command object(s) or class(es) to be registered with.
	 */
	public OptionParser(Object... commands) {
		this.pool = new HashMap<>();
		this.subs = new HashMap<>();
		this.rest = new ArrayList<>();

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
		Object instance;
		Class<?> klass;
		if (command instanceof Class) {
			klass = (Class<?>) command;
			instance = instantiate(klass);
		} else {
			instance = command;
			klass = instance.getClass();
		}

		pool.put(klass, instance);

		Command cmdAnno = klass.getAnnotation(Command.class);
		if (cmdAnno == null) {
			throw new RuntimeException(String.format("Annotation @Command missing on %s", klass.getName()));
		}

		CommandInfo holder = new CommandInfo(instance, cmdAnno);
		if (top == null) {
			top = holder;
		} else {
			subs.put(cmdAnno.name(), holder);
		}

		return this;
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
		if (top == null) {
			return args; // no command registered. nothing to do
		}
		if (!rest.isEmpty()) {
			rest.clear();
		}
		current = top;

		ListIterator<String> liter = Arrays.asList(args).listIterator();
		while (liter.hasNext()) {
			String arg = liter.next();
			CommandInfo ci = subs.get(arg);
			if (ci != null) {
				current = ci;
				continue;
			}

			if (arg.equals("--")) { // treat everything else as paramaters
				while (liter.hasNext()) {
					rest.add(liter.next());
				}
			} else if (arg.startsWith("--")) {
				String opt = arg.substring(2);
				parseOpt(opt, liter, OptionType.LONG);
			} else if (arg.startsWith("-")) {
				String[] opts = Strings.split(arg.substring(1));
				parseOpts(opts, liter, OptionType.SHORT);
			} else if (arg.startsWith("+")) {
				String[] opts = Strings.split(arg.substring(1));
				parseOpts(opts, liter, OptionType.REVERSE);
			} else {
				rest.add(arg);
			}
		}

		// TODO: call command.run

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
				value = (optionType != OptionType.REVERSE);
			} else { // TODO: support arity
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

	@SuppressWarnings("unchecked")
	public <T> void showUsage(T command) {
		StringBuilder sb = new StringBuilder();
		Class<T> klass = (Class<T>) command.getClass();
		Command annoCmd = klass.getAnnotation(Command.class);

		sb.append(String.format("usage: %s ", annoCmd.name()));

		Field[] fields = klass.getDeclaredFields();
		for (Field field : fields) {
			Option annoOpt = field.getAnnotation(Option.class);

			Class<?> fieldType = field.getType();
			if (Boolean.class.equals(fieldType)) {
				String[] opts = annoOpt.opt();
				boolean optional = !annoOpt.required();
				if (optional) {
					sb.append('[');
				}
				for (String opt : opts) {
					sb.append(opt).append('|');
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> klass) {
		return (T) pool.get(klass);
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Command {
		String name() default "";

		String description();
	}

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Option {
		String[] opt();

		String description();

		boolean required() default false;
	}
}
