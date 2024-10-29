/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

/**
 * Provide a CPS property to set for a test variation
 * 
 *  
 *
 */
@Retention(RUNTIME)
public @interface TestVariationProperty {

    String property();

    String value();
}
