/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * This annotation is being deprecated and replaced with a more appropriately named @WebDriver. There is no functionality differences between the two
 * 
 *  
 *
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@SeleniumManagerField
@ValidAnnotatedFields({ ISeleniumManager.class })
public @interface SeleniumManager {
    public Browser browser() default Browser.NOTSPECIFIED;
}