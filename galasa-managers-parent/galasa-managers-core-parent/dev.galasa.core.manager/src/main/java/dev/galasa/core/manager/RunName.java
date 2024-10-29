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
 * Fill this field with the name of the Test Run. 
 * 
 * Can be used for making resource names unique to this run. 
 * The Test Run will be unique across all Local and Automated runs 
 * that are in the system at that point.
 *
 * Will only populate public {@link java.lang.String} fields.
 */
@Retention(RUNTIME)
@Target(FIELD)
@CoreManagerField
@ValidAnnotatedFields({ String.class })
public @interface RunName {

}
