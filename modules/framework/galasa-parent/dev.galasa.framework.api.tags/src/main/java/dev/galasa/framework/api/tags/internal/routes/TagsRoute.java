/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.framework.api.tags.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.MimeType;
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

public class TagsRoute extends ProtectedRoute {

    // Regex to match endpoint /tags and /tags/
    private static final String PATH_PATTERN = "\\/?";

    private ITagsService tagsService;
    private String externalApiServerUrl;

    public TagsRoute(ResponseBuilder responseBuilder, String externalApiServerUrl, ITagsService tagsService, RBACService rbacService) {
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
        String tagsJson = null;

        try {
            logger.info("Getting tags from the tags service");
            List<Tag> tags = tagsService.getTags();
            logger.info("Found " + tags.size() + " tag(s).");

            tagsJson = transform.getTagsAsJsonString(tags, externalApiServerUrl);
        } catch (TagsException e) {
            ServletError error = new ServletError(GAL5438_ERROR_GETTING_TAGS);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        logger.info("handleGetRequest() exiting.");
        return getResponseBuilder().buildResponse(request, response, MimeType.APPLICATION_JSON.toString(), tagsJson, HttpServletResponse.SC_OK);
    }
}
