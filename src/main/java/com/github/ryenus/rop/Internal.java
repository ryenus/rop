package com.github.ryenus.rop;

import java.io.Console;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.github.ryenus.rop.OptionParser.Command;
import com.github.ryenus.rop.OptionParser.Option;

class CommandInfo {
	Object command;
	Command anno;
	Map<String, OptionInfo> map;

	CommandInfo(Object command, Command anno) {
		this.command = command;
		this.anno = anno;

		map = new HashMap<>();
		Class<?> klass = command.getClass();
		for (Field field : klass.getDeclaredFields()) {
			if (!field.isSynthetic()) {
				Option optAnno = field.getAnnotation(Option.class);
				if (optAnno != null) {
					String[] opts = optAnno.opt();
					if (opts.length == 0) {
						throw new RuntimeException(String.format("@Option.opt is empty for '%s'", field));
					}

					if (optAnno.required() && optAnno.hidden()) {
						throw new RuntimeException(String.format("Required option '%s' cannot be hidden for '%s'", opts[0], field));
					}

					OptionInfo optionInfo = new OptionInfo(field, optAnno);
					for (String opt : opts) {
						String key = opt.replaceFirst("^(-)+", "");
						if (map.containsKey(key)) {
							throw new RuntimeException(String.format("Conflict option '%s' found in '%s' and '%s'", opt, map.get(key).field, field));
						}
						map.put(key, optionInfo);
					}
				}
			}
		}
	}

	String help(boolean showNotes) {
		StringBuilder sb = new StringBuilder();
		String cmdDesc = Utils.format(anno.descriptions(), false);
		sb.append(cmdDesc);

		List<String> list = new ArrayList<>(map.size());
		for (OptionInfo oi : new HashSet<>(map.values())) {
			if (!oi.anno.hidden()) {
				list.add(oi.help());
			}
		}

		Collections.sort(list, Utils.OPT_COMPARATOR);
		for (String string : list) {
			sb.append("\n").append(string);
		}

		if (showNotes) {
			sb.append(Utils.format(anno.notes(), true));
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
		String descText = Utils.format(anno.description(), true);
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
	private static final String PADDING = String.format("%36s", "");

	static final Comparator<CommandInfo> CMD_COMPARATOR = new Comparator<CommandInfo>() {
		@Override
		public int compare(CommandInfo o1, CommandInfo o2) {
			return o1.anno.name().compareTo(o2.anno.name());
		}
	};

	static final Comparator<String> OPT_COMPARATOR = new Comparator<String>() {
		@Override
		public int compare(String s1, String s2) {
			return strip(s1).compareTo(strip(s2));
		}

		private String strip(String optStr) {
			return optStr.substring(0, 32).replaceFirst("^\\s*-*", "");
		}
	};

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

	static String format(String[] sentences, boolean enclosed) {
		if (sentences.length == 0) {
			return "";
		}

		String prefix = enclosed ? "\n" : "";
		String suffix = enclosed ? "" : "\n";

		StringBuilder sb = new StringBuilder(prefix);
		for (String sentence : sentences) {
			sb.append(prefix).append(format(sentence, false)).append(suffix);
		}

		if (enclosed) {
			sb.deleteCharAt(sb.length() - 1);
		}

		return sb.toString();
	}

	static String format(String sentence, boolean indent) {
		int width = indent ? 44 : 80;
		String padding = indent ? PADDING : "";
		StringBuilder para = new StringBuilder();
		StringBuilder line = new StringBuilder();
		for (String word : wsplit(sentence)) {
			if (line.length() + word.length() <= width) {
				line.append(word).append(' ');
			} else {
				para.append(line.deleteCharAt(line.length() - 1)).append("\n").append(padding);
				line = new StringBuilder().append(word).append(' ');
			}
		}

		return para.append(line.deleteCharAt(line.length() - 1)).toString();
	}

	static String[] csplit(String word) { // split to chars
		return word.split("(?!^)"); // look-ahead, do not split at '^'
	}

	static String[] wsplit(String sentence) { // split to words
		return sentence.split("(?<!^)\\s+"); // look-behind
	}

	static char[] readSecret(String prompt) {
		Console console = System.console();
		if (console != null) {
			char[] password = null;
			while (password == null || password.length == 0) {
				password = console.readPassword("%s", prompt);
			}
			return password;
		}

		try(Scanner s = new Scanner(System.in)) {
			String line = null;
			while (line == null || line.length() == 0) {
				System.out.print(prompt);
				line = s.nextLine();
			}
			return line.toCharArray();
		}
	}
}
