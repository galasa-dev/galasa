/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.gradle.testcatalog;

import org.gradle.api.logging.Logger;

import dev.galasa.plugin.common.WrappedLog;

public class WrappedLogGradle implements WrappedLog {
    
    private Logger log ;

    public WrappedLogGradle(Logger log) {
        this.log = log ;
    }

    @Override
    public void error(String msg) {
        log.error(msg);
    }

    @Override
    public void info(String msg) {
        log.info(msg);
    }

    @Override
    public void warn(String msg) {
        log.warn(msg);
    }
}
