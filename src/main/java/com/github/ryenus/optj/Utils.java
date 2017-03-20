package com.github.ryenus.optj;

import java.io.Console;
import java.util.Comparator;
import java.util.Scanner;
import java.util.regex.Pattern;

class Utils {
	private static final String PADDING = String.format("%36s", "");
	private static final Pattern OPT_PREFIX = Pattern.compile("^-{1,2}"); // leading '-' or '--'
	private static final Pattern CHAR_SPLITTER = Pattern.compile("(?!^)"); // look-ahead, do not split at '^'
	private static final Pattern WORD_SPLITTER = Pattern.compile("(?<!^)\\s+"); // look-behind

	static final Comparator<CommandUsage> CMD_COMPARATOR = new Comparator<CommandUsage>() {
		@Override
		public int compare(CommandUsage o1, CommandUsage o2) {
			return o1.anno.name().compareTo(o2.anno.name());
		}
	};

	static final Comparator<String> OPT_COMPARATOR = new Comparator<String>() {
		@Override
		public int compare(String s1, String s2) {
			return stripOptPrefix(s1).compareTo(stripOptPrefix(s2));
		}
	};

	private static String stripOptPrefix(String optStr) {
		return OPT_PREFIX.matcher(optStr).replaceFirst("");
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
		return CHAR_SPLITTER.split(word);
	}

	static String[] wsplit(String sentence) { // split to words
		return WORD_SPLITTER.split(sentence);
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
