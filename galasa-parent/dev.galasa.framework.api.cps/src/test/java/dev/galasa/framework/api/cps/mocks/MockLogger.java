/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.mocks;

import org.apache.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;

public class MockLogger implements Log {

    private boolean isDebugEnabled = true;
    private boolean isErrorEnabled = true;
    private boolean isFatalEnabled = true;
    private boolean isInfoEnabled  = true;
    private boolean isTraceEnabled  = true;
    private boolean isWarnEnabled  = true;

    private List<LogRecord> logContent = new ArrayList<LogRecord>();

    public LogRecord getFirstLogRecordContainingText(String textToLookFor) {
        for (LogRecord record : logContent ) {
            if (record.getText().contains(textToLookFor)) {
                return record ;
            }
        }
        return null ;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        isDebugEnabled = debugEnabled;
    }

    public void setErrorEnabled(boolean errorEnabled) {
        isErrorEnabled = errorEnabled;
    }

    public void setFatalEnabled(boolean fatalEnabled) {
        isFatalEnabled = fatalEnabled;
    }

    public void setInfoEnabled(boolean infoEnabled) {
        isInfoEnabled = infoEnabled;
    }

    public void setTraceEnabled(boolean traceEnabled) {
        isTraceEnabled = traceEnabled;
    }

    public void setWarnEnabled(boolean warnEnabled) {
        isWarnEnabled = warnEnabled;
    }

    public List<LogRecord> getLogRecords() {
        return this.logContent;
    }

    @Override
    public void debug(Object message) {
        logContent.add( new LogRecord( LogRecordType.DEBUG , message.toString() ));
    }

    @Override
    public void debug(Object message, Throwable t) {
        logContent.add( new LogRecord( LogRecordType.DEBUG , message.toString() , t ));
    }

    @Override
    public void error(Object message) {
        logContent.add( new LogRecord( LogRecordType.ERROR , message.toString() ));
    }

    @Override
    public void error(Object message, Throwable t) {
        logContent.add( new LogRecord( LogRecordType.ERROR , message.toString() , t ));
    }

    @Override
    public void fatal(Object message) {
        logContent.add( new LogRecord( LogRecordType.FATAL , message.toString() ));
    }

    @Override
    public void fatal(Object message, Throwable t) {
        logContent.add( new LogRecord( LogRecordType.FATAL , message.toString() , t ));
    }

    @Override
    public void info(Object message) {
        logContent.add( new LogRecord( LogRecordType.INFO , message.toString() ));
    }

    @Override
    public void info(Object message, Throwable t) {
        logContent.add( new LogRecord( LogRecordType.INFO , message.toString() ,t));
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
        logContent.add( new LogRecord( LogRecordType.TRACE , message.toString() ));
    }

    @Override
    public void trace(Object message, Throwable t) {
        logContent.add( new LogRecord( LogRecordType.TRACE , message.toString() ,t));
    }

    @Override
    public void warn(Object message) {
        logContent.add( new LogRecord( LogRecordType.WARNING , message.toString() ));
    }

    @Override
    public void warn(Object message, Throwable t) {
        logContent.add( new LogRecord( LogRecordType.WARNING , message.toString() ,t));
    }
}
