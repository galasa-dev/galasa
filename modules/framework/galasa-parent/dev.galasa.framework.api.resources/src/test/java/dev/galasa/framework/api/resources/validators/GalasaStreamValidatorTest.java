/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.validators;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.resources.GalasaResourceType;
import dev.galasa.framework.api.common.resources.ResourceAction;

public class GalasaStreamValidatorTest {

    private void addObrJson(JsonArray obrJsonArr, String groupId, String artifactId, String version) {
        JsonObject obrJson = new JsonObject();
        obrJson.addProperty("group-id", groupId);
        obrJson.addProperty("artifact-id", artifactId);
        obrJson.addProperty("version", version);

        obrJsonArr.add(obrJson);
    }

    @Test
    public void testApplyValidStreamHasNoValidationErrors() throws Exception {
        // Given...
        ResourceAction action = ResourceAction.APPLY;
        GalasaStreamValidator validator = new GalasaStreamValidator(action);

        JsonObject streamJson = new JsonObject();
        streamJson.addProperty("apiVersion", GalasaStreamValidator.DEFAULT_API_VERSION);
        streamJson.addProperty("kind", GalasaResourceType.GALASA_STREAM.toString());

        JsonObject streamMetadata = new JsonObject();
        streamMetadata.addProperty("name", "myStream");
        streamMetadata.addProperty("description", "this is a stream description");

        JsonObject streamData = new JsonObject();

        JsonObject streamMavenRepo = new JsonObject();
        streamMavenRepo.addProperty("url", "https://my-maven-repo");

        JsonObject streamTestCatalog = new JsonObject();
        streamTestCatalog.addProperty("url", "https://my-testcatalog/testcatalog.json");

        JsonArray obrJsonArr = new JsonArray();
        addObrJson(obrJsonArr, "mygroup", "mygroup.myartifact", "my-version");

        streamData.add("repository", streamMavenRepo);
        streamData.add("testCatalog", streamTestCatalog);
        streamData.add("obrs", obrJsonArr);

        streamJson.add("metadata", streamMetadata);
        streamJson.add("data", streamData);

        // When...
        validator.validate(streamJson);

        // Then...
        assertThat(validator.getValidationErrors()).isEmpty();
    }

    @Test
    public void testApplyStreamWithInvalidMavenRepoAndTestCatalogUrlsHasValidationErrors() throws Exception {
        // Given...
        ResourceAction action = ResourceAction.APPLY;
        GalasaStreamValidator validator = new GalasaStreamValidator(action);

        JsonObject streamJson = new JsonObject();
        streamJson.addProperty("apiVersion", GalasaStreamValidator.DEFAULT_API_VERSION);
        streamJson.addProperty("kind", GalasaResourceType.GALASA_STREAM.toString());

        JsonObject streamMetadata = new JsonObject();
        streamMetadata.addProperty("name", "myStream");
        streamMetadata.addProperty("description", "this is a stream description");

        JsonObject streamData = new JsonObject();

        JsonObject streamMavenRepo = new JsonObject();
        streamMavenRepo.addProperty("url", "not-a-url!");

        JsonObject streamTestCatalog = new JsonObject();
        streamTestCatalog.addProperty("url", "a bad testcatalog URL!");

        JsonArray obrJsonArr = new JsonArray();
        addObrJson(obrJsonArr, "mygroup", "mygroup.myartifact", "my-version");

        streamData.add("repository", streamMavenRepo);
        streamData.add("testCatalog", streamTestCatalog);
        streamData.add("obrs", obrJsonArr);

        streamJson.add("metadata", streamMetadata);
        streamJson.add("data", streamData);

        // When...
        validator.validate(streamJson);

        // Then...
        List<String> validationErrors = validator.getValidationErrors();
        assertThat(validationErrors).hasSize(2);
        assertThat(validationErrors.get(0)).contains("The URL provided for the 'repository' field is not a valid URL");
        assertThat(validationErrors.get(1)).contains("The URL provided for the 'testCatalog' field is not a valid URL");
    }

    @Test
    public void testApplyStreamWithInvalidObrHasValidationErrors() throws Exception {
        // Given...
        ResourceAction action = ResourceAction.APPLY;
        GalasaStreamValidator validator = new GalasaStreamValidator(action);

        JsonObject streamJson = new JsonObject();
        streamJson.addProperty("apiVersion", GalasaStreamValidator.DEFAULT_API_VERSION);
        streamJson.addProperty("kind", GalasaResourceType.GALASA_STREAM.toString());

        JsonObject streamMetadata = new JsonObject();
        streamMetadata.addProperty("name", "myStream");
        streamMetadata.addProperty("description", "this is a stream description");

        JsonObject streamData = new JsonObject();

        JsonObject streamMavenRepo = new JsonObject();
        streamMavenRepo.addProperty("url", "https://my-maven-repo");

        JsonObject streamTestCatalog = new JsonObject();
        streamTestCatalog.addProperty("url", "https://my-testcatalog/testcatalog.json");

        JsonArray obrJsonArr = new JsonArray();

        // Create an OBR with no group-id and version
        JsonObject obrJson = new JsonObject();
        obrJson.addProperty("artifact-id", "myartifactid");

        obrJsonArr.add(obrJson);

        streamData.add("repository", streamMavenRepo);
        streamData.add("testCatalog", streamTestCatalog);
        streamData.add("obrs", obrJsonArr);

        streamJson.add("metadata", streamMetadata);
        streamJson.add("data", streamData);

        // When...
        validator.validate(streamJson);

        // Then...
        List<String> validationErrors = validator.getValidationErrors();
        assertThat(validationErrors).hasSize(1);
        assertThat(validationErrors.get(0)).contains("Invalid GalasaStream provided", "group-id", "version");
    }

    @Test
    public void testApplyStreamWithNoObrHasValidationErrors() throws Exception {
        // Given...
        ResourceAction action = ResourceAction.APPLY;
        GalasaStreamValidator validator = new GalasaStreamValidator(action);

        JsonObject streamJson = new JsonObject();
        streamJson.addProperty("apiVersion", GalasaStreamValidator.DEFAULT_API_VERSION);
        streamJson.addProperty("kind", GalasaResourceType.GALASA_STREAM.toString());

        JsonObject streamMetadata = new JsonObject();
        streamMetadata.addProperty("name", "myStream");
        streamMetadata.addProperty("description", "this is a stream description");

        JsonObject streamData = new JsonObject();

        JsonObject streamMavenRepo = new JsonObject();
        streamMavenRepo.addProperty("url", "https://my-maven-repo");

        JsonObject streamTestCatalog = new JsonObject();
        streamTestCatalog.addProperty("url", "https://my-testcatalog/testcatalog.json");

        streamData.add("repository", streamMavenRepo);
        streamData.add("testCatalog", streamTestCatalog);

        streamJson.add("metadata", streamMetadata);
        streamJson.add("data", streamData);

        // When...
        validator.validate(streamJson);

        // Then...
        List<String> validationErrors = validator.getValidationErrors();
        assertThat(validationErrors).hasSize(1);
        assertThat(validationErrors.get(0)).contains("Invalid GalasaStream provided", "data", "obrs");
    }

    @Test
    public void testApplyStreamWithEmptyObrsListHasValidationErrors() throws Exception {
        // Given...
        ResourceAction action = ResourceAction.APPLY;
        GalasaStreamValidator validator = new GalasaStreamValidator(action);

        JsonObject streamJson = new JsonObject();
        streamJson.addProperty("apiVersion", GalasaStreamValidator.DEFAULT_API_VERSION);
        streamJson.addProperty("kind", GalasaResourceType.GALASA_STREAM.toString());

        JsonObject streamMetadata = new JsonObject();
        streamMetadata.addProperty("name", "myStream");
        streamMetadata.addProperty("description", "this is a stream description");

        JsonObject streamData = new JsonObject();

        JsonObject streamMavenRepo = new JsonObject();
        streamMavenRepo.addProperty("url", "https://my-maven-repo");

        JsonObject streamTestCatalog = new JsonObject();
        streamTestCatalog.addProperty("url", "https://my-testcatalog/testcatalog.json");

        JsonArray obrJsonArr = new JsonArray();

        streamData.add("obrs", obrJsonArr);
        streamData.add("repository", streamMavenRepo);
        streamData.add("testCatalog", streamTestCatalog);

        streamJson.add("metadata", streamMetadata);
        streamJson.add("data", streamData);

        // When...
        validator.validate(streamJson);

        // Then...
        List<String> validationErrors = validator.getValidationErrors();
        assertThat(validationErrors).hasSize(1);
        assertThat(validationErrors.get(0)).contains("Invalid GalasaStream provided", "no OBRs were provided");
    }

    @Test
    public void testApplyStreamWithNoMavenAndTestCatalogURLsHasValidationErrors() throws Exception {
        // Given...
        ResourceAction action = ResourceAction.APPLY;
        GalasaStreamValidator validator = new GalasaStreamValidator(action);

        JsonObject streamJson = new JsonObject();
        streamJson.addProperty("apiVersion", GalasaStreamValidator.DEFAULT_API_VERSION);
        streamJson.addProperty("kind", GalasaResourceType.GALASA_STREAM.toString());

        JsonObject streamMetadata = new JsonObject();
        streamMetadata.addProperty("name", "myStream");
        streamMetadata.addProperty("description", "this is a stream description");

        JsonObject streamData = new JsonObject();

        JsonObject streamMavenRepo = new JsonObject();

        JsonObject streamTestCatalog = new JsonObject();

        JsonArray obrJsonArr = new JsonArray();
        addObrJson(obrJsonArr, "mygroup", "mygroup.myartifact", "my-version");

        streamData.add("repository", streamMavenRepo);
        streamData.add("testCatalog", streamTestCatalog);
        streamData.add("obrs", obrJsonArr);

        streamJson.add("metadata", streamMetadata);
        streamJson.add("data", streamData);

        // When...
        validator.validate(streamJson);

        // Then...
        List<String> validationErrors = validator.getValidationErrors();
        assertThat(validationErrors).hasSize(2);
        assertThat(validationErrors.get(0)).contains("Invalid GalasaStream provided", "repository", "url");
        assertThat(validationErrors.get(1)).contains("Invalid GalasaStream provided", "testCatalog", "url");
    }

    @Test
    public void testApplyStreamWithNoMavenAndTestCatalogHasValidationErrors() throws Exception {
        // Given...
        ResourceAction action = ResourceAction.APPLY;
        GalasaStreamValidator validator = new GalasaStreamValidator(action);

        JsonObject streamJson = new JsonObject();
        streamJson.addProperty("apiVersion", GalasaStreamValidator.DEFAULT_API_VERSION);
        streamJson.addProperty("kind", GalasaResourceType.GALASA_STREAM.toString());

        JsonObject streamMetadata = new JsonObject();
        streamMetadata.addProperty("name", "myStream");
        streamMetadata.addProperty("description", "this is a stream description");

        JsonObject streamData = new JsonObject();

        JsonArray obrJsonArr = new JsonArray();
        addObrJson(obrJsonArr, "mygroup", "mygroup.myartifact", "my-version");

        streamData.add("obrs", obrJsonArr);

        streamJson.add("metadata", streamMetadata);
        streamJson.add("data", streamData);

        // When...
        validator.validate(streamJson);

        // Then...
        List<String> validationErrors = validator.getValidationErrors();
        assertThat(validationErrors).hasSize(1);
        assertThat(validationErrors.get(0)).contains("Invalid GalasaStream provided", "repository", "testCatalog");
    }

    @Test
    public void testApplyStreamWithNoApiVersionThrowsError() throws Exception {
        // Given...
        ResourceAction action = ResourceAction.APPLY;
        GalasaStreamValidator validator = new GalasaStreamValidator(action);

        JsonObject streamJson = new JsonObject();
        streamJson.addProperty("kind", GalasaResourceType.GALASA_STREAM.toString());

        JsonObject streamMetadata = new JsonObject();
        streamMetadata.addProperty("name", "myStream");
        streamMetadata.addProperty("description", "this is a stream description");

        JsonObject streamData = new JsonObject();

        JsonObject streamMavenRepo = new JsonObject();
        streamMavenRepo.addProperty("url", "https://my-maven-repo");

        JsonObject streamTestCatalog = new JsonObject();
        streamTestCatalog.addProperty("url", "https://my-testcatalog/testcatalog.json");

        JsonArray obrJsonArr = new JsonArray();
        addObrJson(obrJsonArr, "mygroup", "mygroup.myartifact", "my-version");

        streamData.add("repository", streamMavenRepo);
        streamData.add("testCatalog", streamTestCatalog);
        streamData.add("obrs", obrJsonArr);

        streamJson.add("metadata", streamMetadata);
        streamJson.add("data", streamData);

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            validator.validate(streamJson);
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Invalid request body provided", "apiVersion");
    }

    @Test
    public void testApplyStreamWithNoMetadataThrowsError() throws Exception {
        // Given...
        ResourceAction action = ResourceAction.APPLY;
        GalasaStreamValidator validator = new GalasaStreamValidator(action);

        JsonObject streamJson = new JsonObject();
        streamJson.addProperty("apiVersion", GalasaStreamValidator.DEFAULT_API_VERSION);
        streamJson.addProperty("kind", GalasaResourceType.GALASA_STREAM.toString());

        JsonObject streamData = new JsonObject();

        JsonObject streamMavenRepo = new JsonObject();
        streamMavenRepo.addProperty("url", "https://my-maven-repo");

        JsonObject streamTestCatalog = new JsonObject();
        streamTestCatalog.addProperty("url", "https://my-testcatalog/testcatalog.json");

        JsonArray obrJsonArr = new JsonArray();
        addObrJson(obrJsonArr, "mygroup", "mygroup.myartifact", "my-version");

        streamData.add("repository", streamMavenRepo);
        streamData.add("testCatalog", streamTestCatalog);
        streamData.add("obrs", obrJsonArr);

        streamJson.add("data", streamData);

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            validator.validate(streamJson);
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Invalid request body provided", "metadata");
    }

    @Test
    public void testApplyStreamWithNoDataThrowsError() throws Exception {
        // Given...
        ResourceAction action = ResourceAction.APPLY;
        GalasaStreamValidator validator = new GalasaStreamValidator(action);

        JsonObject streamJson = new JsonObject();
        streamJson.addProperty("apiVersion", GalasaStreamValidator.DEFAULT_API_VERSION);
        streamJson.addProperty("kind", GalasaResourceType.GALASA_STREAM.toString());

        JsonObject streamMetadata = new JsonObject();
        streamMetadata.addProperty("name", "myStream");
        streamMetadata.addProperty("description", "this is a stream description");

        JsonObject streamMavenRepo = new JsonObject();
        streamMavenRepo.addProperty("url", "https://my-maven-repo");

        JsonObject streamTestCatalog = new JsonObject();
        streamTestCatalog.addProperty("url", "https://my-testcatalog/testcatalog.json");

        JsonArray obrJsonArr = new JsonArray();
        addObrJson(obrJsonArr, "mygroup", "mygroup.myartifact", "my-version");

        streamJson.add("metadata", streamMetadata);

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            validator.validate(streamJson);
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Invalid request body provided", "data");
    }


    @Test
    public void testDeleteValidStreamHasNoValidationErrors() throws Exception {
        // Given...
        ResourceAction action = ResourceAction.DELETE;
        GalasaStreamValidator validator = new GalasaStreamValidator(action);

        JsonObject streamJson = new JsonObject();
        streamJson.addProperty("apiVersion", GalasaStreamValidator.DEFAULT_API_VERSION);
        streamJson.addProperty("kind", GalasaResourceType.GALASA_STREAM.toString());

        JsonObject streamMetadata = new JsonObject();
        streamMetadata.addProperty("name", "myStream");

        streamJson.add("metadata", streamMetadata);

        // When...
        validator.validate(streamJson);

        // Then...
        assertThat(validator.getValidationErrors()).isEmpty();
    }
    
}
