package org.ryez;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
			if (!field.isSynthetic()) {
				Option optAnno = field.getAnnotation(Option.class);
				if (optAnno == null) {
					throw new RuntimeException(String.format("Annotation @Option missing on field '%s' in class '%s'", field.getName(), klass.getName()));
				}

				String[] opts = optAnno.opt();
				if (opts.length == 0) {
					throw new RuntimeException(String.format("@Option.opt is empty on field '%s' in class '%s'", field.getName(), klass.getName()));
				}

				OptionInfo optionInfo = new OptionInfo(field, optAnno);
				for (String opt : opts) {
					String key = opt.replaceFirst("^(-)+", "");
					if (map.containsKey(key)) {
						throw new RuntimeException(String.format("Cannot use opt '%s' again on field '%s' in class '%s', already used on field '%s'",
								opt, field.getName(), klass.getName(), map.get(key).field.getName()));
					}
					map.put(key, optionInfo);
				}
			}
		}
	}

	String help() {
		StringBuilder sb = new StringBuilder();
		String cmdDesc = Utils.formatPara(anno.description(), 78, false);
		sb.append(cmdDesc);

		HashSet<OptionInfo> opts = new HashSet<OptionInfo>(map.values());
		List<String> list = new ArrayList<>(opts.size());
		for (OptionInfo oi : opts) {
			list.add(oi.help());
		}

		Collections.sort(list, Utils.OPTS_COMPARATOR);

		for (String string : list) {
			sb.append("\n").append(string);
		}

		return sb.toString();
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

	String help() {
		String optsText = Utils.formatOpts(anno.opt());
		String descText = Utils.formatDesc(anno.description());
		return String.format("%s  %s", optsText, descText);
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

class Utils {
	private static final String PADDING = String.format("%34s", "");

	static Comparator<String> OPTS_COMPARATOR = new Comparator<String>() {
		@Override
		public int compare(String s1, String s2) {
			return strip(s1).compareTo(strip(s2));
		}

		private String strip(String optStr) {
			return optStr.substring(0, 32).replaceFirst("^\\s*-*", "");
		}
	};

	static String[] split(String string) {
		// split with lookahead pattern '(?!^)' to avoid splitting at '^'
		return string.split("(?!^)");
	}

	static String formatOpts(String[] opts) {
		String shortOpt = null, longOpt = null;
		for (String opt : opts) {
			if (opt.length() == 2 && shortOpt == null) {
				shortOpt = opt;
			} else if (opt.length() > 2 && longOpt == null) {
				longOpt = opt;
			}
		}

		String optStr;
		if (longOpt == null) {
			optStr = String.format("  %s", shortOpt);
		} else if (shortOpt == null) {
			optStr = String.format("      %s", longOpt);
		} else {
			optStr = String.format("  %s, %s", shortOpt, longOpt);
		}

		if (optStr.length() > 32) {
			return String.format("%s\n%32s", optStr, "");
		} else {
			return String.format("%-32s", optStr);
		}
	}

	static String formatDesc(String desc) {
		return formatPara(desc, 44, true);
	}

	static String formatPara(String desc, int width, boolean indent) {
		StringBuilder para = new StringBuilder();
		StringBuilder line = new StringBuilder();
		String padding = indent ? PADDING : "";
		for (String word : desc.split("\\s+")) {
			if (line.length() + word.length() < width) {
				line.append(word).append(' ');
			} else {
				para.append(line.deleteCharAt(line.length() - 1)).append("\n").append(padding);
				line = new StringBuilder().append(word).append(' ');
			}
		}

		return para.append(line.deleteCharAt(line.length() - 1)).toString();
	}
}
