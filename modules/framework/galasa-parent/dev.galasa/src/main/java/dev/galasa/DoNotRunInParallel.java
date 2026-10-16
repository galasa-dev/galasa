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
 * Marks a test class as serial: at most one instance of this class may run
 * across the entire ecosystem at any one time.
 *
 * The Galasa scheduler enforces this by holding a DSS lock for the test class
 * while a run is active.  Any other run of the same class stays {@code queued}
 * until the lock is released.
 *
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface DoNotRunInParallel {
}
