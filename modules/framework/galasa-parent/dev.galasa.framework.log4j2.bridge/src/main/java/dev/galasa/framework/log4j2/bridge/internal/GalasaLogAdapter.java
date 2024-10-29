/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.log4j2.bridge.internal;

import org.apache.commons.logging.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.AbstractLoggerAdapter;
import org.apache.logging.log4j.spi.LoggerContext;

public class GalasaLogAdapter extends AbstractLoggerAdapter<Log> {

    @Override
    protected Log newLogger(final String name, final LoggerContext context) {
        return new GalasaLog(context.getLogger(name));
    }

    @Override
    protected LoggerContext getContext() {
        return LogManager.getContext();
    }

}
