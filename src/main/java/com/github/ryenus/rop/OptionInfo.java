package com.github.ryenus.rop;

import java.lang.reflect.Field;

import com.github.ryenus.rop.OptionParser.Option;

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
		String optsText = OptUtils.formatOpts(anno.opt());
		String descText = OptUtils.format(anno.description(), true);
		return String.format("%s  %s", optsText, descText);
	}
}