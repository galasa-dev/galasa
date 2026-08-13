/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.net.*;

public interface PluginCommonFactory<Ex extends Exception> {
    AuthenticationService newAuthenticationService(URL apiServerUrl, String galasaAccessToken, CloseableHttpClient httpClient) throws AuthenticationException ;
    
    UrlCalculator<Ex> newUrlCalculator(ErrorRaiser<Ex> errorRaiser);

    BootstrapLoader<Ex> newBootstrapLoader( WrappedLog log , ErrorRaiser<Ex> errorRaiser );

}
