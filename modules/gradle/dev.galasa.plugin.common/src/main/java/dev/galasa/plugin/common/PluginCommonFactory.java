/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common;

import org.apache.http.client.HttpClient;

import java.net.*;

public interface PluginCommonFactory<Ex extends Exception> {
    AuthenticationService newAuthenticationService(URL apiServerUrl, String galasaAccessToken, HttpClient httpClient) throws AuthenticationException ;
    
    UrlCalculator<Ex> newUrlCalculator(ErrorRaiser<Ex> errorRaiser);

    BootstrapLoader<Ex> newBootstrapLoader( WrappedLog log , ErrorRaiser<Ex> errorRaiser );

}
