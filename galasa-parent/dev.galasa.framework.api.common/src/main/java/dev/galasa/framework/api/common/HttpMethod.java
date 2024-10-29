/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

/**
 * A simple enum consisting of all possible HTTP request methods.
 */
public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    HEAD,
    PATCH,
    TRACE,
    OPTIONS,
    CONNECT
    ;

    public static HttpMethod getFromString(String httpMethodStr) {
        HttpMethod match = null;
        for (HttpMethod possibleMatch : values()) {
            if (possibleMatch.toString().equalsIgnoreCase(httpMethodStr)) {
                match = possibleMatch;
                break;
            }
        }
        return match;
    }
}
