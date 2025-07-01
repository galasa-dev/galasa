/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.processors;

import static dev.galasa.framework.api.common.resources.ResourceAction.*;
import static dev.galasa.framework.spi.rbac.BuiltInAction.GENERAL_API_ACCESS;
import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.RBACValidator;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.resources.ResourcesServletTest;
import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.mocks.MockStream;
import dev.galasa.framework.mocks.MockStreamsService;
import dev.galasa.framework.spi.rbac.Action;
import dev.galasa.framework.spi.streams.IStream;

public class GalasaStreamProcessorTest extends ResourcesServletTest {

    private void addObrJson(JsonArray obrsArray, String groupId, String artifactId, String version) {
        JsonObject obrJson = new JsonObject();
        obrJson.addProperty("group-id", groupId);
        obrJson.addProperty("artifact-id", artifactId);
        obrJson.addProperty("version", version);

        obrsArray.add(obrJson);
    }

    private JsonObject generateStreamJson(
        String streamName,
        String description,
        String streamUrl,
        String mavenUrl,
        String catalogUrl,
        JsonArray obrs
    ) {
        JsonObject streamJson = new JsonObject();
        streamJson.addProperty("apiVersion", "galasa-dev/v1alpha1");
        streamJson.addProperty("kind", "GalasaStream");

        JsonObject streamMetadata = new JsonObject();
        if (streamName != null) {
            streamMetadata.addProperty("name", streamName);
        }

        if (description != null) {
            streamMetadata.addProperty("description", description);
        }

        if (streamUrl != null) {
            streamMetadata.addProperty("url", streamUrl);
        }

        JsonObject streamData = new JsonObject();
        streamData.addProperty("isEnabled", true);

        if (obrs != null) {
            streamData.add("obrs", obrs);
        }

        if (catalogUrl != null) {
            JsonObject streamCatalog = new JsonObject();
            streamCatalog.addProperty("url", catalogUrl);
            streamData.add("testCatalog", streamCatalog);
        }

        if (mavenUrl != null) {
            JsonObject streamMavenRepo = new JsonObject();
            streamMavenRepo.addProperty("url", mavenUrl);
            streamData.add("repository", streamMavenRepo);
        }

        streamJson.add("metadata", streamMetadata);
        streamJson.add("data", streamData);

        // Expecting a JSON structure in the form:
        // {
        //     "apiVersion": "galasa-dev/v1alpha1",
        //     "kind": "GalasaStream",
        //     "metadata": {
        //         "name": "mystream",
        //         "description": "This is a test stream",
        //         "url": "http://localhost:8080/streams/mystream"
        //     },
        //     "data": {
        //         "isEnabled": true,
        //         "testCatalog": {
        //              "url": "http://points-to-my-test-catalog.example.org"
        //         },
        //         "repository" : {
        //              "url": "http://points-to-my-maven-repo.example.org"
        //         },
        //         "obrs": [
        //             {
        //                 "group-id": "mygroup",
        //                 "artifact-id": "myartifact",
        //                 "version": "myversion"
        //             }
        //         ]
        //     }
        // }
        return streamJson;
    }

    @Test
    public void testValidateDeletePermissionsWithMissingPropertiesDeleteReturnsForbidden() throws Exception {
        // Given...
        List<Action> permittedActions = List.of(GENERAL_API_ACCESS.getAction());
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME, permittedActions);

        MockStreamsService streamService = new MockStreamsService(new ArrayList<>());
        MockFramework mockFramework = new MockFramework();
        mockFramework.setRBACService(mockRbacService);

        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaStreamProcessor streamProcessor = new GalasaStreamProcessor(streamService, rbacValidator);

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            streamProcessor.validateActionPermissions(DELETE, JWT_USERNAME);
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        checkErrorStructure(thrown.getMessage(), 5125, "GAL5125E", "CPS_PROPERTIES_DELETE");
    }

    @Test
    public void testDeleteStreamWithMissingNamePropertyReturnsBadRequest() throws Exception {
        // Given...
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);

        MockStreamsService streamService = new MockStreamsService(new ArrayList<>());
        MockFramework mockFramework = new MockFramework();
        mockFramework.setRBACService(mockRbacService);

        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaStreamProcessor streamProcessor = new GalasaStreamProcessor(streamService, rbacValidator);

        String description = "This is a test stream";
        String streamUrl = "http://localhost:8080/streams/mystream";
        String requestUsername = "myuser";

        JsonObject streamJson = generateStreamJson(null, description, streamUrl, null, null, null);
        
        // When...
        List<String> errors = streamProcessor.processResource(streamJson, DELETE, requestUsername);
        
        // Then...
        assertThat(errors).isNotEmpty();

        String errorMessage = errors.get(0);
        errorMessage.contains("GAL5427E");
        errorMessage.contains("E: Error occurred because the Galasa Stream is invalid. The 'metadata' field cannot be empty. The field 'name' is mandatory for the type GalasaStream.");
    }

    @Test
    public void testDeleteStreamWithInvalidNameEndingWithPeriodReturnsError() throws Exception {
        // Given...
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);

        MockStreamsService streamService = new MockStreamsService(new ArrayList<>());
        MockFramework mockFramework = new MockFramework();
        mockFramework.setRBACService(mockRbacService);

        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaStreamProcessor streamProcessor = new GalasaStreamProcessor(streamService, rbacValidator);

        String streamName = "mystream.";
        String description = "This is a test stream";
        String streamUrl = "http://localhost:8080/streams/mystream";
        String requestUsername = "myuser";

        JsonObject streamJson = generateStreamJson(streamName, description, streamUrl, null, null, null);
        
        // When...
        List<String> errors = streamProcessor.processResource(streamJson, DELETE, requestUsername);
        
        // Then...
        assertThat(errors).isNotEmpty();
        String errorMessage = errors.get(0);

        errorMessage.contains("GAL5418");
        errorMessage.contains("E: Invalid 'name' provided. A valid stream name should always start with 'a'-'z' or 'A'-'Z' and end with 'a'-'z', 'A'-'Z' or 0-9.");
    }

    @Test
    public void testDeleteStreamDeletesStreamsOk() throws Exception {
        // Given...
        String streamName = "mystream";
        String description = "This is a test stream";
        String streamUrl = "http://localhost:8080/streams/mystream";
        String requestUsername = "myuser";

        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);

        MockStream mockStream = new MockStream();
        mockStream.setName(streamName);
        mockStream.setDescription(description);

        List<IStream> streams = new ArrayList<>();
        streams.add(mockStream);

        MockStreamsService streamService = new MockStreamsService(streams);
        MockFramework mockFramework = new MockFramework();
        mockFramework.setRBACService(mockRbacService);

        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaStreamProcessor streamProcessor = new GalasaStreamProcessor(streamService, rbacValidator);

        JsonObject streamJson = generateStreamJson(streamName, description, streamUrl, null, null, null);

        // Check that we have a stream before processing
        assertThat(streams).hasSize(1);
        
        // When...
        List<String> errors = streamProcessor.processResource(streamJson, DELETE, requestUsername);

        // Then...
        // The stream should have been deleted
        assertThat(errors).isEmpty();
        assertThat(streams).hasSize(0);
    }

    @Test
    public void testCanCreateStream() throws Exception {
        // Given...
        String streamName = "mystream";
        String description = "This is a test stream";
        String streamUrl = "http://localhost:8080/streams/mystream";
        String mavenUrl = "http://my-maven-repo/tests";
        String catalogUrl = "http://my-maven-repo/testcatalog/testcatalog.json";
        String requestUsername = "myuser";
        String groupId = "myGroup";
        String artifactId = "myArtifact";
        String version = "myVersion";

        JsonArray obrsArray = new JsonArray();
        addObrJson(obrsArray, groupId, artifactId, version);

        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);

        MockStreamsService streamService = new MockStreamsService(new ArrayList<>());
        MockFramework mockFramework = new MockFramework();
        mockFramework.setRBACService(mockRbacService);

        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaStreamProcessor streamProcessor = new GalasaStreamProcessor(streamService, rbacValidator);

        JsonObject streamJson = generateStreamJson(streamName, description, streamUrl, mavenUrl, catalogUrl, obrsArray);
        
        // When...
        List<String> errors = streamProcessor.processResource(streamJson, CREATE, requestUsername);

        // Then...
        assertThat(errors).isEmpty();

        List<IStream> streams = streamService.getStreams();
        assertThat(streams).hasSize(1);
        assertThat(streams.get(0).getName()).isEqualTo(streamName);
    }

    @Test
    public void testCanApplyToCreateAStream() throws Exception {
        // Given...
        String streamName = "mystream";
        String description = "This is a test stream";
        String streamUrl = "http://localhost:8080/streams/mystream";
        String mavenUrl = "http://my-maven-repo/tests";
        String catalogUrl = "http://my-maven-repo/testcatalog/testcatalog.json";
        String requestUsername = "myuser";
        String groupId = "myGroup";
        String artifactId = "myArtifact";
        String version = "myVersion";

        JsonArray obrsArray = new JsonArray();
        addObrJson(obrsArray, groupId, artifactId, version);

        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);

        List<IStream> streams = new ArrayList<>();
        MockStreamsService streamService = new MockStreamsService(streams);
        MockFramework mockFramework = new MockFramework();
        mockFramework.setRBACService(mockRbacService);

        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaStreamProcessor streamProcessor = new GalasaStreamProcessor(streamService, rbacValidator);

        JsonObject streamJson = generateStreamJson(streamName, description, streamUrl, mavenUrl, catalogUrl, obrsArray);
        
        // When...
        List<String> errors = streamProcessor.processResource(streamJson, APPLY, requestUsername);

        // Then...
        assertThat(errors).isEmpty();

        List<IStream> streamsGotBack = streamService.getStreams();
        assertThat(streamsGotBack).hasSize(1);
        assertThat(streamsGotBack.get(0).getName()).isEqualTo(streamName);
        assertThat(streamsGotBack.get(0).getDescription()).isEqualTo(description);
    }

    @Test
    public void testCanApplyAnExistingStream() throws Exception {
        // Given...
        String streamName = "mystream";
        String description = "This is a test stream";
        String streamUrl = "http://localhost:8080/streams/mystream";
        String mavenUrl = "http://my-maven-repo/tests";
        String catalogUrl = "http://my-maven-repo/testcatalog/testcatalog.json";
        String requestUsername = "myuser";
        String groupId = "myGroup";
        String artifactId = "myArtifact";
        String version = "myVersion";

        String newDescription = "This is an updated stream description!";

        JsonArray obrsArray = new JsonArray();
        addObrJson(obrsArray, groupId, artifactId, version);

        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);

        List<IStream> streams = new ArrayList<>();
        MockStream mockStream = new MockStream();
        mockStream.setName(streamName);
        mockStream.setDescription(description);
        mockStream.setMavenRepositoryUrl(mavenUrl);
        mockStream.setTestCatalogUrl(catalogUrl);

        streams.add(mockStream);

        MockStreamsService streamService = new MockStreamsService(streams);
        MockFramework mockFramework = new MockFramework();
        mockFramework.setRBACService(mockRbacService);

        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaStreamProcessor streamProcessor = new GalasaStreamProcessor(streamService, rbacValidator);

        JsonObject streamJson = generateStreamJson(streamName, newDescription, streamUrl, mavenUrl, catalogUrl, obrsArray);
        
        // When...
        List<String> errors = streamProcessor.processResource(streamJson, APPLY, requestUsername);

        // Then...
        assertThat(errors).isEmpty();

        List<IStream> streamsGotBack = streamService.getStreams();
        assertThat(streamsGotBack).hasSize(1);
        assertThat(streamsGotBack.get(0).getName()).isEqualTo(streamName);
        assertThat(streamsGotBack.get(0).getDescription()).isEqualTo(newDescription);
    }

    @Test
    public void testCanUpdateAnExistingStream() throws Exception {
        // Given...
        String streamName = "mystream";
        String description = "This is a test stream";
        String streamUrl = "http://localhost:8080/streams/mystream";
        String mavenUrl = "http://my-maven-repo/tests";
        String catalogUrl = "http://my-maven-repo/testcatalog/testcatalog.json";
        String requestUsername = "myuser";
        String groupId = "myGroup";
        String artifactId = "myArtifact";
        String version = "myVersion";

        String newDescription = "This is an updated stream description!";

        JsonArray obrsArray = new JsonArray();
        addObrJson(obrsArray, groupId, artifactId, version);

        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);

        List<IStream> streams = new ArrayList<>();
        MockStream mockStream = new MockStream();
        mockStream.setName(streamName);
        mockStream.setDescription(description);
        mockStream.setMavenRepositoryUrl(mavenUrl);
        mockStream.setTestCatalogUrl(catalogUrl);

        streams.add(mockStream);

        MockStreamsService streamService = new MockStreamsService(streams);
        MockFramework mockFramework = new MockFramework();
        mockFramework.setRBACService(mockRbacService);

        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaStreamProcessor streamProcessor = new GalasaStreamProcessor(streamService, rbacValidator);

        JsonObject streamJson = generateStreamJson(streamName, newDescription, streamUrl, mavenUrl, catalogUrl, obrsArray);
        
        // When...
        List<String> errors = streamProcessor.processResource(streamJson, UPDATE, requestUsername);

        // Then...
        assertThat(errors).isEmpty();

        List<IStream> streamsGotBack = streamService.getStreams();
        assertThat(streamsGotBack).hasSize(1);
        assertThat(streamsGotBack.get(0).getName()).isEqualTo(streamName);
        assertThat(streamsGotBack.get(0).getDescription()).isEqualTo(newDescription);
    }

    @Test
    public void testUpdateANonExistentStreamThrowsError() throws Exception {
        // Given...
        String streamName = "mystream";
        String description = "This is a test stream";
        String streamUrl = "http://localhost:8080/streams/mystream";
        String mavenUrl = "http://my-maven-repo/tests";
        String catalogUrl = "http://my-maven-repo/testcatalog/testcatalog.json";
        String requestUsername = "myuser";
        String groupId = "myGroup";
        String artifactId = "myArtifact";
        String version = "myVersion";

        JsonArray obrsArray = new JsonArray();
        addObrJson(obrsArray, groupId, artifactId, version);

        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);

        MockStreamsService streamService = new MockStreamsService(new ArrayList<>());
        MockFramework mockFramework = new MockFramework();
        mockFramework.setRBACService(mockRbacService);

        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaStreamProcessor streamProcessor = new GalasaStreamProcessor(streamService, rbacValidator);

        JsonObject streamJson = generateStreamJson(streamName, description, streamUrl, mavenUrl, catalogUrl, obrsArray);
        
        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            streamProcessor.processResource(streamJson, UPDATE, requestUsername);
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("A stream with the provided name does not exist");
    }

    @Test
    public void testCreateAnExistingStreamThrowsError() throws Exception {
        // Given...
        String streamName = "mystream";
        String description = "This is a test stream";
        String streamUrl = "http://localhost:8080/streams/mystream";
        String mavenUrl = "http://my-maven-repo/tests";
        String catalogUrl = "http://my-maven-repo/testcatalog/testcatalog.json";
        String requestUsername = "myuser";
        String groupId = "myGroup";
        String artifactId = "myArtifact";
        String version = "myVersion";

        JsonArray obrsArray = new JsonArray();
        addObrJson(obrsArray, groupId, artifactId, version);

        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);

        List<IStream> streams = new ArrayList<>();
        MockStream mockStream = new MockStream();
        mockStream.setName(streamName);
        mockStream.setDescription(description);
        mockStream.setMavenRepositoryUrl(mavenUrl);
        mockStream.setTestCatalogUrl(catalogUrl);

        streams.add(mockStream);

        MockStreamsService streamService = new MockStreamsService(streams);
        MockFramework mockFramework = new MockFramework();
        mockFramework.setRBACService(mockRbacService);

        RBACValidator rbacValidator = new RBACValidator(mockFramework.getRBACService());
        GalasaStreamProcessor streamProcessor = new GalasaStreamProcessor(streamService, rbacValidator);

        JsonObject streamJson = generateStreamJson(streamName, description, streamUrl, mavenUrl, catalogUrl, obrsArray);
        
        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            streamProcessor.processResource(streamJson, CREATE, requestUsername);
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("A stream with the provided name already exists");
    }
}
