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
 * Indicates the test has multiple Test Variations
 * 
 *  
 *
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface TestVariations {

    TestVariation[] value();
}
