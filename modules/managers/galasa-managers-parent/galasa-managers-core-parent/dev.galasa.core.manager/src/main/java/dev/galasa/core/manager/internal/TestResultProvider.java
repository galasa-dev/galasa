/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.core.manager.internal;

import dev.galasa.core.manager.ITestResultProvider;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IRun;

public class TestResultProvider implements ITestResultProvider {

    private IFramework framework;

    public TestResultProvider(IFramework framework) {
        this.framework = framework;
    }

    public IFramework getFramework() {
        return this.framework;
    }

    public IRun getTestRun() {
        return this.framework.getTestRun();
    }

    public String getResult() {
        return this.framework.getTestRun().getResult();
    }

    public boolean isPassed() {
        return (getResult().equals("Passed"));
    }

    public boolean isFailed() {
        return (getResult().equals("Failed"));
    }

}
