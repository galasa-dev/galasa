/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.processors;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;
import static dev.galasa.framework.api.common.resources.ResourceAction.*;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.beans.generated.GalasaTag;
import dev.galasa.framework.api.beans.generated.GalasaTagdata;
import dev.galasa.framework.api.beans.generated.GalasaTagmetadata;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.RBACValidator;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.ResourceAction;
import dev.galasa.framework.api.resources.validators.GalasaTagValidator;
import dev.galasa.framework.spi.rbac.BuiltInAction;
import dev.galasa.framework.spi.tags.ITagsService;
import dev.galasa.framework.spi.tags.Tag;
import dev.galasa.framework.spi.tags.TagsException;

public class GalasaTagProcessor extends AbstractGalasaResourceProcessor implements IGalasaResourceProcessor {

    private ITagsService tagsService;
    private final Log logger = LogFactory.getLog(getClass());

    public GalasaTagProcessor(ITagsService tagsService, RBACValidator rbacValidator) {
        super(rbacValidator);
        this.tagsService = tagsService;
    }

    @Override
    public List<String> processResource(JsonObject resourceJson, ResourceAction action, String username)
            throws InternalServletException {

        logger.info("Processing GalasaTag resource");
        List<String> errors = checkGalasaTagJsonStructure(resourceJson, action);

        if (errors.isEmpty()) {
            GalasaTag galasaTag = gson.fromJson(resourceJson, GalasaTag.class);
            String tagName = galasaTag.getmetadata().getname();

            if (action == DELETE) {
                deleteTag(tagName);
            } else {
                try {
                    Tag existingTag = tagsService.getTagByName(tagName);
                    if (action == CREATE && existingTag != null) {
                        ServletError error = new ServletError(GAL5445_ERROR_TAG_ALREADY_EXISTS);
                        throw new InternalServletException(error, HttpServletResponse.SC_CONFLICT);
                    } else if (action == UPDATE && existingTag == null) {
                        ServletError error = new ServletError(GAL5441_ERROR_TAG_NOT_FOUND);
                        throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
                    }

                    setTag(galasaTag);
                } catch (TagsException e) {
                    ServletError error = new ServletError(GAL5446_ERROR_SETTING_TAG);
                    throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }

            logger.info("Processed GalasaTag resource OK");
        }

        return errors;
    }

    private void setTag(GalasaTag galasaTag) throws InternalServletException {
        try {
            Tag tag = transformGalasaTagToTag(galasaTag);
            logger.info("Setting tag in CPS store");
            tagsService.setTag(tag);
            logger.info("Set tag in CPS store OK");
        } catch (TagsException e) {
            ServletError error = new ServletError(GAL5446_ERROR_SETTING_TAG);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void deleteTag(String tagName) throws InternalServletException {
        try {
            logger.info("Deleting tag from CPS store");
            tagsService.deleteTag(tagName);
            logger.info("Deleted tag from CPS store OK");
        } catch (TagsException e) {
            ServletError error = new ServletError(GAL5442_ERROR_DELETING_TAG);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private List<String> checkGalasaTagJsonStructure(JsonObject tagJson, ResourceAction action) throws InternalServletException {
        GalasaTagValidator validator = new GalasaTagValidator(action);
        return checkGalasaResourceJsonStructure(validator, tagJson);
    }

    @Override
    public void validateActionPermissions(ResourceAction action, String username) throws InternalServletException {
        BuiltInAction requestedAction = getResourceActionAsBuiltInAction(action, BuiltInAction.CPS_PROPERTIES_SET, BuiltInAction.CPS_PROPERTIES_DELETE);
        rbacValidator.validateActionPermitted(requestedAction, username);
    }

    private Tag transformGalasaTagToTag(GalasaTag galasaTag) {
        GalasaTagmetadata metadata = galasaTag.getmetadata();
        GalasaTagdata data = galasaTag.getdata();

        Tag tag = new Tag(metadata.getname());
        
        String description = metadata.getdescription();
        if (description != null) {
            tag.setDescription(description);
        }

        Integer priority = data.getpriority();
        if (priority != null) {
            tag.setPriority(priority);
        }
        return tag;
    }
}
