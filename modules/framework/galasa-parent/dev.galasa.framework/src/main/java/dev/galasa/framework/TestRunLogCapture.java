/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.ResultArchiveStoreException;

public class TestRunLogCapture implements Appender {

    private final Framework         framework;

    private IResultArchiveStore     ras;

    private final ArrayList<String> startupCache = new ArrayList<>();
    private PatternLayout           layout       = PatternLayout.newBuilder().withPattern("%d{HH:mm:ss} %p [%t] %c - %m%n").build();
    private Level                   minimumLevel = Level.ALL;

    private boolean                 shutdown     = false;

    private State                   state        = State.STOPPED;

    public TestRunLogCapture(Framework framework) {
        this.framework = framework;

        LoggerContext ctx = (LoggerContext) LogManager.getContext();
        Configuration config = ctx.getConfiguration();

        Appender stdout = config.getAppender("stdout");
        if (stdout != null) {
            this.layout = (PatternLayout) stdout.getLayout();
        }

        start();
        
        config.addAppender(this);
        config.getRootLogger().addAppender(this, null, null);
        for (final LoggerConfig loggerConfig : config.getLoggers().values()) {
            loggerConfig.addAppender(this, null, null);
        }
    }

    public void shutdown() {
        this.shutdown = true;
    }


    @Override
    public void append(LogEvent event) {
        if (this.shutdown) {
            return;
        }

        if (!event.getLevel().isMoreSpecificThan(minimumLevel)) {
            return;
        }

        String message = this.layout.toSerializable(event);
        String throwable = null;

        if (event.getThrownProxy() != null) {
            throwable = event.getThrownProxy().getExtendedStackTraceAsString();
        }

        if (ras == null) {
            if (framework.isInitialised()) {
                this.ras = framework.getResultArchiveStore();
            } else {
                startupCache.add(message);
                if (throwable != null) {
                    startupCache.add(throwable);
                }
                return;
            }
        }

        if (!startupCache.isEmpty()) {
            try {
                this.ras.writeLog(startupCache);
                this.startupCache.clear();
            } catch (ResultArchiveStoreException e) {
                e.printStackTrace(); // *** Do not use logger, will cause a loop //NOSONAR
                startupCache.add(message);
                return;
            }
        }

        try {
            this.ras.writeLog(message);
            if (throwable != null) {
                this.ras.writeLog(throwable);
            }
        } catch (ResultArchiveStoreException e) {
            e.printStackTrace(); // *** Do not use logger, will cause a loop //NOSONAR
            startupCache.add(message);
        }
    }

    @Override
    public State getState() {
        return this.state;
    }

    @Override
    public void initialize() {
        this.state = State.INITIALIZED;
    }

    @Override
    public boolean isStarted() {
        return this.state == State.STARTED;
    }

    @Override
    public boolean isStopped() {
        return this.state == State.STOPPED;
    }

    @Override
    public void start() {
        this.state = State.STARTED;
    }

    @Override
    public void stop() {
        this.state = State.STOPPED;
    }


    @Override
    public ErrorHandler getHandler() {
        return null;
    }

    @Override
    public Layout<? extends Serializable> getLayout() {
        return null;
    }

    @Override
    public String getName() {
        return "galasa-appender";
    }

    @Override
    public boolean ignoreExceptions() {
        return false;
    }

    @Override
    public void setHandler(ErrorHandler handler) {
    }


}