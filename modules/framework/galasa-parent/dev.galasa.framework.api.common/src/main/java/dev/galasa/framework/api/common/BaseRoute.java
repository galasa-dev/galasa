/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonElement;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.utils.GalasaGson;

import static dev.galasa.framework.api.common.MimeType.*;
import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class BaseRoute implements IRoute {

    protected static final GalasaGson gson = new GalasaGson();

    protected Log logger = LogFactory.getLog(this.getClass());

    private final ResponseBuilder responseBuilder;

    private final Pattern pathRegex;
    public BaseRoute(ResponseBuilder responseBuilder, String path) {
        this.pathRegex = Pattern.compile(path);
		this.responseBuilder = responseBuilder;
    }

    @Override
    public Pattern getPathRegex() {
        return pathRegex;
    }

	public ResponseBuilder getResponseBuilder() {
		return this.responseBuilder;
	}

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams, 
            HttpRequestContext requestContext, HttpServletResponse response) 
            throws ServletException, IOException, FrameworkException {
        throwMethodNotAllowedException(requestContext, pathInfo);
        return response;
    }

    @Override
    public HttpServletResponse handlePutRequest(String pathInfo,
            HttpRequestContext requestContext, HttpServletResponse response) 
            throws ServletException, IOException, FrameworkException {
        throwMethodNotAllowedException(requestContext, pathInfo);
        return response;
    }

    @Override
    public HttpServletResponse handlePostRequest(String pathInfo,
            HttpRequestContext requestContext, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {
        throwMethodNotAllowedException(requestContext, pathInfo);
        return response;
    }

    @Override
    public HttpServletResponse handleDeleteRequest(String pathInfo,
            HttpRequestContext requestContext, HttpServletResponse response) 
            throws ServletException, IOException, FrameworkException {
        throwMethodNotAllowedException(requestContext, pathInfo);
        return response;
    }

    private void throwMethodNotAllowedException(HttpRequestContext requestContext, String pathInfo) throws InternalServletException {
        HttpServletRequest request = requestContext.getRequest();
        ServletError error = new ServletError(GAL5405_METHOD_NOT_ALLOWED, pathInfo, request.getMethod());
        throw new InternalServletException(error, HttpServletResponse.SC_METHOD_NOT_ALLOWED);        
    }

    protected boolean checkRequestHasContent(HttpServletRequest request) throws InternalServletException {
        boolean valid = false;
        try {
            if (request.getContentLength() > 0){
                valid = true;
            }
        } catch (NullPointerException e ){
            // Catch the NullPointerException (empty request body) to throw error in if 
        }  
        if (!valid){
            ServletError error = new ServletError(GAL5411_NO_REQUEST_BODY, request.getPathInfo());
            throw new InternalServletException(error, HttpServletResponse.SC_LENGTH_REQUIRED);
        }
        return valid;
    }

    /**
     * Parses a given HTTP request's body into a bean object
     * 
     * @param request the HTTP request to be parsed
     * @param clazz the bean class to parse the request body into
     * @return the parsed HTTP request body
     * @throws IOException if there is an issue reading the request body
     * @throws InternalServletException if the request does not contain a body
     */
    protected <T> T parseRequestBody(HttpServletRequest request, Class<T> clazz) throws IOException, InternalServletException {
        StringBuilder sbRequestBody = new StringBuilder();
        checkRequestHasContent(request);

        try (BufferedReader bodyReader = request.getReader()) {
            String line = bodyReader.readLine();
            while (line != null) {
                sbRequestBody.append(line);
                line = bodyReader.readLine();
            }
        }
        return gson.fromJson(sbRequestBody.toString(), clazz);
    }

    /**
     * Checks the json element to make sure it is not a NULL value or an empty object
     * @param element The json element we want to check
     * @throws InternalServletException if the json element is empty or null
     */
    protected void checkJsonElementIsValidJSON(JsonElement element) throws InternalServletException{
        if ( element.isJsonNull()){
            ServletError error = new ServletError(GAL5067_NULL_RESOURCE_IN_BODY);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        if ( element.getAsJsonObject().entrySet().isEmpty()){
            ServletError error = new ServletError(GAL5068_EMPTY_JSON_RESOURCE_IN_BODY);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Checks if a given HTTP request contains an "Accept" header and if the values of that 
     * header are part of the default or the supplied list. If no supported types are given,
     * then "application/json" will be assumed as the default supported type.
     * 
     * @param request the HTTP request to be validated
     * @param supportedTypes the MIME types that are supported by the route
     * @throws InternalServletException if an unsupported Accept header value was provided
     */
    protected void validateAcceptHeader(HttpServletRequest request, MimeType... supportedTypes) throws InternalServletException {
        List<MimeType> supportedMimeTypes;
        if (supportedTypes != null) {
            supportedMimeTypes = Arrays.asList(supportedTypes);
        } else {
            // Default to supporting application/json
            supportedMimeTypes = List.of(APPLICATION_JSON);
        }

        // Try to get a response type using the accepted MIME types given in the "Accept" header
        getResponseType(request.getHeader("Accept"), APPLICATION_JSON, supportedMimeTypes);
    }

    protected String getResponseType(String requestedAcceptTypes, MimeType defaultType, List<MimeType> supportedTypes) throws InternalServletException {
        // If no "Accept" header value is set, then use the given default type
        String responseContentType = defaultType.toString();
        if (requestedAcceptTypes != null) {

            // Find the matching types that are supported by the server and were requested by the client
            List<AcceptContentType> supportedAcceptTypes = getSupportedTypesFromAcceptHeader(requestedAcceptTypes, supportedTypes);

            if (!supportedAcceptTypes.isEmpty()) {
                String responseType = supportedAcceptTypes.get(0).getType();

                // If something like "application/*" or "*/*" is given, then we'll use the default subtype for the wildcard type
                if (responseType.contains("*")) {
                    responseContentType = WildcardMimeType.getFromString(responseType).getDefaultSubtype();
                } else {
                    responseContentType = responseType;
                }
            } else {
                // The "Accept" header only contained types that aren't supported, so throw an error
                String supportedContentTypesStr = supportedTypes.stream()
                    .map(MimeType::toString)
                    .collect(Collectors.joining(", "));

                ServletError error = new ServletError(GAL5406_UNSUPPORTED_CONTENT_TYPE_REQUESTED, supportedContentTypesStr);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_ACCEPTABLE);
            }
        }
        return responseContentType;
    }

    private List<AcceptContentType> getSupportedTypesFromAcceptHeader(String acceptHeaderContent, List<MimeType> supportedTypes) {
        List<AcceptContentType> supportedAcceptTypes = new ArrayList<>();
        List<AcceptContentType> parsedAcceptTypes = parseAcceptHeader(acceptHeaderContent);

        for (AcceptContentType acceptType : parsedAcceptTypes) {
            String contentType = acceptType.getType();

            for (MimeType supportedType : supportedTypes) {
                if (supportedType.matchesType(contentType)) {
                    supportedAcceptTypes.add(acceptType);
                    break;
                }
            }
        }

        // Sort the supported types based on their quality values
        supportedAcceptTypes = supportedAcceptTypes.stream()
            .sorted((acceptType1, acceptType2) -> Double.compare(acceptType2.getQuality(), acceptType1.getQuality()))
            .collect(Collectors.toList());

        return supportedAcceptTypes;
    }

    /**
     * Parses the value of an "Accept" HTTP header into a map of content types and their associated quality value,
     * used to assign the priority of given types. See https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept
     * for details on "Accept" headers
     *
     * @param acceptHeader the value of an "Accept" header, potentially a comma-separated list
     * @return a map of content types and quality values
     */
    private List<AcceptContentType> parseAcceptHeader(String acceptHeader) {
        List<AcceptContentType> contentTypes = new ArrayList<>();

        // Multiple content types may have been set, so split them into tokens
        String[] acceptHeaderTokens = acceptHeader.split(",");

        for (String token : acceptHeaderTokens) {

            // Some content types may be in the form "<content-type>;q=<priority>", so split them into their sub-parts
            // For example: "Accept: application/json;q=0.8, application/yaml;q=0.9"
            String[] tokenParts = token.trim().split(";");
            String type = tokenParts[0].trim();

            // If no quality value was given, it defaults to 1 (see https://developer.mozilla.org/en-US/docs/Glossary/Quality_values)
            double quality = 1;
            if (tokenParts.length == 2) {
                try {
                    quality = Double.parseDouble(tokenParts[1].replace("q=", "").trim());
                } catch (NumberFormatException ex) {
                    logger.warn("Could not parse quality value for content type '" + tokenParts[0] + "', setting quality to 0");
                    quality = 0;
                }
            }
            contentTypes.add(new AcceptContentType(type, quality));
        }
        return contentTypes;
    }

    @Override
    public SupportedQueryParameterNames getSupportedQueryParameterNames() {
        return SupportedQueryParameterNames.NO_QUERY_PARAMETERS_SUPPORTED;
    }
}
