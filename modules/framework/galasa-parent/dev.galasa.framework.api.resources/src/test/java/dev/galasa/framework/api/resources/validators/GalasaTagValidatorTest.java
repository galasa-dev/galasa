/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.validators;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.Test;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.resources.GalasaResourceType;
import dev.galasa.framework.api.common.resources.ResourceAction;

public class GalasaTagValidatorTest {

    @Test
    public void testApplyValidTagHasNoValidationErrors() throws Exception {
        // Given...
        ResourceAction action = ResourceAction.APPLY;
        GalasaTagValidator validator = new GalasaTagValidator(action);

        JsonObject tagJson = new JsonObject();
        tagJson.addProperty("apiVersion", GalasaStreamValidator.DEFAULT_API_VERSION);
        tagJson.addProperty("kind", GalasaResourceType.GALASA_TAG.toString());

        JsonObject metadata = new JsonObject();
        metadata.addProperty("name", "my tag");
        metadata.addProperty("description", "this is a tag description");

        JsonObject data = new JsonObject();
        int priority = 100;
        data.addProperty("priority", priority);

        tagJson.add("metadata", metadata);
        tagJson.add("data", data);

        // When...
        validator.validate(tagJson);

        // Then...
        assertThat(validator.getValidationErrors()).isEmpty();
    }

    @Test
    public void testApplyTagWithNoDescriptionHasNoValidationErrors() throws Exception {
        // Given...
        ResourceAction action = ResourceAction.APPLY;
        GalasaTagValidator validator = new GalasaTagValidator(action);

        JsonObject tagJson = new JsonObject();
        tagJson.addProperty("apiVersion", GalasaStreamValidator.DEFAULT_API_VERSION);
        tagJson.addProperty("kind", GalasaResourceType.GALASA_TAG.toString());

        JsonObject metadata = new JsonObject();
        metadata.addProperty("name", "my tag");

        JsonObject data = new JsonObject();
        int priority = 100;
        data.addProperty("priority", priority);

        tagJson.add("metadata", metadata);
        tagJson.add("data", data);

        // When...
        validator.validate(tagJson);

        // Then...
        assertThat(validator.getValidationErrors()).isEmpty();
    }

    @Test
    public void testApplyTagWithNoPriorityHasNoValidationErrors() throws Exception {
        // Given...
        ResourceAction action = ResourceAction.APPLY;
        GalasaTagValidator validator = new GalasaTagValidator(action);

        JsonObject tagJson = new JsonObject();
        tagJson.addProperty("apiVersion", GalasaStreamValidator.DEFAULT_API_VERSION);
        tagJson.addProperty("kind", GalasaResourceType.GALASA_TAG.toString());

        JsonObject metadata = new JsonObject();
        metadata.addProperty("name", "my tag");
        metadata.addProperty("description", "this is a tag description");

        JsonObject data = new JsonObject();
        tagJson.add("metadata", metadata);
        tagJson.add("data", data);

        // When...
        validator.validate(tagJson);

        // Then...
        assertThat(validator.getValidationErrors()).isEmpty();
    }

    @Test
    public void testApplyTagWithNoNameHasValidationError() throws Exception {
        // Given...
        ResourceAction action = ResourceAction.APPLY;
        GalasaTagValidator validator = new GalasaTagValidator(action);

        JsonObject tagJson = new JsonObject();
        tagJson.addProperty("apiVersion", GalasaStreamValidator.DEFAULT_API_VERSION);
        tagJson.addProperty("kind", GalasaResourceType.GALASA_TAG.toString());

        JsonObject metadata = new JsonObject();
        metadata.addProperty("description", "this is a tag description");

        JsonObject data = new JsonObject();
        int priority = 100;
        data.addProperty("priority", priority);

        tagJson.add("metadata", metadata);
        tagJson.add("data", data);

        // When...
        validator.validate(tagJson);

        // Then...
        List<String> validationErrors = validator.getValidationErrors();
        assertThat(validationErrors).hasSize(1);
        assertThat(validationErrors.get(0)).contains("GAL5447", "The required field 'name' was missing from the request payload");
    }

    @Test
    public void testApplyTagWithInvalidNameHasValidationError() throws Exception {
        // Given...
        ResourceAction action = ResourceAction.APPLY;
        GalasaTagValidator validator = new GalasaTagValidator(action);

        JsonObject tagJson = new JsonObject();
        tagJson.addProperty("apiVersion", GalasaStreamValidator.DEFAULT_API_VERSION);
        tagJson.addProperty("kind", GalasaResourceType.GALASA_TAG.toString());

        JsonObject metadata = new JsonObject();
        metadata.addProperty("name", "   ");
        metadata.addProperty("description", "this is a tag description");

        JsonObject data = new JsonObject();
        int priority = 100;
        data.addProperty("priority", priority);

        tagJson.add("metadata", metadata);
        tagJson.add("data", data);

        // When...
        validator.validate(tagJson);

        // Then...
        List<String> validationErrors = validator.getValidationErrors();
        assertThat(validationErrors).hasSize(1);
        assertThat(validationErrors.get(0)).contains("GAL5443E", "Invalid tag name provided");
    }

    @Test
    public void testApplyTagWithInvalidDescriptionHasValidationError() throws Exception {
        // Given...
        ResourceAction action = ResourceAction.APPLY;
        GalasaTagValidator validator = new GalasaTagValidator(action);

        JsonObject tagJson = new JsonObject();
        tagJson.addProperty("apiVersion", GalasaStreamValidator.DEFAULT_API_VERSION);
        tagJson.addProperty("kind", GalasaResourceType.GALASA_TAG.toString());

        JsonObject metadata = new JsonObject();
        metadata.addProperty("name", "my tag");
        metadata.addProperty("description", "    ");

        JsonObject data = new JsonObject();
        int priority = 100;
        data.addProperty("priority", priority);

        tagJson.add("metadata", metadata);
        tagJson.add("data", data);

        // When...
        validator.validate(tagJson);

        // Then...
        List<String> validationErrors = validator.getValidationErrors();
        assertThat(validationErrors).hasSize(1);
        assertThat(validationErrors.get(0)).contains("GAL5444E", "Invalid tag description provided");
    }

    @Test
    public void testApplyTagWithInvalidPriorityHasValidationError() throws Exception {
        // Given...
        ResourceAction action = ResourceAction.APPLY;
        GalasaTagValidator validator = new GalasaTagValidator(action);

        JsonObject tagJson = new JsonObject();
        tagJson.addProperty("apiVersion", GalasaStreamValidator.DEFAULT_API_VERSION);
        tagJson.addProperty("kind", GalasaResourceType.GALASA_TAG.toString());

        JsonObject metadata = new JsonObject();
        metadata.addProperty("name", "my tag");
        metadata.addProperty("description", "a tag description");

        JsonObject data = new JsonObject();
        String priority = "not a valid priority!";
        data.addProperty("priority", priority);

        tagJson.add("metadata", metadata);
        tagJson.add("data", data);

        // When...
        validator.validate(tagJson);

        // Then...
        List<String> validationErrors = validator.getValidationErrors();
        assertThat(validationErrors).hasSize(1);
        assertThat(validationErrors.get(0)).contains("GAL5448E", "Invalid tag priority provided");
    }
}
