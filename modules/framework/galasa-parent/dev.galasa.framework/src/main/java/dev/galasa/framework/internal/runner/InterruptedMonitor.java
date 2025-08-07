/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.runner;

import dev.galasa.framework.TestRunException;

/**
 * 
 * Implementations of this interface can tell the caller whether their test run has been interrupted or not.
 * 
 * Usually this will mean that a user has cancelled the test run while the test is running.
 */
public interface InterruptedMonitor {
    public boolean isInterrupted() throws TestRunException;
}
