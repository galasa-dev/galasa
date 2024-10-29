/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * Fill this field with a Text Scanner object.
 *
 * Will only populate public {@link ITextScanner} fields.
 *
 * @see ITextScanner
 */
@Retention(RUNTIME)
@Target(FIELD)
@TextScanManagerField
@ValidAnnotatedFields({ ITextScanner.class })
public @interface TextScanner {

}
