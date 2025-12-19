/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.tags.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.GAL5446_ERROR_SETTING_TAG;

import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ProtectedRoute;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.tags.ITagsService;
import dev.galasa.framework.spi.tags.Tag;
import dev.galasa.framework.spi.tags.TagsException;

/**
 * An abstract parent class for tag servlet routes which contains methods and fields 
 * that are commonly used by different tag routes.
 */
public abstract class AbstractTagRoute extends ProtectedRoute {

    protected ITagsService tagsService;

    public AbstractTagRoute(ResponseBuilder responseBuilder, String path, ITagsService tagsService, RBACService rbacService) {
        super(responseBuilder, path, rbacService);
        this.tagsService = tagsService;
    }

    protected void setTagIntoCPS(Tag tagToSet) throws InternalServletException {
        logger.info("Setting tag in CPS");
        try {
            tagsService.setTag(tagToSet);
        } catch (TagsException e) {
            ServletError error = new ServletError(GAL5446_ERROR_SETTING_TAG);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        logger.info("Tag set in CPS OK");
    }
}
