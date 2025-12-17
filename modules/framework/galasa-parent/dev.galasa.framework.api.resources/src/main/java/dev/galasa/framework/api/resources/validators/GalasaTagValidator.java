/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.validators;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.GalasaResourceValidator;
import dev.galasa.framework.api.common.resources.ResourceAction;
import dev.galasa.framework.api.common.resources.TagValidator;

public class GalasaTagValidator extends GalasaResourceValidator<JsonObject> {

    private TagValidator tagValidator = new TagValidator();

    public GalasaTagValidator(ResourceAction action) {
        super(action);
    }

    @Override
    public void validate(JsonObject tagJson) throws InternalServletException {
        checkResourceHasRequiredFields(tagJson, DEFAULT_API_VERSION);
        validateTagMetadata(tagJson);

        // Delete operations shouldn't require a 'data' section, just the metadata to identify
        // the tag to delete
        if (action != ResourceAction.DELETE) {
            validateTagData(tagJson);
        }
    }

    private void validateTagMetadata(JsonObject tagJson) {

        JsonObject metadata = tagJson.getAsJsonObject("metadata");

        if (!metadata.has("name")) {
            ServletError error = new ServletError(GAL5447_MISSING_REQUIRED_TAG_FIELD, "name");
            validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
        } else {
            String tagName = metadata.get("name").getAsString();
            try {
                tagValidator.validateTagName(tagName);
            } catch (InternalServletException e) {
                validationErrors.add(e.getMessage());
            }
        }

        if (metadata.has("description")) {
            String description = metadata.get("description").getAsString();
            try {
                tagValidator.validateDescription(description);
            } catch (InternalServletException e) {
                validationErrors.add(e.getMessage());
            }
        }

    }

    private void validateTagData(JsonObject tagJson) {
        JsonObject data = tagJson.getAsJsonObject("data");

        if (data.has("priority")) {
            try {
                data.get("priority").getAsInt();
            } catch (NumberFormatException | ClassCastException e) {
                ServletError error = new ServletError(GAL5448_INVALID_TAG_PRIORITY_PROVIDED);
                validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
            }
        }
    }
}
