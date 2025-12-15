/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.tags.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.beans.generated.GalasaTag;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ProtectedRoute;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.tags.internal.common.TagsBeanTransform;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.tags.ITagsService;
import dev.galasa.framework.spi.tags.Tag;
import dev.galasa.framework.spi.tags.TagsException;

public class TagByNameRoute extends ProtectedRoute {

    // Regex to match endpoint /tags/{encoded-tag-name}
    private static final String PATH_PATTERN = "\\/([a-zA-Z0-9-_]+)\\/?";

    private ITagsService tagsService;
    private String externalApiServerUrl;

    public TagByNameRoute(ResponseBuilder responseBuilder, String externalApiServerUrl, ITagsService tagsService, RBACService rbacService) {
        super(responseBuilder, PATH_PATTERN, rbacService);
        this.tagsService = tagsService;
        this.externalApiServerUrl = externalApiServerUrl;
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
            HttpRequestContext requestContext, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {

        logger.info("handleGetRequest() entered.");
        HttpServletRequest request = requestContext.getRequest();
        TagsBeanTransform transform = new TagsBeanTransform();

        String tagName = getTagNameFromPath(pathInfo);

        Tag tag = getTagByName(tagName);
        if (tag == null) {
            ServletError error = new ServletError(GAL5441_ERROR_TAG_NOT_FOUND, tagName);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }

        GalasaTag tagBean = transform.createTagBean(tag, externalApiServerUrl);
        String tagJson = gson.toJson(tagBean);

        logger.info("handleGetRequest() exiting.");
        return getResponseBuilder().buildResponse(request, response, "application/json", tagJson,
                HttpServletResponse.SC_OK);
    }

    @Override
    public HttpServletResponse handleDeleteRequest(
        String pathInfo,
        HttpRequestContext requestContext,
        HttpServletResponse response
    ) throws FrameworkException {

        logger.info("handleDeleteRequest() entered");
        HttpServletRequest request = requestContext.getRequest();

        String tagName = getTagNameFromPath(pathInfo);

        Tag tagToDelete = getTagByName(tagName);
        if (tagToDelete == null) {
            ServletError error = new ServletError(GAL5441_ERROR_TAG_NOT_FOUND, tagName);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
        }

        try {
            tagsService.deleteTag(tagName);
        } catch (TagsException e) {
            ServletError error = new ServletError(GAL5442_ERROR_DELETING_TAG, tagName);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        logger.info("handleDeleteRequest() exiting");
        return getResponseBuilder().buildResponse(request, response, HttpServletResponse.SC_NO_CONTENT);
    }

    private Tag getTagByName(String tagName) throws InternalServletException {
        Tag tag = null;
        try {
            tag = tagsService.getTagByName(tagName);
        } catch (TagsException e) {
            ServletError error = new ServletError(GAL5439_ERROR_GETTING_TAG_BY_NAME, tagName);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return tag;
    }

    private String getTagNameFromPath(String urlPath) throws InternalServletException {
        Matcher matcher = getPathRegex().matcher(urlPath);

        if (!matcher.matches()) {
            ServletError error = new ServletError(GAL5440_INVALID_TAG_NAME_PROVIDED);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        String tagName = null;
        try {
            String encodedTagName = matcher.group(1);
            tagName = new String(Base64.getUrlDecoder().decode(encodedTagName), StandardCharsets.UTF_8);

        } catch (IllegalArgumentException e) {
            ServletError error = new ServletError(GAL5440_INVALID_TAG_NAME_PROVIDED);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
        return tagName;
    }
}
