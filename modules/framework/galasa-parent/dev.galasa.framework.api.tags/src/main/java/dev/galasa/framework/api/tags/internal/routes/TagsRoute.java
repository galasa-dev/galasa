/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.framework.api.tags.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.beans.generated.GalasaTag;
import dev.galasa.framework.api.beans.generated.TagCreateRequest;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.MimeType;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.SupportedQueryParameterNames;
import dev.galasa.framework.api.tags.internal.common.TagCreateRequestValidator;
import dev.galasa.framework.api.tags.internal.common.TagsBeanTransform;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.rbac.BuiltInAction;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.tags.ITagsService;
import dev.galasa.framework.spi.tags.Tag;
import dev.galasa.framework.spi.tags.TagsException;
import dev.galasa.framework.spi.utils.StringValidator;

public class TagsRoute extends AbstractTagRoute {

    // Query parameters
    public static final String QUERY_PARAMETER_NAME = "name";
    public static final SupportedQueryParameterNames SUPPORTED_QUERY_PARAMETER_NAMES = new SupportedQueryParameterNames(
        QUERY_PARAMETER_NAME
    );

    // Regex to match endpoint /tags and /tags/
    private static final String PATH_PATTERN = "\\/?";

    private String externalApiServerUrl;

    public TagsRoute(ResponseBuilder responseBuilder, String externalApiServerUrl, ITagsService tagsService, RBACService rbacService) {
        super(responseBuilder, PATH_PATTERN, tagsService, rbacService);
        this.externalApiServerUrl = externalApiServerUrl;
    }

    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
            HttpRequestContext requestContext, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {

        logger.info("handleGetRequest() entered.");
        HttpServletRequest request = requestContext.getRequest();
        TagsBeanTransform transform = new TagsBeanTransform();
        StringValidator validator = new StringValidator();

        String tagNameQuery = validator.sanitizeString(queryParams.getSingleString(QUERY_PARAMETER_NAME, null));

        List<Tag> tags = new ArrayList<>();
        try {
            if (tagNameQuery != null) {
                logger.info("Getting tag with the given name from the tags service");

                Tag tag = tagsService.getTagByName(tagNameQuery);
                if (tag != null) {
                    tags.add(tag);
                    logger.info("Found a tag with the given name");
                }
            } else {
                logger.info("Getting all tags from the tags service");
                tags = tagsService.getTags();
                logger.info("Found " + tags.size() + " tag(s).");
            }
        } catch (TagsException e) {
            ServletError error = new ServletError(GAL5438_ERROR_GETTING_TAGS);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        String tagsJson = transform.getTagsAsJsonString(tags, externalApiServerUrl);

        logger.info("handleGetRequest() exiting.");
        return getResponseBuilder().buildResponse(request, response, MimeType.APPLICATION_JSON.toString(), tagsJson, HttpServletResponse.SC_OK);
    }

    @Override
    public HttpServletResponse handlePostRequest(String pathInfo,
            HttpRequestContext requestContext, HttpServletResponse response)
            throws  IOException, FrameworkException {

        logger.info("handlePostRequest() entered. Validating request");
        HttpServletRequest request = requestContext.getRequest();
        validateActionPermitted(BuiltInAction.CPS_PROPERTIES_SET, requestContext.getUsername());

        TagCreateRequest payload = parseRequestBody(request, TagCreateRequest.class);
        TagCreateRequestValidator validator = new TagCreateRequestValidator();
        validator.validate(payload);
        logger.info("Request payload validated");

        // Check if a tag with the given name already exists, throwing an error if so
        String tagName = payload.getname();
        if (tagsService.getTagByName(tagName) != null) {
            ServletError error = new ServletError(GAL5445_ERROR_TAG_ALREADY_EXISTS);
            throw new InternalServletException(error, HttpServletResponse.SC_CONFLICT);
        }

        Tag tagToSet = buildTagFromRequestPayload(payload);
        setTagIntoCPS(tagToSet);

        TagsBeanTransform transform = new TagsBeanTransform();
        GalasaTag createdTag = transform.createTagBean(tagToSet, externalApiServerUrl);
        String tagJson = gson.toJson(createdTag);
        
        logger.info("handlePostRequest() exiting");
        return getResponseBuilder().buildResponse(request, response, MimeType.APPLICATION_JSON.toString(), tagJson, HttpServletResponse.SC_CREATED);
    }

    private Tag buildTagFromRequestPayload(TagCreateRequest requestPayload) {
        Tag tag = new Tag(requestPayload.getname());

        String description = requestPayload.getdescription();
        if (description != null) {
            tag.setDescription(description);
        }

        Integer priority = requestPayload.getpriority();
        if (priority != null) {
            tag.setPriority(priority);
        }
        return tag;
    }

    @Override
    public SupportedQueryParameterNames getSupportedQueryParameterNames() {
        return SUPPORTED_QUERY_PARAMETER_NAMES;
    }
}
