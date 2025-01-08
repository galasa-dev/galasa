/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal.routes;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.ResponseBuilder;

/**
 * An abstract route class containing common methods used by auth routes.
 */
public abstract class AbstractAuthRoute extends BaseRoute {

    // Suffix for the DSS auth property 'dss.auth.STATEID.callback.url'
    public static final String DSS_CALLBACK_URL_PROPERTY_SUFFIX = ".callback.url";

    public AbstractAuthRoute(ResponseBuilder responseBuilder, String path) {
        super(responseBuilder, path);
    }

    /**
     * Checks if a given URL is a valid URL.
     */
    protected boolean isUrlValid(String url) {
        boolean isValid = false;
        try {
            new URL(url).toURI();
            isValid = true;
            logger.info("Valid URL provided: '" + url + "'");
        } catch (URISyntaxException | MalformedURLException e) {
            logger.error("Invalid URL provided: '" + url + "'");
        }
        return isValid;
    }

    protected String sanitizeString(String paramValue) {
        String cleanValue = null;
        if (paramValue != null) {
            cleanValue = paramValue.trim()
                .replaceAll("\n", "")
                .replaceAll("\r", "");
        }
        return cleanValue;
    }
}
