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
 * Continue On Test Failure will instruct Galasa that the event
 * of a test method failure, the test will containue to run.
 * This does not affect befores and afters, failures in these will continue to terminate the test.
 * This annotation can be overridden by CPS property framework.continue.on.test.failure.
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface ContinueOnTestFailure {

}
