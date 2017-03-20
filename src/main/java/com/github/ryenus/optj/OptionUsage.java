package com.github.ryenus.optj;

import java.lang.reflect.Field;

class OptionUsage
{
	Field field;
	Option anno;
	boolean set;

	OptionUsage(Field field, Option optAnno) {
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