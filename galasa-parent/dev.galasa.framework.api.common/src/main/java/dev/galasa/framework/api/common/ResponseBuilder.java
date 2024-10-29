/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ResponseBuilder {

	protected Log logger  =  LogFactory.getLog(ResponseBuilder.class);

    private List<String> allowedOrigins;
    private final String ORIGIN_HEADER = "Origin";
    private final String CORS_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";

    public ResponseBuilder() {
        this(new SystemEnvironment());
    }

    public ResponseBuilder(Environment env) {
        this.allowedOrigins = getAllowedOriginsAsList(env.getenv(EnvironmentVariables.GALASA_ALLOWED_ORIGINS));
    }

    public HttpServletResponse buildResponse(
        HttpServletRequest req,
		HttpServletResponse resp,
		String contentType,
		String content,
		int status
	) {
		//Set headers for HTTP Response
        resp = buildResponse(req, resp, contentType, status);

		try(PrintWriter out = resp.getWriter()) {
			out.print(content);
			out.flush();
		} catch (Exception ex) {
			// TODO: Should we actually ignore these exceptions ? How bad is this ?
			logger.warn("Failed to build response object. Ignoring.", ex);
		}
		return resp;
	}

	public HttpServletResponse buildResponse(
        HttpServletRequest req,
		HttpServletResponse resp,
		String contentType,
		int status
	) {
		resp = buildResponse(req, resp, status);
		resp.setContentType(contentType);

		return resp;
	}

	public HttpServletResponse buildResponse(
        HttpServletRequest req,
		HttpServletResponse resp,
		int status
	) {
		//Set headers for HTTP Response
		resp.setStatus(status);

        String origin = validateRequestOrigin(req);
        if (origin != null) {
            resp.addHeader(CORS_ALLOW_ORIGIN_HEADER, origin);
        } else {
            logger.info("Not setting '" + CORS_ALLOW_ORIGIN_HEADER + "' header");
        }

		return resp;
	}

    private List<String> getAllowedOriginsAsList(String allowedOriginsStr) {
        List<String> allowedOrigins = new ArrayList<>();
        if (allowedOriginsStr != null) {
            for (String origin : allowedOriginsStr.split(",")) {
                allowedOrigins.add(origin.trim());
            }
        }
        return allowedOrigins;
    }

    private String validateRequestOrigin(HttpServletRequest req) {
        String requestOrigin = req.getHeader(ORIGIN_HEADER);
        if (requestOrigin == null || !isOriginAllowed(requestOrigin)) {
            logger.error("Request origin is not set or is not permitted to receive responses");
            requestOrigin = null;
        }
        return requestOrigin;
    }

    private boolean isOriginAllowed(String requestOrigin) {
        boolean isAllowed = false;
        try {
            // Validate that the provided origin is a valid URI
            new URI(requestOrigin);

            for (String allowedOrigin : allowedOrigins) {
                if (allowedOrigin.startsWith("*")) {
                    // If the allowed origin is of the form '*.example.com', remove the '*' and compare suffixes
                    String allowedOriginSuffix = allowedOrigin.substring(1);
                    isAllowed = requestOrigin.endsWith(allowedOriginSuffix);
                } else {
                    // The allowed origin is of the form '<scheme>://<hostname>' or '<scheme>://<hostname>:<port>',
                    // so compare the origins exactly as they are
                    isAllowed = requestOrigin.equals(allowedOrigin);
                }

                // We've matched the origin, so break out of the loop
                if (isAllowed) {
                    break;
                }
            }
        } catch (URISyntaxException e) {
            logger.error("Invalid request origin provided");
            isAllowed = false;
        }
        return isAllowed;
    }
}