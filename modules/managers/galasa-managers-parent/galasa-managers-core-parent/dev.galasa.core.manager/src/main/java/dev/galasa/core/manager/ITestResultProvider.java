/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.core.manager;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.IResult;

/**
 * Implementations of this interface can provide a test result when asked.
 * 
 * This is used by a Galasa test to find out what the current test result is.
 */
public interface ITestResultProvider {

    /**
     * Gets the test result.
     * 
     * @return The IResult which can be queried for whether the test has passed or failed at this point.
     * It will never be null.
     * 
     * The returned result can be neither passing nor failing. If the test has not completed, then it won't have passed yet.
     */
    @NotNull IResult getResult();
}