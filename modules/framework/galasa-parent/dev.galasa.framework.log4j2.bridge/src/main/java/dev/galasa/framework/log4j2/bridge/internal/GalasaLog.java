/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.log4j2.bridge.internal;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.spi.ExtendedLogger;

public class GalasaLog implements Log, Serializable {
    private static final long serialVersionUID = 1L;
    private static final String FQCN = GalasaLog.class.getName();

    private final ExtendedLogger logger;

    public GalasaLog(final ExtendedLogger logger) {
        this.logger = logger;
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isEnabled(Level.DEBUG, null, null);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isEnabled(Level.ERROR, null, null);
    }

    @Override
    public boolean isFatalEnabled() {
        return logger.isEnabled(Level.FATAL, null, null);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isEnabled(Level.INFO, null, null);
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isEnabled(Level.TRACE, null, null);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isEnabled(Level.WARN, null, null);
    }

    @Override
    public void trace(final Object message) {
        logger.logIfEnabled(FQCN, Level.TRACE, null, message, null);
    }

    @Override
    public void trace(final Object message, final Throwable t) {
        logger.logIfEnabled(FQCN, Level.TRACE, null, message, t);
    }

    @Override
    public void debug(final Object message) {
        logger.logIfEnabled(FQCN, Level.DEBUG, null, message, null);
    }

    @Override
    public void debug(final Object message, final Throwable t) {
        logger.logIfEnabled(FQCN, Level.DEBUG, null, message, t);
    }

    @Override
    public void info(final Object message) {
        logger.logIfEnabled(FQCN, Level.INFO, null, message, null);
    }

    @Override
    public void info(final Object message, final Throwable t) {
        logger.logIfEnabled(FQCN, Level.INFO, null, message, t);
    }

    @Override
    public void warn(final Object message) {
        logger.logIfEnabled(FQCN, Level.WARN, null, message, null);
    }

    @Override
    public void warn(final Object message, final Throwable t) {
        logger.logIfEnabled(FQCN, Level.WARN, null, message, t);
    }

    @Override
    public void error(final Object message) {
        logger.logIfEnabled(FQCN, Level.ERROR, null, message, null);
    }

    @Override
    public void error(final Object message, final Throwable t) {
        logger.logIfEnabled(FQCN, Level.ERROR, null, message, t);
    }

    @Override
    public void fatal(final Object message) {
        logger.logIfEnabled(FQCN, Level.FATAL, null, message, null);
    }

    @Override
    public void fatal(final Object message, final Throwable t) {
        logger.logIfEnabled(FQCN, Level.FATAL, null, message, t);
    }
}