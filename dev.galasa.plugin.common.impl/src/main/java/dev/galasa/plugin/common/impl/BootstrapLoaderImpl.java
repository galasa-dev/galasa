/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common.impl;

import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.Properties;

import dev.galasa.plugin.common.BootstrapLoader;
import dev.galasa.plugin.common.ErrorRaiser;
import dev.galasa.plugin.common.WrappedLog;

public class BootstrapLoaderImpl<Ex extends Exception> implements BootstrapLoader<Ex> {
    WrappedLog log ;
    ErrorRaiser<Ex> errorRaiser ;

    public BootstrapLoaderImpl( WrappedLog log , ErrorRaiser<Ex> errorRaiser ) {
        this.log = log ;
        this.errorRaiser = errorRaiser;
    }

    @Override
    public Properties getBootstrapProperties(URL bootstrapUrl) throws Ex {
        Properties bootstrapProperties = new Properties();
        try {
            URLConnection connection = bootstrapUrl.openConnection();
            String msg = MessageFormat.format("execute(): URLConnection: connected to:{0}",connection.getURL().toString());
            log.info(msg);
            bootstrapProperties.load(connection.getInputStream());
            log.info("execute(): bootstrapProperties loaded: " + bootstrapProperties);
        } catch (Exception ex) {
            errorRaiser.raiseError(ex,"execute() - Unable to load bootstrap properties, Reason: {0}",ex.getMessage());
        }
        return bootstrapProperties;
    }
}
