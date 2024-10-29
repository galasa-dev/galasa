/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;

public class MockLog implements Log {

    public static final Throwable NO_THROWABLE = null;

    private boolean isDebugEnabled = true ;
    private boolean isErrorEnabled = true ;
    private boolean isFatalEnabled = true ;
    private boolean isInfoEnabled  = true ;
    private boolean isWarnEnabled  = true ;
    private boolean isTraceEnabled = true ;

    List<MockLogRecord> lines = new LinkedList<MockLogRecord>();

    public MockLog() {
        this(true,true,true,true,true,true);
    }

    public MockLog(
        boolean isDebugEnabled, 
        boolean isErrorEnabled, 
        boolean isFatalEnabled, 
        boolean isInfoEnabled, 
        boolean isWarnEnabled,
        boolean isTraceEnabled) {
        this.isDebugEnabled = isDebugEnabled;
        this.isErrorEnabled = isErrorEnabled;
        this.isFatalEnabled = isFatalEnabled;
        this.isInfoEnabled = isInfoEnabled;
        this.isWarnEnabled = isWarnEnabled ;
        this.isTraceEnabled = isTraceEnabled;
    }

    @Override
    public void debug(Object message) {
        this.debug(message,null);
    }

    @Override
    public void debug(Object message, Throwable t) {
        MockLogRecord record = new MockLogRecord(MockLogRecordType.DEBUG , message, t);
        lines.add(record);
    }

    @Override
    public void error(Object message) {
        MockLogRecord record = new MockLogRecord(MockLogRecordType.ERROR , message, NO_THROWABLE);
        lines.add(record);
    }

    @Override
    public void error(Object message, Throwable t) {
        MockLogRecord record = new MockLogRecord(MockLogRecordType.ERROR , message, t);
        lines.add(record);
    }

    @Override
    public void fatal(Object message) {
        MockLogRecord record = new MockLogRecord(MockLogRecordType.FATAL , message, NO_THROWABLE);
        lines.add(record);
    }

    @Override
    public void fatal(Object message, Throwable t) {
        MockLogRecord record = new MockLogRecord(MockLogRecordType.FATAL , message, t);
        lines.add(record);
    }

    @Override
    public void info(Object message) {
        MockLogRecord record = new MockLogRecord(MockLogRecordType.INFO , message, NO_THROWABLE);
        lines.add(record);
    }

    @Override
    public void info(Object message, Throwable t) {
        MockLogRecord record = new MockLogRecord(MockLogRecordType.FATAL , message, t);
        lines.add(record);
    }

    @Override
    public boolean isDebugEnabled() {
        return this.isDebugEnabled;
    }

    @Override
    public boolean isErrorEnabled() {
        return this.isErrorEnabled;
    }

    @Override
    public boolean isFatalEnabled() {
        return this.isFatalEnabled;
    }

    @Override
    public boolean isInfoEnabled() {
        return this.isInfoEnabled;
    }

    @Override
    public boolean isTraceEnabled() {
        return this.isTraceEnabled;
    }

    @Override
    public boolean isWarnEnabled() {
        return this.isWarnEnabled;
    }

    @Override
    public void trace(Object message) {
        MockLogRecord record = new MockLogRecord(MockLogRecordType.TRACE , message, NO_THROWABLE);
        lines.add(record);
    }

    @Override
    public void trace(Object message, Throwable t) {
        MockLogRecord record = new MockLogRecord(MockLogRecordType.TRACE , message, t);
        lines.add(record);
    }

    @Override
    public void warn(Object message) {
        MockLogRecord record = new MockLogRecord(MockLogRecordType.WARN , message, NO_THROWABLE);
        lines.add(record);
    }

    @Override
    public void warn(Object message, Throwable t) {
        MockLogRecord record = new MockLogRecord(MockLogRecordType.WARN , message, t);
        lines.add(record);
    }

    public void setDebugEnabled(boolean isDebugEnabled) {
        this.isDebugEnabled = isDebugEnabled;
    }

    public void setErrorEnabled(boolean isErrorEnabled) {
        this.isErrorEnabled = isErrorEnabled;
    }

    public void setFatalEnabled(boolean isFatalEnabled) {
        this.isFatalEnabled = isFatalEnabled;
    }

    public void setInfoEnabled(boolean isInfoEnabled) {
        this.isInfoEnabled = isInfoEnabled;
    }

    public void setWarnEnabled(boolean isWarnEnabled) {
        this.isWarnEnabled = isWarnEnabled;
    }

    public void setTraceEnabled(boolean isTraceEnabled) {
        this.isTraceEnabled = isTraceEnabled;
    }

    public boolean contains(String containsSubString) {
        boolean contains = false ;
        for (MockLogRecord record : lines) {
            contains = record.getContent().contains(containsSubString);
            if (contains) {
                break;
            }
        }
        return contains;
    }
    
}
