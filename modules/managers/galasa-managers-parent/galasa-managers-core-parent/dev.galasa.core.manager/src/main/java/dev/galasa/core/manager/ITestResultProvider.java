/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.core.manager;

import javax.validation.constraints.NotNull;

import java.util.List;

import dev.galasa.framework.IResult;
import dev.galasa.framework.ITestMethodResult;

/**
 * Implementations of this interface can provide a test result when asked.
 * 
 * This is used by a Galasa test to find out what the current test result is.
 */
public interface ITestResultProvider {

    /**
     * Gets the overall test class result.
     * 
     * @return The IResult which can be queried for whether the test class has passed or failed at this point.
     * It will never be null.
     * 
     * The returned result can be neither passing nor failing. If the test has not completed, then it won't have passed yet.
     */
    @NotNull IResult getResult();

    /**
     * Gets the list of individual test method results.
     * 
     * @return The list of ITestMethodResults which includes the method name, if it passed, if it failed, and thrown exception.
     */
    List<ITestMethodResult> getTestMethodResults();
}