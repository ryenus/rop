package com.github.ryenus.optj;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate the {@link Command} fields with Option. A default option value
 * can be directly set on the annotated field.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Option
{
	/**
	 * The option keys, like {@literal '-f'}, {@literal '--file'}.
	 *
	 * Multiple option keys are supported, but the built-in help information
	 * would only display the first short option and the first long option
	 * if there're many.
	 * @return option keys
	 */
	String[] opt();

	/**
	 * The description of the option, if too long, it would be wrapped
	 * correctly with a 2-space indent starting from the second line.
	 * @return option description
	 */
	String description();

	boolean required() default false;

	/**
	 * Hide the option in the help information
	 * @return whether to hide the option in help information
	 */
	boolean hidden() default false;

	/**
	 * Must read the option from terminal, and do not echo input
	 * @return whether to treat the option as secret data like password
	 */
	boolean secret() default false;

	/**
	 * The prompt to display when reading secret from terminal
	 * @return the prompt for reading input
	 */
	String prompt() default "password: ";
}
