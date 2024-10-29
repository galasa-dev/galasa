/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.impl;

import org.apache.commons.logging.Log;

import dev.galasa.extensions.common.api.LogFactory;

// We have our own LogFactory interface, so that unit tests find it easy to inject their own logger, 
// and capture the logs made by the production code for checking.
// This implementation of the interface just delegates to the apache commons logger.
public class LogFactoryImpl implements LogFactory {

    @Override
    public Log getLog(Class<?> clazz) {
        return org.apache.commons.logging.LogFactory.getLog(clazz);
    }
    
}
