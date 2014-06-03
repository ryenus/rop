package com.github.ryenus.rop;

import java.lang.reflect.Field;

import com.github.ryenus.rop.OptionParser.Option;

public class OptionInfo {
	Field field;
	Option anno;
	boolean set;

	public OptionInfo(Field field, Option optAnno) {
		this.field = field;
		this.anno = optAnno;
		this.set = false;
	}

	public Field getField() {
		return field;
	}

	public Option getAnno() {
		return anno;
	}

	public boolean isSet() {
		return set;
	}

	public String help() {
		String optsText = Utils.formatOpts(anno.opt());
		String descText = Utils.format(anno.description(), true);
		return String.format("%s  %s", optsText, descText);
	}
}