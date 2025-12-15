/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.tags.internal.routes;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.HttpMethod;
import dev.galasa.framework.api.common.mocks.FilledMockEnvironment;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.resources.GalasaResourceValidator;
import dev.galasa.framework.api.tags.mocks.MockTagsServlet;
import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.mocks.MockTagsService;
import dev.galasa.framework.spi.tags.Tag;

public class TagByNameRouteTest extends BaseServletTest {

    private JsonObject generateExpectedTagJson(String tagName, String description, int priority) {
        JsonObject tagObject = new JsonObject();
        tagObject.addProperty("apiVersion", GalasaResourceValidator.DEFAULT_API_VERSION);

        JsonObject metadata = new JsonObject();
        tagObject.add("metadata", metadata);

        String encodedName = Base64.getUrlEncoder().withoutPadding().encodeToString(tagName.getBytes(StandardCharsets.UTF_8));
        metadata.addProperty("url", "http://my-api.server/api/tags/" + encodedName);

        metadata.addProperty("name", tagName);
        metadata.addProperty("id", encodedName);

        if (description != null) {
            metadata.addProperty("description", description);
        }

        JsonObject data = new JsonObject();
        tagObject.add("data", data);
        data.addProperty("priority", priority);

        tagObject.addProperty("kind", "GalasaTag");

        return tagObject;
    }

    @Test
    public void testTagByNameRouteRegexMatchesExpectedPaths() throws Exception {
        // Given...
        Pattern routePattern = new TagByNameRoute(null, null, null, null).getPathRegex();

        // Then...
        assertThat(routePattern.matcher("/mytag").matches()).isTrue();
        assertThat(routePattern.matcher("/my-tag123/").matches()).isTrue();
        assertThat(routePattern.matcher("/123my-_-tag456/").matches()).isTrue();

        // The route should not accept the following
        assertThat(routePattern.matcher("////").matches()).isFalse();
        assertThat(routePattern.matcher("/this is a bad tag name").matches()).isFalse();
        assertThat(routePattern.matcher("/this.is.also_a.bad.tag.name").matches()).isFalse();
    }

    @Test
    public void testGetTagByNameRouteReturnsNotFoundErrorWhenNoTagWasFound() throws Exception {
        // Given...
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockTagsService mockTagsService = new MockTagsService();
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService);
        mockFramework.setTagsService(mockTagsService);

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();
        MockTagsServlet mockServlet = new MockTagsServlet(mockFramework, env);

        String encodedTagName = Base64.getUrlEncoder().withoutPadding().encodeToString("mytag".getBytes(StandardCharsets.UTF_8));

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + encodedTagName, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(404);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        checkErrorStructure(output, 5441, "GAL5441E", "Failed to find a tag with the given name");
    }

    @Test
    public void testGetTagByNameRouteWithBadTagNameReturnsError() throws Exception {
        // Given...
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockTagsService mockTagsService = new MockTagsService();
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService);
        mockFramework.setTagsService(mockTagsService);

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();
        MockTagsServlet mockServlet = new MockTagsServlet(mockFramework, env);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/notbase64", headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(400);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        checkErrorStructure(output, 5440, "GAL5440E", "Invalid tag name provided");
    }

    @Test
    public void testGetTagsByNameRouteReturnsSingleTagOK() throws Exception {
        // Given...
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        String tagName = "tag1";
        String description = "My first tag!";
        List<Tag> tags = new ArrayList<>();
        Tag tag1 = new Tag(tagName);
        tag1.setDescription(description);
        tag1.setPriority(100);
        tags.add(tag1);

        String tagName2 = "tag2";
        String description2 = "My second tag!";
        Tag tag2 = new Tag(tagName2);
        tag2.setDescription(description2);
        tag2.setPriority(12);
        tags.add(tag2);

        String tagName3 = "tag3";
        String description3 = "My third tag!";
        Tag tag3 = new Tag(tagName3);
        tag3.setDescription(description3);
        tag3.setPriority(456);
        tags.add(tag3);

        MockTagsService mockTagsService = new MockTagsService(tags);

        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService);
        mockFramework.setTagsService(mockTagsService);

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();
        MockTagsServlet mockServlet = new MockTagsServlet(mockFramework, env);

        String encodedTagName = Base64.getUrlEncoder().withoutPadding().encodeToString(tagName2.getBytes(StandardCharsets.UTF_8));

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + encodedTagName, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");

        String expectedJson = gson.toJson(generateExpectedTagJson(tagName2, description2, 12));
        assertThat(output).isEqualTo(expectedJson);
    }

    @Test
    public void testCanDeleteTagByName() throws Exception {
        // Given...
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        String tagName = "tag1";
        String description = "My first tag!";
        List<Tag> tags = new ArrayList<>();
        Tag tag1 = new Tag(tagName);
        tag1.setDescription(description);
        tag1.setPriority(100);
        tags.add(tag1);

        String tagName2 = "tag2";
        String description2 = "My second tag!";
        Tag tag2 = new Tag(tagName2);
        tag2.setDescription(description2);
        tag2.setPriority(12);
        tags.add(tag2);

        MockTagsService mockTagsService = new MockTagsService(tags);

        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService);
        mockFramework.setTagsService(mockTagsService);

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();
        MockTagsServlet mockServlet = new MockTagsServlet(mockFramework, env);

        String encodedTagName = Base64.getUrlEncoder().withoutPadding().encodeToString(tagName2.getBytes(StandardCharsets.UTF_8));

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + encodedTagName, headerMap);
        mockRequest.setMethod(HttpMethod.DELETE.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doDelete(mockRequest, servletResponse);

        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(204);
        assertThat(output).isEmpty();
    }

    @Test
    public void testDeleteUnknownTagByNameReturnsError() throws Exception {
        // Given...
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        String tagName = "tag1";
        String description = "My first tag!";
        List<Tag> tags = new ArrayList<>();
        Tag tag1 = new Tag(tagName);
        tag1.setDescription(description);
        tag1.setPriority(100);
        tags.add(tag1);

        String tagName2 = "tag2";
        String description2 = "My second tag!";
        Tag tag2 = new Tag(tagName2);
        tag2.setDescription(description2);
        tag2.setPriority(12);
        tags.add(tag2);

        MockTagsService mockTagsService = new MockTagsService(tags);

        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService);
        mockFramework.setTagsService(mockTagsService);

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();
        MockTagsServlet mockServlet = new MockTagsServlet(mockFramework, env);

        String encodedTagName = Base64.getUrlEncoder().withoutPadding().encodeToString("unknown".getBytes(StandardCharsets.UTF_8));

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + encodedTagName, headerMap);
        mockRequest.setMethod(HttpMethod.DELETE.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doDelete(mockRequest, servletResponse);

        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(404);
        checkErrorStructure(output, 5441, "GAL5441E", "Failed to find a tag with the given name");
    }
}
