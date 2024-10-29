/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.core.manager;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * <p>
 * Fill this field a test property from the Configuration Property Store
 * </p>
 * <p>
 * Will only populate public {@link String} fields.
 * </p>
 *
 * @see {@link String}
 *  
 *
 */
@Retention(RUNTIME)
@Target(FIELD)
@CoreManagerField
@ValidAnnotatedFields({ String.class })
public @interface TestProperty {
	
	String prefix();
	String suffix();
	String[] infixes() default {};
	
	boolean required() default true;

}
