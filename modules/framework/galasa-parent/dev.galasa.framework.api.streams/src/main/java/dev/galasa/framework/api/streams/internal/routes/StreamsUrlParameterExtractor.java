/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.streams.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.GAL5418_INVALID_STREAM_NAME;
import static dev.galasa.framework.api.common.ServletErrorMessage.GAL5419_FAILED_TO_GET_STREAM_NAME_FROM_URL;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;

public class StreamsUrlParameterExtractor {

    protected Pattern pathPattern;

    public StreamsUrlParameterExtractor(Pattern pathPattern) {
        this.pathPattern = pathPattern;
    }

    public String getStreamName(String urlPathInfo) throws InternalServletException {
        try {

            Matcher matcher = pathPattern.matcher(urlPathInfo);

            if (!matcher.matches()) {
                ServletError error = new ServletError(GAL5418_INVALID_STREAM_NAME);
                throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
            }

            String streamName = matcher.group(1);
            return streamName;

        } catch (Exception e) {
            ServletError error = new ServletError(GAL5419_FAILED_TO_GET_STREAM_NAME_FROM_URL);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND, e);
        }
    }

}
