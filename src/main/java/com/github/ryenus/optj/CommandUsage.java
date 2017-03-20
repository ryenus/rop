package com.github.ryenus.optj;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

class CommandUsage {
	Object command;
	Command anno;
	Map<String, OptionUsage> optsMap = new HashMap<>();

	CommandUsage(Object command, Command anno) {
		this.command = command;
		this.anno = anno;

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

					OptionUsage optUsage = new OptionUsage(field, optAnno);
					for (String opt : opts) {
						String key = opt.replaceFirst("^(-)+", "");
						if (optsMap.containsKey(key)) {
							throw new RuntimeException(String.format("Conflict option '%s' found in '%s' and '%s'", opt, optsMap.get(key).field, field));
						}
						optsMap.put(key, optUsage);
					}
				}
			}
		}
	}

	String help(boolean showNotes) {
		StringBuilder sb = new StringBuilder();
		String cmdDesc = Utils.format(anno.descriptions(), false);
		sb.append(cmdDesc);

		List<String> list = new ArrayList<>(optsMap.size());
		for (OptionUsage oi : new HashSet<>(optsMap.values())) {
			if (!oi.anno.hidden()) {
				list.add(oi.help());
			}
		}

		list.sort(Utils.OPT_COMPARATOR);
		for (String string : list) {
			sb.append("\n").append(string);
		}

		if (showNotes) {
			sb.append(Utils.format(anno.notes(), true));
		}

		return sb.toString();
	}
}