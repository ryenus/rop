package com.github.ryenus.optj;

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