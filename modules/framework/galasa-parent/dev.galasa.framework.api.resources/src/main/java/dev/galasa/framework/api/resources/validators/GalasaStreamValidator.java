/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.validators;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;
import static dev.galasa.framework.api.common.resources.ResourceAction.*;

import java.util.List;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.resources.GalasaResourceValidator;
import dev.galasa.framework.api.common.resources.ResourceAction;

public class GalasaStreamValidator extends GalasaResourceValidator<JsonObject> {

    private static final List<String> REQUIRED_STREAM_DATA_FIELDS = List.of(
        "repository",
        "obrs",
        "testCatalog"
    );

    private static final List<String> REQUIRED_STREAM_MAVEN_REPO_FIELDS = List.of("url");
    private static final List<String> REQUIRED_STREAM_TESTCATALOG_FIELDS = List.of("url");
    private static final List<String> REQUIRED_STREAM_OBR_FIELDS = List.of("group-id", "artifact-id", "version");

    public GalasaStreamValidator(ResourceAction action) {
        super(action);
    }

    @Override
    public void validate(JsonObject streamJson) throws InternalServletException {

        checkResourceHasRequiredFields(streamJson, DEFAULT_API_VERSION);
        validateStreamMetadata(streamJson);

        // Delete operations shouldn't require a 'data' section, just the metadata to identify
        // the stream to delete
        if (validationErrors.isEmpty() && action != DELETE) {
            validateStreamData(streamJson);
        }
    }

    private void validateStreamData(JsonObject streamJson) {
        validateStreamField("data", streamJson, REQUIRED_STREAM_DATA_FIELDS);

        if (validationErrors.isEmpty()) {
            JsonObject dataJson = streamJson.get("data").getAsJsonObject();
            validateStreamField("repository", dataJson, REQUIRED_STREAM_MAVEN_REPO_FIELDS);
            validateStreamField("testCatalog", dataJson, REQUIRED_STREAM_TESTCATALOG_FIELDS);
            validateObrArray(dataJson);
        }
    }

    private void validateStreamField(String fieldName, JsonObject streamJson, List<String> requiredDataFields) {
        JsonObject streamDataFieldJson = streamJson.get(fieldName).getAsJsonObject();
        List<String> missingFields = getMissingResourceFields(streamDataFieldJson, requiredDataFields);
        if (!missingFields.isEmpty()) {
            ServletError error = new ServletError(GAL5434_INVALID_GALASA_STREAM_MISSING_FIELDS, fieldName, String.join(", ", missingFields));
            validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
        }
    }

    private void validateObrArray(JsonObject streamData) {
        JsonArray streamArray = streamData.get("obrs").getAsJsonArray();
        for (JsonElement obrElement : streamArray) {
            if (obrElement.isJsonObject()) {
                JsonObject obrJsonObj = obrElement.getAsJsonObject();
                List<String> missingFields = getMissingResourceFields(obrJsonObj, REQUIRED_STREAM_OBR_FIELDS);

                if (!missingFields.isEmpty()) {
                    ServletError error = new ServletError(GAL5434_INVALID_GALASA_STREAM_MISSING_FIELDS, "obrs", String.join(", ", missingFields));
                    validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
                    break;
                }
            } else {
                ServletError error = new ServletError(GAL5435_INVALID_GALASA_STREAM_OBR_DEFINITION);
                validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
                break;
            }
        }
    }

    private void validateStreamMetadata(JsonObject streamJson) {

        JsonObject metadata = streamJson.get("metadata").getAsJsonObject();

        // Check for name as we will delete the stream by name
        if (metadata.has("name")) {

            JsonElement name = metadata.get("name");
            boolean isStreamNameValid = isAlphanumWithDashes(name.getAsString());

            if(!isStreamNameValid) {
                validationErrors.add(GAL5418_INVALID_STREAM_NAME.toString());
            }

        } else {
            ServletError error = new ServletError(GAL5427_MISSING_STREAM_NAME);
            validationErrors.add(new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST).getMessage());
        }

    }
}
