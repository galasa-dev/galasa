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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.beans.generated.GalasaTag;
import dev.galasa.framework.api.common.BaseServletTest;
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

public class TagsRouteTest extends BaseServletTest {

    private JsonObject generateExpectedTagJson(String tagName, String description, int priority) {
        JsonObject tagObject = new JsonObject();
        tagObject.addProperty("apiVersion", GalasaResourceValidator.DEFAULT_API_VERSION);

        JsonObject metadata = new JsonObject();
        tagObject.add("metadata", metadata);

        String encodedName = Base64.getUrlEncoder().withoutPadding().encodeToString(tagName.getBytes(StandardCharsets.UTF_8));
        metadata.addProperty("url", "http://my-api.server/api/tags/" + encodedName);

        metadata.addProperty("name", tagName);

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
    public void testTagsRouteRegexMatchesExpectedPaths() throws Exception {
        // Given...
        Pattern routePattern = new TagsRoute(null, null, null, null).getPathRegex();

        // Then...
        // The servlet's whiteboard pattern will match /tags, so the tags route
        // should only allow an optional / or an empty string (no suffix after "/tags")
        assertThat(routePattern.matcher("/").matches()).isTrue();
        assertThat(routePattern.matcher("").matches()).isTrue();

        // The route should not accept the following
        assertThat(routePattern.matcher("////").matches()).isFalse();
        assertThat(routePattern.matcher("/wrongpath!").matches()).isFalse();
    }

    @Test
    public void testGetTagsRouteReturnsEmptyListWhenThereAreNoTags() throws Exception {
        // Given...
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockTagsService mockTagsService = new MockTagsService();
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService);
        mockFramework.setTagsService(mockTagsService);

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();
        MockTagsServlet mockServlet = new MockTagsServlet(mockFramework, env);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(null, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
    
        GalasaTag[] tagsGotBack = gson.fromJson(output, GalasaTag[].class);
        assertThat(tagsGotBack).hasSize(0);
    }

    @Test
    public void testGetTagsRouteReturnsSingleTagOK() throws Exception {
        // Given...
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        String tagName = "tag1";
        String description = "My first tag!";
        List<Tag> tags = new ArrayList<>();
        Tag tag1 = new Tag(tagName);
        tag1.setDescription(description);
        tag1.setPriority(100);
        tags.add(tag1);

        MockTagsService mockTagsService = new MockTagsService(tags);

        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService);
        mockFramework.setTagsService(mockTagsService);

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();
        MockTagsServlet mockServlet = new MockTagsServlet(mockFramework, env);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(null, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
    
        JsonArray expectedJsonArray = new JsonArray();
        expectedJsonArray.add(generateExpectedTagJson(tagName, description, 100));

        assertThat(output).isEqualTo(gson.toJson(expectedJsonArray));
    }

    @Test
    public void testGetTagsRouteReturnsMultipleTagsOK() throws Exception {
        // Given...
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        List<Tag> tags = new ArrayList<>();

        String tagName = "tag1";
        String description = "My first tag!";
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

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(null, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
    
        JsonArray expectedJsonArray = new JsonArray();
        expectedJsonArray.add(generateExpectedTagJson(tagName, description, 100));
        expectedJsonArray.add(generateExpectedTagJson(tagName2, description2, 12));
        expectedJsonArray.add(generateExpectedTagJson(tagName3, description3, 456));

        assertThat(output).isEqualTo(gson.toJson(expectedJsonArray));
    }
}
