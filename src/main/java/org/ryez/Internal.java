package org.ryez;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.ryez.OptionParser.Command;
import org.ryez.OptionParser.Option;

class CommandInfo {
	Object command;
	Command anno;
	Map<String, OptionInfo> map;

	CommandInfo(Object command, Command anno) {
		this.command = command;
		this.anno = anno;

		map = new HashMap<>();
		Class<? extends Object> klass = command.getClass();
		for (Field field : klass.getDeclaredFields()) {
			if (field.isSynthetic()) {
				continue;
			}

			Option optAnno = field.getAnnotation(Option.class);
			if (optAnno == null) {
				throw new RuntimeException(String.format("Annotation @Option missing on %s.%s", klass.getName(), field.getName()));
			}

			for (String opt : optAnno.opt()) {
				map.put(opt.replaceFirst("^(-)+", ""), new OptionInfo(field, optAnno));
			}
		}
	}
}

class OptionInfo {
	Field field;
	Option anno;
	boolean set;

	OptionInfo(Field field, Option optAnno) {
		this.field = field;
		this.anno = optAnno;
		this.set = false;
	}
}

enum OptionType {
	LONG("--"), SHORT("-"), REVERSE("+");

	public final String prefix;

	private OptionType(String prefix) {
		this.prefix = prefix;
	}

	public static OptionType get(String prefix) {
		String p = prefix.intern();
		return p == "-" ? SHORT : p == "+" ? REVERSE : LONG;
	}
}

class Arrays {
	static <T> String join(T[] array, String delimiter) {
		if (array.length == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder().append(array[0]);
		for (int i = 1; i < array.length; i++) {
			sb.append(delimiter).append(array[i]);
		}
		return sb.toString();
	}
}

class Strings {
	static String[] split(String string) {
		// split with lookahead pattern '(?!^)' to avoid splitting at '^'
		return string.split("(?!^)");
	}
}
