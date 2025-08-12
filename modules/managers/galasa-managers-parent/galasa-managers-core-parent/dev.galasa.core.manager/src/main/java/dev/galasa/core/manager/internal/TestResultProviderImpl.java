/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.core.manager.internal;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.core.manager.ITestResultProvider;
import dev.galasa.framework.IResult;
import dev.galasa.framework.ITestMethodResult;

public class TestResultProviderImpl implements ITestResultProvider {

    /** 
     * A simple implementation of a result which is neither pass or failed. So we never return null.
     */
    private class ResultNotSetYetResult implements IResult {

        @Override
        public boolean isPassed() {
            return false;
        }

        @Override
        public boolean isFailed() {
            return false;
        }
    }

    private IResult result = new ResultNotSetYetResult();

    private List<ITestMethodResult> testMethodResults = new ArrayList<ITestMethodResult>();

    public void setResult(IResult newResult) {
        this.result = newResult;
    }

    @Override
    public @NotNull IResult getResult() {
        return this.result;
    }

    public void addTestMethodResult(ITestMethodResult newTestMethodResult) {
        this.testMethodResults.add(newTestMethodResult);
    }

    @Override
    public List<ITestMethodResult> getTestMethodResults() {
        return this.testMethodResults;
    }

}