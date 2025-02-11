/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.validators;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;
import static dev.galasa.framework.api.common.resources.ResourceAction.*;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.beans.GalasaProperty;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.GalasaResourceValidator;
import dev.galasa.framework.api.common.resources.ResourceAction;
import dev.galasa.framework.api.common.resources.ResourceNameValidator;

public class GalasaPropertyValidator extends GalasaResourceValidator<JsonObject> {

    private ResourceNameValidator nameValidator = new ResourceNameValidator();

    public GalasaPropertyValidator(ResourceAction action) {
        super(action);
    }

    @Override
    public void validate(JsonObject propertyJson) throws InternalServletException {
        checkResourceHasRequiredFields(propertyJson, GalasaProperty.DEFAULTAPIVERSION);

        validatePropertyMetadata(propertyJson);

        // Delete operations shouldn't require a 'data' section, just the metadata to identify
        // the property to delete
        if (action != DELETE) {
            validatePropertyData(propertyJson);
        }
    }

    private void validatePropertyMetadata(JsonObject propertyJson) {
        //Check metadata is not null and contains name and namespace fields in the correct format
        JsonObject metadata = propertyJson.get("metadata").getAsJsonObject();
        if (metadata.has("name") && metadata.has("namespace")) {
            JsonElement name = metadata.get("name");
            JsonElement namespace = metadata.get("namespace"); 

            // Use the ResourceNameValidator to check that the name is correctly formatted and not null
            try {
                nameValidator.assertPropertyNameCharPatternIsValid(name.getAsString());
            } catch (InternalServletException e) {
                // All ResourceNameValidator error should be added to the list of reasons why the property action has failed
                validationErrors.add(e.getMessage());
            }

            // Use the ResourceNameValidator to check that the namespace is correctly formatted and not null
            try {
                nameValidator.assertNamespaceCharPatternIsValid(namespace.getAsString());
            } catch (InternalServletException e) {
                validationErrors.add(e.getMessage());   
            }
        } else {
            ServletError error = new ServletError(GAL5415_INVALID_GALASAPROPERTY_EMPTY_METADATA);
            validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
        }
    }

    private void validatePropertyData(JsonObject propertyJson) {
        //Check that data is not null and contains the value field
        JsonObject data = propertyJson.get("data").getAsJsonObject();
        if (data.size() > 0 && data.has("value")) {
            String value = data.get("value").getAsString();
            if (value == null ) {
                ServletError error = new ServletError(GAL5416_INVALID_GALASAPROPERTY_NULL_VALUE);
                validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
            }
        } else {
            ServletError error = new ServletError(GAL5417_INVALID_GALASAPROPERTY_DATA_FIELD_MISSING);
            validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
        }
    }
}
