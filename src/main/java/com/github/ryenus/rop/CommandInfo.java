package com.github.ryenus.rop;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
						throw new OptParseException(String.format("@Option.opt is empty for '%s'", field));
					}

					if (optAnno.required() && optAnno.hidden()) {
						throw new OptParseException(String.format("Required option '%s' cannot be hidden for '%s'", opts[0], field));
					}

					OptionInfo optionInfo = new OptionInfo(field, optAnno);
					for (String opt : opts) {
						String key = opt.replaceFirst("^(-)+", "");
						if (map.containsKey(key)) {
							throw new OptParseException(String.format("Conflict option '%s' found in '%s' and '%s'", opt, map.get(key).field, field));
						}
						map.put(key, optionInfo);
					}
				}
			}
		}
	}

	String help(boolean showNotes) {
		StringBuilder sb = new StringBuilder();
		String cmdDesc = OptUtils.format(anno.descriptions(), false);
		sb.append(cmdDesc);

		List<String> list = new ArrayList<>(map.size());
		for (OptionInfo oi : new HashSet<>(map.values())) {
			if (!oi.anno.hidden()) {
				list.add(oi.help());
			}
		}

		Collections.sort(list, OptUtils.OPT_COMPARATOR);
		for (String string : list) {
			sb.append("\n").append(string);
		}

		if (showNotes) {
			sb.append(OptUtils.format(anno.notes(), true));
		}

		return sb.toString();
	}
}