/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.processors;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;
import static dev.galasa.framework.api.common.resources.ResourceAction.*;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.beans.GalasaProperty;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.CPSFacade;
import dev.galasa.framework.api.common.resources.CPSNamespace;
import dev.galasa.framework.api.common.resources.CPSProperty;
import dev.galasa.framework.api.common.resources.ResourceAction;
import dev.galasa.framework.api.common.resources.ResourceNameValidator;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;

public class GalasaPropertyProcessor extends AbstractGalasaResourceProcessor implements IGalasaResourceProcessor {

    private CPSFacade cps;
    static final ResourceNameValidator nameValidator = new ResourceNameValidator();

    public GalasaPropertyProcessor(CPSFacade cps) {
        this.cps = cps;
    }

    @Override
    public List<String> processResource(JsonObject resource, ResourceAction action) throws InternalServletException {
        List<String> errors = checkGalasaPropertyJsonStructure(resource, action);
        try {
            if (errors.isEmpty()) {
                GalasaProperty galasaProperty = gson.fromJson(resource, GalasaProperty.class);           
                CPSNamespace namespace = cps.getNamespace(galasaProperty.getNamespace());

                //getPropertyFromStore() will only return null if the property is in a hidden namespace
                CPSProperty property = namespace.getPropertyFromStore(galasaProperty.getName());

                if (action == DELETE) {
                    property.deletePropertyFromStore();
                } else {
                    /*
                    * The logic below is used to determine if the exclusive Not Or condition in property.setPropertyToStore 
                    * (i.e. "the property exists" must equal to "is this an update action") will action the request or error
                    *
                    * Logic Table to Determine actions
                    * If the action is equal to "update" (force update) the updateProperty is set to true (update property,
                    * will error if the property does not exist in CPS)
                    * If the action is either "update" or "apply" and the property exists in CPS the updateProperty is set to true (update property)
                    * If the action is equal to "apply" and the property does not exist in CPS the updateProperty is set to false (create property)
                    * If the action is equal to "create" (force create) the updateProperty is set to false (create property, will error if the property exists in CPS)
                    */
                    boolean updateProperty = false;
                    if ((updateActions.contains(action) && property.existsInStore()) || action == UPDATE) {
                        updateProperty = true;
                    }
                    property.setPropertyToStore(galasaProperty, updateProperty);
                }
            }
        } catch (ConfigurationPropertyStoreException e){
            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR, e.getMessage());
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
        return errors;
    }

    private List<String> checkGalasaPropertyJsonStructure(JsonObject propertyJson, ResourceAction action) throws InternalServletException {
        checkResourceHasRequiredFields(propertyJson, GalasaProperty.DEFAULTAPIVERSION, action);

        List<String> validationErrors = new ArrayList<String>();
        validatePropertyMetadata(propertyJson, validationErrors);

        // Delete operations shouldn't require a 'data' section, just the metadata to identify
        // the property to delete
        if (action != DELETE) {
            validatePropertyData(propertyJson, validationErrors);
        }
        return validationErrors;
    }

    private void validatePropertyMetadata(JsonObject propertyJson, List<String> validationErrors) {
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
            String message = "The 'metadata' field cannot be empty. The fields 'name' and 'namespace' are mandatory for the type GalasaProperty.";
            ServletError error = new ServletError(GAL5024_INVALID_GALASAPROPERTY, message);
            validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
        }
    }

    private void validatePropertyData(JsonObject propertyJson, List<String> validationErrors) {
        //Check that data is not null and contains the value field
        JsonObject data = propertyJson.get("data").getAsJsonObject();
        if (data.size() > 0 && data.has("value")) {
            String value = data.get("value").getAsString();
            if (value == null || value.isBlank()) {
                String message = "The 'value' field cannot be empty. The field 'value' is mandatory for the type GalasaProperty.";
                ServletError error = new ServletError(GAL5024_INVALID_GALASAPROPERTY, message);
                validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
            }
        } else {
            String message = "The 'data' field cannot be empty. The field 'value' is mandatory for the type GalasaProperty.";
            ServletError error = new ServletError(GAL5024_INVALID_GALASAPROPERTY, message);
            validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
        }
    }
}
