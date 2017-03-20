package com.github.ryenus.optj;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a class as Command to use it with {@link OptionParser}.
 *
 * <p>The descriptions and notes will be used to make up the help information.
 * When crafted well, the descriptions and/or notes could span multiple
 * paragraphs, as well as indented list items, thus to provide a well
 * explained help.
 *
 * @see Command#descriptions()
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command
{
	String name();

	/**
	 * Command descriptions are automatically included in the help
	 * information before options list.
	 *
	 * <p>
	 * With an array of sentences, by prefixing an item with a {@code '\n'}
	 * character, a new line would be inserted above it to separated from
	 * the previous one hence make it a new paragraph.
	 * </p>
	 *
	 * <pre>
	 * descriptions                         result
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * {"statement 1",                      statement 1
	 *  "\nstatement 2"}
	 *                                      statement 2
	 * -------------------------------------------------------------------
	 * {"statement 1",                      statement 1
	 *  " space indented item A",            space indented item A
	 *  "\ttab indented item B",                    tab indented item B
	 *  "\n separated &amp; indented item C"}
	 *                                       separated &amp; indented item C
	 * </pre>
	 *
	 * If a line contains than 80 characters, the line would be
	 * automatically wrapped near the 80th column.
	 *
	 * @return Command descriptions
	 */
	String[] descriptions() default {};

	/**
	 * Command notes are automatically included in the help information
	 * after options list.
	 *
	 * <p>
	 * As with {@link #descriptions()}, the same trick can be used to
	 * separate paragraphs
	 * @return Command usage notes
	 */
	String[] notes() default {};
}
