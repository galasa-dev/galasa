/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.processors;

import static dev.galasa.framework.api.common.resources.ResourceAction.*;
import static dev.galasa.framework.spi.rbac.BuiltInAction.GENERAL_API_ACCESS;
import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.RBACValidator;
import dev.galasa.framework.api.common.resources.GalasaResourceType;
import dev.galasa.framework.api.common.resources.GalasaResourceValidator;
import dev.galasa.framework.api.resources.ResourcesServletTest;
import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.mocks.MockTagsService;
import dev.galasa.framework.spi.rbac.Action;
import dev.galasa.framework.spi.tags.Tag;

public class GalasaTagProcessorTest extends ResourcesServletTest {

    private JsonObject generateTagJson(
        String tagName,
        String description,
        Integer priority
    ) {
        JsonObject tagJson = new JsonObject();
        tagJson.addProperty("apiVersion", GalasaResourceValidator.DEFAULT_API_VERSION);
        tagJson.addProperty("kind", GalasaResourceType.GALASA_TAG.toString());

        JsonObject metadata = new JsonObject();
        if (tagName != null) {
            metadata.addProperty("name", tagName);
        }

        if (description != null) {
            metadata.addProperty("description", description);
        }

        JsonObject data = new JsonObject();

        if (priority != null) {
            data.addProperty("priority", priority);
        }

        tagJson.add("metadata", metadata);
        tagJson.add("data", data);
        return tagJson;
    }

    @Test
    public void testValidateDeletePermissionsWithMissingPropertiesDeleteReturnsForbidden() throws Exception {
        // Given...
        List<Action> permittedActions = List.of(GENERAL_API_ACCESS.getAction());
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME, permittedActions);

        MockTagsService tagService = new MockTagsService();
        RBACValidator rbacValidator = new RBACValidator(mockRbacService);
        GalasaTagProcessor tagProcessor = new GalasaTagProcessor(tagService, rbacValidator);

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            tagProcessor.validateActionPermissions(DELETE, JWT_USERNAME);
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        checkErrorStructure(thrown.getMessage(), 5125, "GAL5125E", "CPS_PROPERTIES_DELETE");
    }

    @Test
    public void testDeleteTagWithMissingNameReturnsBadRequest() throws Exception {
        // Given...
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);

        MockTagsService tagService = new MockTagsService();

        RBACValidator rbacValidator = new RBACValidator(mockRbacService);
        GalasaTagProcessor tagProcessor = new GalasaTagProcessor(tagService, rbacValidator);

        String description = "This is a test tag";
        String requestUsername = "myuser";

        JsonObject tagJson = generateTagJson(null, description, null);

        // When...
        List<String> errors = tagProcessor.processResource(tagJson, DELETE, requestUsername);

        // Then...
        assertThat(errors).isNotEmpty();
        assertThat(errors.get(0)).contains("GAL5447E", "The required field 'name' was missing from the request payload");
    }

    @Test
    public void testDeleteTagWithInvalidNameReturnsError() throws Exception {
        // Given...
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);

        MockTagsService tagService = new MockTagsService();

        RBACValidator rbacValidator = new RBACValidator(mockRbacService);
        GalasaTagProcessor tagProcessor = new GalasaTagProcessor(tagService, rbacValidator);

        String tagName = "     ";
        String description = "This is a test tag";
        Integer priority = 100;
        String requestUsername = "myuser";

        JsonObject tagJson = generateTagJson(tagName, description, priority);

        // When...
        List<String> errors = tagProcessor.processResource(tagJson, DELETE, requestUsername);

        // Then...
        assertThat(errors).isNotEmpty();
        assertThat(errors.get(0)).contains("GAL5443E", "Invalid tag name provided");
    }

    @Test
    public void testCanDeleteTag() throws Exception {
        // Given...
        String tagName = "mytag";
        String description = "This is a test tag";
        Integer priority = 100;
        String requestUsername = "myuser";

        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);

        Tag tag = new Tag(tagName);
        tag.setName(tagName);
        tag.setDescription(description);
        tag.setPriority(priority);

        Map<String, Tag> tags = new HashMap<>();
        tags.put(tag.getName(), tag);

        MockTagsService tagService = new MockTagsService(tags);

        RBACValidator rbacValidator = new RBACValidator(mockRbacService);
        GalasaTagProcessor tagProcessor = new GalasaTagProcessor(tagService, rbacValidator);

        JsonObject tagJson = generateTagJson(tagName, description, priority);

        // Check that we have a tag before processing
        assertThat(tags).hasSize(1);

        // When...
        List<String> errors = tagProcessor.processResource(tagJson, DELETE, requestUsername);

        // Then...
        assertThat(errors).isEmpty();
        assertThat(tags).isEmpty();
    }

    @Test
    public void testCanCreateTag() throws Exception {
        // Given...
        String tagName = "mytag";
        String description = "This is a test tag";
        Integer priority = 100;
        String requestUsername = "myuser";

        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);

        Map<String, Tag> tags = new HashMap<>();

        MockTagsService tagService = new MockTagsService(tags);

        RBACValidator rbacValidator = new RBACValidator(mockRbacService);
        GalasaTagProcessor tagProcessor = new GalasaTagProcessor(tagService, rbacValidator);

        JsonObject tagJson = generateTagJson(tagName, description, priority);

        // Check that we have no existing tags before processing
        assertThat(tags).isEmpty();

        // When...
        List<String> errors = tagProcessor.processResource(tagJson, CREATE, requestUsername);

        // Then...
        assertThat(errors).isEmpty();
        assertThat(tags).hasSize(1);
        assertThat(tags.get(tagName).getName()).isEqualTo(tagName);
        assertThat(tags.get(tagName).getDescription()).isEqualTo(description);
        assertThat(tags.get(tagName).getPriority()).isEqualTo(priority);
    }

    @Test
    public void testCreateExistingTagReturnsError() throws Exception {
        // Given...
        String tagName = "mytag";
        String description = "This is a test tag";
        Integer priority = 100;
        String requestUsername = "myuser";

        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);

        Tag tag = new Tag(tagName);
        tag.setName(tagName);
        tag.setDescription("old description");
        tag.setPriority(0);

        Map<String, Tag> tags = new HashMap<>();
        tags.put(tag.getName(), tag);


        MockTagsService tagService = new MockTagsService(tags);

        RBACValidator rbacValidator = new RBACValidator(mockRbacService);
        GalasaTagProcessor tagProcessor = new GalasaTagProcessor(tagService, rbacValidator);

        JsonObject tagJson = generateTagJson(tagName, description, priority);

        // Check that we have a tag before processing
        assertThat(tags).hasSize(1);

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            tagProcessor.processResource(tagJson, CREATE, requestUsername);
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5445E", "A tag with the provided name already exists");
    }

    @Test
    public void testCanApplyTag() throws Exception {
        // Given...
        String tagName = "mytag";
        String description = "This is a test tag";
        Integer priority = 100;
        String requestUsername = "myuser";

        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);

        Tag tag = new Tag(tagName);
        tag.setName(tagName);
        tag.setDescription("old description");
        tag.setPriority(0);

        Map<String, Tag> tags = new HashMap<>();
        tags.put(tag.getName(), tag);


        MockTagsService tagService = new MockTagsService(tags);

        RBACValidator rbacValidator = new RBACValidator(mockRbacService);
        GalasaTagProcessor tagProcessor = new GalasaTagProcessor(tagService, rbacValidator);

        JsonObject tagJson = generateTagJson(tagName, description, priority);

        // Check that we have a tag before processing
        assertThat(tags).hasSize(1);

        // When...
        List<String> errors = tagProcessor.processResource(tagJson, APPLY, requestUsername);

        // Then...
        assertThat(errors).isEmpty();
        assertThat(tags).hasSize(1);
        assertThat(tags.get(tagName).getName()).isEqualTo(tagName);
        assertThat(tags.get(tagName).getDescription()).isEqualTo(description);
        assertThat(tags.get(tagName).getPriority()).isEqualTo(priority);
    }

    @Test
    public void testCanUpdateTag() throws Exception {
        // Given...
        String tagName = "mytag";
        String description = "This is a test tag";
        Integer priority = 100;
        String requestUsername = "myuser";

        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);

        Tag tag = new Tag(tagName);
        tag.setName(tagName);
        tag.setDescription("old description");
        tag.setPriority(0);

        Map<String, Tag> tags = new HashMap<>();
        tags.put(tag.getName(), tag);


        MockTagsService tagService = new MockTagsService(tags);

        RBACValidator rbacValidator = new RBACValidator(mockRbacService);
        GalasaTagProcessor tagProcessor = new GalasaTagProcessor(tagService, rbacValidator);

        JsonObject tagJson = generateTagJson(tagName, description, priority);

        // Check that we have a tag before processing
        assertThat(tags).hasSize(1);

        // When...
        List<String> errors = tagProcessor.processResource(tagJson, UPDATE, requestUsername);

        // Then...
        assertThat(errors).isEmpty();
        assertThat(tags).hasSize(1);
        assertThat(tags.get(tagName).getName()).isEqualTo(tagName);
        assertThat(tags.get(tagName).getDescription()).isEqualTo(description);
        assertThat(tags.get(tagName).getPriority()).isEqualTo(priority);
    }

    @Test
    public void testUpdateNonExistentTagReturnsError() throws Exception {
        // Given...
        String tagName = "mytag";
        String description = "This is a test tag";
        Integer priority = 100;
        String requestUsername = "myuser";

        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);

        Map<String, Tag> tags = new HashMap<>();

        MockTagsService tagService = new MockTagsService(tags);

        RBACValidator rbacValidator = new RBACValidator(mockRbacService);
        GalasaTagProcessor tagProcessor = new GalasaTagProcessor(tagService, rbacValidator);

        JsonObject tagJson = generateTagJson(tagName, description, priority);

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            tagProcessor.processResource(tagJson, UPDATE, requestUsername);
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5441E", "Failed to find a tag with the given name");
    }
}
