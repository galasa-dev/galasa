/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.maven.plugin;

import org.apache.maven.plugin.logging.Log;

import dev.galasa.plugin.common.WrappedLog;

public class WrappedLogMaven implements WrappedLog {

    private Log log ;

    public WrappedLogMaven(Log log) {
        this.log = log;
    }

    @Override
    public void info(String message) {
        log.info(message);
    }

    @Override
    public void error(String message) {
        log.error(message);
    }

    @Override
    public void warn(String message) {
        log.warn(message);
    }
    
}
