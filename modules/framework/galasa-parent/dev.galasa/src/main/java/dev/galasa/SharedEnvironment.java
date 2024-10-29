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
 * Indicates that this Class setups up and validates a Shared Environment.
 * The structure is the same as a {@literal @}Test class
 */
@Retention(RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface SharedEnvironment {
    
    /**
     * How many hours after the Shared Environment was built should the Run be discarded, should only
     * matter if the discard was run properly after all the testing has been completed
     * @return The number of hours to keep the Run
     */
    int expireAfterHours() default 8;

}
