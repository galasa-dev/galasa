/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates this test has variations that will be entered into the Test Catalog
 * 
 *  
 *
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(TestVariations.class)
public @interface TestVariation {

    String name();

    boolean defaultVariation() default false;

    TestVariationProperty[] properties() default {};

}
