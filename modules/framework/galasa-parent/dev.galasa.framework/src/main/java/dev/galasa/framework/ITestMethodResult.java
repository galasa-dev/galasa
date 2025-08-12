/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

public interface ITestMethodResult {
    String getMethodName();
    boolean isPassed();
    boolean isFailed();
    Throwable getThrowable();
}
