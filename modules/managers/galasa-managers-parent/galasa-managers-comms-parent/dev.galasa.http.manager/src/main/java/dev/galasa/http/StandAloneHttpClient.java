/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http;

import org.apache.commons.logging.Log;

import dev.galasa.http.internal.HttpClientImpl;


/**
 * This is a class to allow the HTTP manager implementations be used without the need for the manager to be active
 */
public class StandAloneHttpClient {

    public static IHttpClient getHttpClient(int timeout, Log log) {
        return new HttpClientImpl(timeout, log);
    }
    
}