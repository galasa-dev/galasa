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
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.ResourceAction;
import dev.galasa.framework.spi.utils.GalasaGson;

public abstract class AbstractGalasaResourceProcessor {
    protected static final Set<ResourceAction> updateActions = Set.of(APPLY, UPDATE);
    protected static final GalasaGson gson = new GalasaGson();

    protected void checkResourceHasRequiredFields(
        JsonObject resourceJson,
        String expectedApiVersion,
        ResourceAction action
    ) throws InternalServletException {
        List<String> requiredFields = getRequiredResourceFields(action);
        List<String> missingFields = getMissingResourceFields(resourceJson, requiredFields);
        if (!missingFields.isEmpty()) {
            ServletError error = new ServletError(GAL5069_MISSING_REQUIRED_FIELDS, String.join(", ", missingFields));
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        String apiVersion = resourceJson.get("apiVersion").getAsString();
        if (!apiVersion.equals(expectedApiVersion)) {
            ServletError error = new ServletError(GAL5027_UNSUPPORTED_API_VERSION, expectedApiVersion);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    protected List<String> getMissingResourceFields(JsonObject resourceJson, List<String> requiredFields) {
        List<String> missingFields = new ArrayList<>();
        for (String field : requiredFields) {
            if (!resourceJson.has(field)) {
                missingFields.add(field);
            }
        }
        return missingFields;
    }

    private List<String> getRequiredResourceFields(ResourceAction action) {
        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("apiVersion");
        requiredFields.add("metadata");
        if (action != DELETE) {
            requiredFields.add("data");
        }
        return requiredFields;
    }
}
