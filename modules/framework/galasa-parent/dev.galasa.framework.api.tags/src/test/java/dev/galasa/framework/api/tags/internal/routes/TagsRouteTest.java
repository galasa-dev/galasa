/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.tags.internal.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import com.google.gson.JsonArray;

import dev.galasa.framework.api.beans.generated.GalasaTag;
import dev.galasa.framework.api.beans.generated.TagCreateRequest;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.HttpMethod;
import dev.galasa.framework.api.common.mocks.FilledMockEnvironment;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.tags.mocks.MockTagsServlet;
import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.mocks.MockTagsService;
import dev.galasa.framework.spi.tags.Tag;

public class TagsRouteTest extends TagsServletTest {

    private String getTagCreateRequestJsonString(String tagName, String tagDescription, int tagPriority) {
        TagCreateRequest requestPayload = new TagCreateRequest();
        requestPayload.setname(tagName);
        requestPayload.setdescription(tagDescription);
        requestPayload.setpriority(tagPriority);

        return gson.toJson(requestPayload);
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
        Map<String, Tag> tags = new HashMap<>();
        Tag tag1 = new Tag(tagName);
        tag1.setDescription(description);
        tag1.setPriority(100);
        tags.put(tag1.getName(), tag1);

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

        Map<String, Tag> tags = new HashMap<>();

        String tagName = "tag1";
        String description = "My first tag!";
        Tag tag1 = new Tag(tagName);
        tag1.setDescription(description);
        tag1.setPriority(100);
        tags.put(tag1.getName(), tag1);

        String tagName2 = "tag2";
        String description2 = "My second tag!";
        Tag tag2 = new Tag(tagName2);
        tag2.setDescription(description2);
        tag2.setPriority(12);
        tags.put(tag2.getName(), tag2);

        String tagName3 = "tag3";
        String description3 = "My third tag!";
        Tag tag3 = new Tag(tagName3);
        tag3.setDescription(description3);
        tag3.setPriority(456);
        tags.put(tag3.getName(), tag3);

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

    @Test
    public void testGetTagsWithNameQueryParameterReturnsMatchingTag() throws Exception {
        // Given...
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        Map<String, Tag> tags = new HashMap<>();

        String tagName = "tag1";
        String description = "My first tag!";
        Tag tag1 = new Tag(tagName);
        tag1.setDescription(description);
        tag1.setPriority(100);
        tags.put(tag1.getName(), tag1);

        String tagName2 = "tag2";
        String description2 = "My second tag!";
        Tag tag2 = new Tag(tagName2);
        tag2.setDescription(description2);
        tag2.setPriority(12);
        tags.put(tag2.getName(), tag2);

        String tagName3 = "tag3";
        String description3 = "My third tag!";
        Tag tag3 = new Tag(tagName3);
        tag3.setDescription(description3);
        tag3.setPriority(456);
        tags.put(tag3.getName(), tag3);

        MockTagsService mockTagsService = new MockTagsService(tags);

        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService);
        mockFramework.setTagsService(mockTagsService);

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();
        MockTagsServlet mockServlet = new MockTagsServlet(mockFramework, env);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(null, headerMap);
        mockRequest.setQueryParameter("name", tagName3);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
    
        JsonArray expectedJsonArray = new JsonArray();
        expectedJsonArray.add(generateExpectedTagJson(tagName3, description3, 456));

        assertThat(output).isEqualTo(gson.toJson(expectedJsonArray));
    }

    @Test
    public void testGetTagsWithNameQueryParameterEmptyListWhenNoneMatch() throws Exception {
        // Given...
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        Map<String, Tag> tags = new HashMap<>();

        String tagName = "tag1";
        String description = "My first tag!";
        Tag tag1 = new Tag(tagName);
        tag1.setDescription(description);
        tag1.setPriority(100);
        tags.put(tag1.getName(), tag1);

        String tagName2 = "tag2";
        String description2 = "My second tag!";
        Tag tag2 = new Tag(tagName2);
        tag2.setDescription(description2);
        tag2.setPriority(12);
        tags.put(tag2.getName(), tag2);

        String tagName3 = "tag3";
        String description3 = "My third tag!";
        Tag tag3 = new Tag(tagName3);
        tag3.setDescription(description3);
        tag3.setPriority(456);
        tags.put(tag3.getName(), tag3);

        MockTagsService mockTagsService = new MockTagsService(tags);

        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService);
        mockFramework.setTagsService(mockTagsService);

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();
        MockTagsServlet mockServlet = new MockTagsServlet(mockFramework, env);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(null, headerMap);
        mockRequest.setQueryParameter("name", "this is an unknown tag!");

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
    
        JsonArray expectedJsonArray = new JsonArray();
        assertThat(output).isEqualTo(gson.toJson(expectedJsonArray));
    }

    @Test
    public void testCanCreateTag() throws Exception {
        // Given...
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockTagsService mockTagsService = new MockTagsService();

        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService);
        mockFramework.setTagsService(mockTagsService);

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();
        MockTagsServlet mockServlet = new MockTagsServlet(mockFramework, env);

        String tagName = "tag1";
        String tagDescription = "my first tag!";
        int tagPriority = 150;
        String tagRequestJson = getTagCreateRequestJsonString(tagName, tagDescription, tagPriority);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(null, headerMap);
        mockRequest.setMethod(HttpMethod.POST.toString());
        mockRequest.setPayload(tagRequestJson);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doPost(mockRequest, servletResponse);

        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(201);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
    
        assertThat(output).isEqualTo(gson.toJson(generateExpectedTagJson(tagName, tagDescription, tagPriority)));
    }

    @Test
    public void testCreateTagWithBadDescriptionReturnsError() throws Exception {
        // Given...
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockTagsService mockTagsService = new MockTagsService();

        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService);
        mockFramework.setTagsService(mockTagsService);

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();
        MockTagsServlet mockServlet = new MockTagsServlet(mockFramework, env);

        String tagName = "tag1";
        String tagDescription = "     ";
        int tagPriority = 150;
        String tagRequestJson = getTagCreateRequestJsonString(tagName, tagDescription, tagPriority);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(null, headerMap);
        mockRequest.setMethod(HttpMethod.POST.toString());
        mockRequest.setPayload(tagRequestJson);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doPost(mockRequest, servletResponse);

        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(400);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");

        checkErrorStructure(output, 5444, "GAL5444E", "Invalid tag description provided");
    }

    @Test
    public void testCreateTagWithNoNameReturnsError() throws Exception {
        // Given...
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockTagsService mockTagsService = new MockTagsService();

        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService);
        mockFramework.setTagsService(mockTagsService);

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();
        MockTagsServlet mockServlet = new MockTagsServlet(mockFramework, env);

        String tagName = null;
        String tagDescription = "my first tag!";
        int tagPriority = 150;
        String tagRequestJson = getTagCreateRequestJsonString(tagName, tagDescription, tagPriority);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(null, headerMap);
        mockRequest.setMethod(HttpMethod.POST.toString());
        mockRequest.setPayload(tagRequestJson);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doPost(mockRequest, servletResponse);

        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(400);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");

        checkErrorStructure(output, 5443, "GAL5443E", "Invalid tag name provided");
    }

    @Test
    public void testCreateExistingTagReturnsError() throws Exception {
        // Given...
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        Map<String, Tag> tags = new HashMap<>();

        String tagName = "tag1";
        String tagDescription = "my first tag!";
        int tagPriority = 150;
        Tag tag1 = new Tag(tagName);
        tag1.setDescription(tagDescription);
        tag1.setPriority(tagPriority);
        tags.put(tag1.getName(), tag1);

        MockTagsService mockTagsService = new MockTagsService(tags);

        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService);
        mockFramework.setTagsService(mockTagsService);

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();
        MockTagsServlet mockServlet = new MockTagsServlet(mockFramework, env);

        String tagRequestJson = getTagCreateRequestJsonString(tagName, tagDescription, tagPriority);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(null, headerMap);
        mockRequest.setMethod(HttpMethod.POST.toString());
        mockRequest.setPayload(tagRequestJson);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doPost(mockRequest, servletResponse);

        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(409);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");

        checkErrorStructure(output, 5445, "GAL5445E", "A tag with the provided name already exists");
    }
}
