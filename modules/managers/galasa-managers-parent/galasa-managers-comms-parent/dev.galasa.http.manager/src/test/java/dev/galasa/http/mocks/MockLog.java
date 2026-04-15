/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http.mocks;

import org.apache.commons.logging.Log;

/**
 * A mock implementation of the Log interface for testing purposes.
 */
public class MockLog implements Log {
    private StringBuilder logMessages = new StringBuilder();

    @Override
    public void debug(Object message) {
        logMessages.append("DEBUG: ").append(message).append("\n");
    }

    @Override
    public void debug(Object message, Throwable t) {
        logMessages.append("DEBUG: ").append(message).append(" - ").append(t.getMessage()).append("\n");
    }

    @Override
    public void error(Object message) {
        logMessages.append("ERROR: ").append(message).append("\n");
    }

    @Override
    public void error(Object message, Throwable t) {
        logMessages.append("ERROR: ").append(message).append(" - ").append(t.getMessage()).append("\n");
    }

    @Override
    public void fatal(Object message) {
        logMessages.append("FATAL: ").append(message).append("\n");
    }

    @Override
    public void fatal(Object message, Throwable t) {
        logMessages.append("FATAL: ").append(message).append(" - ").append(t.getMessage()).append("\n");
    }

    @Override
    public void info(Object message) {
        logMessages.append("INFO: ").append(message).append("\n");
    }

    @Override
    public void info(Object message, Throwable t) {
        logMessages.append("INFO: ").append(message).append(" - ").append(t.getMessage()).append("\n");
    }

    @Override
    public void trace(Object message) {
        logMessages.append("TRACE: ").append(message).append("\n");
    }

    @Override
    public void trace(Object message, Throwable t) {
        logMessages.append("TRACE: ").append(message).append(" - ").append(t.getMessage()).append("\n");
    }

    @Override
    public void warn(Object message) {
        logMessages.append("WARN: ").append(message).append("\n");
    }

    @Override
    public void warn(Object message, Throwable t) {
        logMessages.append("WARN: ").append(message).append(" - ").append(t.getMessage()).append("\n");
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isFatalEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }
}