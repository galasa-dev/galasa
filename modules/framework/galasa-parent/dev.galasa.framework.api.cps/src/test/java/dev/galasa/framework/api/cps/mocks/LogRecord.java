/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.mocks;

public class LogRecord {
    private String text;
    private LogRecordType type;
    private Throwable cause;

    public LogRecord(LogRecordType type, String text) {
        this(type, text, null);
    }

    public LogRecord(LogRecordType type, String text, Throwable cause) {
        this.text = text;
        this.type = type;
        this.cause = cause;
    }

    public String getText() {
        return this.text;
    }

    public LogRecordType getType() {
        return this.type;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
