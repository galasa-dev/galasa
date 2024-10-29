/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common.impl;

import java.net.URL;

import org.apache.http.client.HttpClient;

import dev.galasa.plugin.common.AuthenticationException;
import dev.galasa.plugin.common.AuthenticationService;
import dev.galasa.plugin.common.BootstrapLoader;
import dev.galasa.plugin.common.ErrorRaiser;
import dev.galasa.plugin.common.PluginCommonFactory;
import dev.galasa.plugin.common.UrlCalculator;
import dev.galasa.plugin.common.WrappedLog;
import dev.galasa.plugin.common.impl.auth.AuthenticationServiceImpl;

public class PluginCommonFactoryImpl<Ex extends Exception> implements PluginCommonFactory<Ex> {

    @Override
    public AuthenticationService newAuthenticationService(URL apiServerUrl, String galasaAccessToken, HttpClient httpClient) throws AuthenticationException {
        return new AuthenticationServiceImpl(apiServerUrl, galasaAccessToken, httpClient);
    }

    @Override
    public UrlCalculator<Ex> newUrlCalculator(ErrorRaiser<Ex> errorRaiser) {
        return new UrlCalculatorImpl<Ex>(errorRaiser);
    }

    @Override
    public BootstrapLoader<Ex> newBootstrapLoader( WrappedLog log , ErrorRaiser<Ex> errorRaiser ) {
        return new BootstrapLoaderImpl<Ex>(log, errorRaiser);
    }
}
