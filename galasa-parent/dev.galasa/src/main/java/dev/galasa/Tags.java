/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used to tag a test.  These tags can be used when requesting tests for running, eg from Jenkins
 * 
 *  
 *
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Tags {

    String[] value();
}
