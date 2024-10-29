/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.mocks;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;

// A logger which funnels any log traffic into a shared string buffer.
public class MockLog implements Log {

    private final PrintWriter output;
    private final String className ;
    private boolean isDebugEnabled = true ;
    private boolean isErrorEnabled = true ;
    private boolean isFatalEnabled = true ;
    private boolean isInfoEnabled = true ;
    private boolean isTraceEnabled = true ;
    private boolean isWarnEnabled = true ;

    public MockLog( PrintWriter output, Class<?> clazz ) {
        this.output = output;
        this.className = clazz.getName();
    }

    private void outputMessage(String message) {
        output.println(message);
        output.flush();
        System.out.println(message);
        System.out.flush();
    }

    private void outputMessage(String type, Object message) {
        outputMessage(MessageFormat.format("{0} : {1} : {2}",this.className,type,message.toString()));
    }

    private void outputMessage(String type, Object message, Throwable t) {
        String messageToLog = MessageFormat.format("{0} : {1} : {2} {3}",this.className,type,message.toString(),t.toString());
        ByteArrayOutputStream stackStream = new ByteArrayOutputStream();
        PrintWriter stackWriter = new PrintWriter(stackStream);
        t.printStackTrace(stackWriter);

        messageToLog += "\n"+stackStream.toString();
        outputMessage(messageToLog);
    }

    @Override
    public void debug(Object message) {
        outputMessage("debug",message);
    }

    @Override
    public void debug(Object message, Throwable t) {
        outputMessage("debug",message,t);
    }

    @Override
    public void error(Object message) {
        outputMessage("error",message);
    }

    @Override
    public void error(Object message, Throwable t) {
        outputMessage("error",message,t);
    }

    @Override
    public void fatal(Object message) {
        outputMessage("fatal",message);
    }

    @Override
    public void fatal(Object message, Throwable t) {
        outputMessage("fatal",message,t);
    }

    @Override
    public void info(Object message) {
        outputMessage("info",message);
    }

    @Override
    public void info(Object message, Throwable t) {
        outputMessage("info",message,t);
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
        return this.isWarnEnabled ;
    }

    @Override
    public void trace(Object message) {
        outputMessage("trace",message);
    }

    @Override
    public void trace(Object message, Throwable t) {
        outputMessage("trace",message,t);
    }

    @Override
    public void warn(Object message) {
        outputMessage("warn",message);
    }

    @Override
    public void warn(Object message, Throwable t) {
        outputMessage("warn",message,t);
    }
}
