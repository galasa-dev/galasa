/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http;

import java.net.URI;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

/**
 * HTTP DELETE method.
 * <p>
 * The apache provided HttpDelete does not allow the inclusion
 * of a body, which goes against HTTP specs, which allows it.
 * This simple class now provides the capability.
 */
public class HttpDelete extends HttpUriRequestBase {

    public static final String METHOD_NAME = "DELETE";

    public HttpDelete() {
        super(METHOD_NAME, (URI) null);
    }

    public HttpDelete(final URI uri) {
        super(METHOD_NAME, uri);
    }

    public HttpDelete(final String uri) {
        super(METHOD_NAME, URI.create(uri));
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
