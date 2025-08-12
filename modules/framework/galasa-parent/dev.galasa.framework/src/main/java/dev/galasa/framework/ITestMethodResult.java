/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

/**
 * Represents the result of a single Galasa method execution.
 * <p>
 * An {@code ITestMethodResult} instance provides details about a method including the name, 
 * whether it passed or failed, and if it failed, the exception that caused the failure.
 * A single test method result can be retrieved from a list of multiple test method results
 * that have already run with {@link ITestResultProvider#getTestMethodResults()}.
 */
public interface ITestMethodResult {

    /**
     * Returns the name of a method associated with this result.
     * 
     * @return the method name.
     */
    String getMethodName();

    /**
     * Indicates if the method passed successfully.
     * 
     * @return {@code true} if the method passed, and {@code false} otherwise.
     */
    boolean isPassed();

    /**
     * Indicates if the method failed.
     * 
     * @return {@code true} if the method failed, and {@code false} otherwise.
     */
    boolean isFailed();

    /**
     * Returns the exception that caused the method to fail, if applicable.
     * 
     * @return the {@link Throwable} that caused the failure, or {@code null} if the method passed
     * or did not throw an exception.
     */
    Throwable getFailureReason();
}
