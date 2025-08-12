/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import dev.galasa.framework.ITestMethodResult;

public class TestMethodResult implements ITestMethodResult {

    private String name;
    private boolean isPassed;
    private boolean isFailed;
    private Throwable throwable;

    public TestMethodResult(String name, boolean passed, boolean failed, Throwable throwable) {
        this.name = name;
        this.isPassed = passed;
        this.isFailed = failed;
        this.throwable = throwable;
    }

    public String getMethodName() {
        return this.name;
    }

    public boolean isPassed() {
        return this.isPassed;
    }

    public boolean isFailed() {
        return this.isFailed;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

}
