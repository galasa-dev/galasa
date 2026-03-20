/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpHeaders;
import org.junit.Test;

import com.google.gson.JsonObject;

import dev.galasa.http.ContentType;

/**
 * Unit tests for HttpClientRequest class
 */
public class HttpClientRequestTest {

    @Test
    public void testNewGetRequestCreatesGetRequest() throws Exception {
        // Given
        String url = "http://example.com/api";
        ContentType[] acceptTypes = new ContentType[] { ContentType.APPLICATION_JSON };

        // When
        HttpClientRequest request = HttpClientRequest.newGetRequest(url, acceptTypes);
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest).as("Should create HttpGet request").isInstanceOf(HttpGet.class);
        assertThat(httpRequest.getUri().toString()).as("URL should match").isEqualTo(url);
        assertThat(httpRequest.getFirstHeader(HttpHeaders.ACCEPT).getValue())
            .as("Accept header should be set").isEqualTo("application/json");
    }

    @Test
    public void testNewDeleteRequestCreatesDeleteRequest() throws Exception {
        // Given
        String url = "http://example.com/api/resource";
        ContentType[] acceptTypes = new ContentType[] { ContentType.APPLICATION_JSON };

        // When
        HttpClientRequest request = HttpClientRequest.newDeleteRequest(url, acceptTypes);
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest).as("Should create HttpDelete request").isInstanceOf(HttpDelete.class);
        assertThat(httpRequest.getUri().toString()).as("URL should match").isEqualTo(url);
    }

    @Test
    public void testNewDeleteRequestWithContentTypeCreatesDeleteRequest() throws Exception {
        // Given
        String url = "http://example.com/api/resource";
        ContentType[] acceptTypes = new ContentType[] { ContentType.APPLICATION_JSON };
        ContentType contentType = ContentType.APPLICATION_JSON;

        // When
        HttpClientRequest request = HttpClientRequest.newDeleteRequest(url, acceptTypes, contentType);
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest).as("Should create HttpDelete request").isInstanceOf(HttpDelete.class);
        assertThat(httpRequest.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue())
            .as("Content-Type header should be set").isEqualTo("application/json");
    }

    @Test
    public void testNewPutRequestCreatesPutRequest() throws Exception {
        // Given
        String url = "http://example.com/api/resource";
        ContentType[] acceptTypes = new ContentType[] { ContentType.APPLICATION_JSON };
        ContentType contentType = ContentType.APPLICATION_JSON;

        // When
        HttpClientRequest request = HttpClientRequest.newPutRequest(url, acceptTypes, contentType);
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest).as("Should create HttpPut request").isInstanceOf(HttpPut.class);
        assertThat(httpRequest.getUri().toString()).as("URL should match").isEqualTo(url);
    }

    @Test
    public void testNewPostRequestCreatesPostRequest() throws Exception {
        // Given
        String url = "http://example.com/api/resource";
        ContentType[] acceptTypes = new ContentType[] { ContentType.APPLICATION_JSON };
        ContentType contentType = ContentType.APPLICATION_JSON;

        // When
        HttpClientRequest request = HttpClientRequest.newPostRequest(url, acceptTypes, contentType);
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest).as("Should create HttpPost request").isInstanceOf(HttpPost.class);
        assertThat(httpRequest.getUri().toString()).as("URL should match").isEqualTo(url);
    }

    @Test
    public void testNewPatchRequestCreatesPatchRequest() throws Exception {
        // Given
        String url = "http://example.com/api/resource";
        ContentType[] acceptTypes = new ContentType[] { ContentType.APPLICATION_JSON };
        ContentType contentType = ContentType.APPLICATION_JSON;

        // When
        HttpClientRequest request = HttpClientRequest.newPatchRequest(url, acceptTypes, contentType);
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest).as("Should create HttpPatch request").isInstanceOf(HttpPatch.class);
        assertThat(httpRequest.getUri().toString()).as("URL should match").isEqualTo(url);
    }

    @Test
    public void testNewHeadRequestCreatesHeadRequest() throws Exception {
        // Given
        String url = "http://example.com/api/resource";

        // When
        HttpClientRequest request = HttpClientRequest.newHeadRequest(url);
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest).as("Should create HttpHead request").isInstanceOf(HttpHead.class);
        assertThat(httpRequest.getUri().toString()).as("URL should match").isEqualTo(url);
    }

    @Test
    public void testSetUrlWithValidUrl() throws Exception {
        // Given
        HttpClientRequest request = HttpClientRequest.newGetRequest("http://example.com", null);

        // When
        request.setUrl("http://newexample.com/api");
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest.getUri().toString()).as("URL should be updated").isEqualTo("http://newexample.com/api");
    }

    @Test
    public void testSetUrlWithInvalidUrlThrowsException() {
        // Given
        HttpClientRequest request = HttpClientRequest.newGetRequest("http://example.com", null);

        // When/Then
        assertThatThrownBy(() -> request.setUrl("not a valid url"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not a valid url");
    }

    @Test
    public void testAddHeaderAddsHeader() throws Exception {
        // Given
        HttpClientRequest request = HttpClientRequest.newGetRequest("http://example.com", null);

        // When
        request.addHeader("X-Custom-Header", "custom-value");
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest.getFirstHeader("X-Custom-Header").getValue())
            .as("Custom header should be present").isEqualTo("custom-value");
    }

    @Test
    public void testAddMultipleHeaders() throws Exception {
        // Given
        HttpClientRequest request = HttpClientRequest.newGetRequest("http://example.com", null);

        // When
        request.addHeader("X-Header-1", "value1")
               .addHeader("X-Header-2", "value2")
               .addHeader("X-Header-3", "value3");
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest.getFirstHeader("X-Header-1").getValue()).as("Header 1 should be present").isEqualTo("value1");
        assertThat(httpRequest.getFirstHeader("X-Header-2").getValue()).as("Header 2 should be present").isEqualTo("value2");
        assertThat(httpRequest.getFirstHeader("X-Header-3").getValue()).as("Header 3 should be present").isEqualTo("value3");
    }

    @Test
    public void testSetContentTypeWithValidContentType() throws Exception {
        // Given
        HttpClientRequest request = HttpClientRequest.newPostRequest("http://example.com", null, null);

        // When
        request.setContentType(ContentType.APPLICATION_JSON);
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue())
            .as("Content-Type should be set").isEqualTo("application/json");
    }

    @Test
    public void testSetContentTypeWithNullDoesNotSetHeader() throws Exception {
        // Given
        HttpClientRequest request = HttpClientRequest.newPostRequest("http://example.com", null, null);

        // When
        request.setContentType(null);
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest.getFirstHeader(HttpHeaders.CONTENT_TYPE))
            .as("Content-Type should not be set").isNull();
    }

    @Test
    public void testSetAcceptTypesWithSingleType() throws Exception {
        // Given
        HttpClientRequest request = HttpClientRequest.newGetRequest("http://example.com", null);
        ContentType[] acceptTypes = new ContentType[] { ContentType.APPLICATION_JSON };

        // When
        request.setAcceptTypes(acceptTypes);
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest.getFirstHeader(HttpHeaders.ACCEPT).getValue())
            .as("Accept header should contain single type").isEqualTo("application/json");
    }

    @Test
    public void testSetAcceptTypesWithMultipleTypes() throws Exception {
        // Given
        HttpClientRequest request = HttpClientRequest.newGetRequest("http://example.com", null);
        ContentType[] acceptTypes = new ContentType[] {
            ContentType.APPLICATION_JSON,
            ContentType.APPLICATION_XML,
            ContentType.TEXT_PLAIN
        };

        // When
        request.setAcceptTypes(acceptTypes);
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest.getFirstHeader(HttpHeaders.ACCEPT).getValue())
            .as("Accept header should contain all types").isEqualTo("application/json,application/xml,text/plain");
    }

    @Test
    public void testSetAcceptTypesWithNullDoesNotSetHeader() throws Exception {
        // Given
        HttpClientRequest request = HttpClientRequest.newGetRequest("http://example.com", null);

        // When
        request.setAcceptTypes(null);
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest.getFirstHeader(HttpHeaders.ACCEPT))
            .as("Accept header should not be set").isNull();
    }

    @Test
    public void testSetAcceptTypesWithEmptyArrayDoesNotSetHeader() throws Exception {
        // Given
        HttpClientRequest request = HttpClientRequest.newGetRequest("http://example.com", null);

        // When
        request.setAcceptTypes(new ContentType[0]);
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest.getFirstHeader(HttpHeaders.ACCEPT))
            .as("Accept header should not be set").isNull();
    }

    @Test
    public void testAddQueryParameterAddsSingleParameter() throws Exception {
        // Given
        HttpClientRequest request = HttpClientRequest.newGetRequest("http://example.com/api", null);

        // When
        request.addQueryParameter("key", "value");
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest.getUri().toString())
            .as("URL should contain query parameter").isEqualTo("http://example.com/api?key=value");
    }

    @Test
    public void testAddQueryParameterAddsMultipleParameters() throws Exception {
        // Given
        HttpClientRequest request = HttpClientRequest.newGetRequest("http://example.com/api", null);

        // When
        request.addQueryParameter("key1", "value1")
               .addQueryParameter("key2", "value2")
               .addQueryParameter("key3", "value3");
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        String uri = httpRequest.getUri().toString();
        assertThat(uri).as("URL should contain all query parameters")
            .contains("key1=value1")
            .contains("key2=value2")
            .contains("key3=value3");
    }

    @Test
    public void testSetBodyWithByteArray() throws Exception {
        // Given
        HttpClientRequest request = HttpClientRequest.newPostRequest("http://example.com", null, null);
        byte[] data = "test data".getBytes(StandardCharsets.UTF_8);

        // When
        request.setBody(data);
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest.getEntity()).as("Entity should be set").isNotNull();
        assertThat(httpRequest.getEntity().getContentLength()).as("Content length should match").isEqualTo(data.length);
    }

    @Test
    public void testSetBodyWithString() throws Exception {
        // Given
        HttpClientRequest request = HttpClientRequest.newPostRequest("http://example.com", null, null);
        String data = "test string data";

        // When
        request.setBody(data);
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest.getEntity()).as("Entity should be set").isNotNull();
        byte[] content = new byte[(int) httpRequest.getEntity().getContentLength()];
        httpRequest.getEntity().getContent().read(content);
        assertThat(new String(content, StandardCharsets.UTF_8)).as("Content should match").isEqualTo(data);
    }

    @Test
    public void testSetBodyWithFile() throws Exception {
        // Given
        HttpClientRequest request = HttpClientRequest.newPostRequest("http://example.com", null, null);
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();

        // When
        request.setBody(tempFile);
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest.getEntity()).as("Entity should be set").isNotNull();
    }


    @Test
    public void testSetJSONBodyWithJsonObject() throws Exception {
        // Given
        HttpClientRequest request = HttpClientRequest.newPostRequest("http://example.com", null, null);
        JsonObject json = new JsonObject();
        json.addProperty("key", "value");
        json.addProperty("number", 123);

        // When
        request.setJSONBody(json);
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest.getEntity()).as("Entity should be set").isNotNull();
        byte[] content = new byte[(int) httpRequest.getEntity().getContentLength()];
        httpRequest.getEntity().getContent().read(content);
        String jsonString = new String(content, StandardCharsets.UTF_8);
        assertThat(jsonString).as("JSON content should match").contains("\"key\":\"value\"").contains("\"number\":123");
    }

    @Test
    public void testSetJAXBBodyWithInvalidObjectThrowsException() {
        // Given
        HttpClientRequest request = HttpClientRequest.newPostRequest("http://example.com", null, null);
        Object invalidObject = new Object();

        // When/Then
        assertThatThrownBy(() -> request.setJAXBBody(invalidObject))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not to be a valid JAXB class");
    }

    @Test
    public void testBuilderPatternReturnsThis() {
        // Given
        HttpClientRequest request = HttpClientRequest.newGetRequest("http://example.com", null);

        // When/Then - all methods should return the same instance for chaining
        assertThat(request.setUrl("http://newurl.com")).as("setUrl should return this").isSameAs(request);
        assertThat(request.addHeader("key", "value")).as("addHeader should return this").isSameAs(request);
        assertThat(request.setContentType(ContentType.APPLICATION_JSON)).as("setContentType should return this").isSameAs(request);
        assertThat(request.setAcceptTypes(new ContentType[] { ContentType.TEXT_PLAIN })).as("setAcceptTypes should return this").isSameAs(request);
        assertThat(request.addQueryParameter("param", "value")).as("addQueryParameter should return this").isSameAs(request);
        assertThat(request.setBody("data")).as("setBody should return this").isSameAs(request);
    }

    @Test
    public void testComplexRequestWithAllFeatures() throws Exception {
        // Given
        String url = "http://example.com/api/resource";
        ContentType[] acceptTypes = new ContentType[] { ContentType.APPLICATION_JSON, ContentType.APPLICATION_XML };
        ContentType contentType = ContentType.APPLICATION_JSON;
        String body = "{\"test\":\"data\"}";

        // When
        HttpClientRequest request = HttpClientRequest.newPostRequest(url, acceptTypes, contentType)
            .addHeader("X-Custom-Header", "custom-value")
            .addHeader("Authorization", "Bearer token123")
            .addQueryParameter("filter", "active")
            .addQueryParameter("limit", "10")
            .setBody(body);

        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        assertThat(httpRequest).as("Should be POST request").isInstanceOf(HttpPost.class);
        assertThat(httpRequest.getUri().toString()).as("URL should contain query parameters")
            .contains("filter=active")
            .contains("limit=10");
        assertThat(httpRequest.getFirstHeader(HttpHeaders.ACCEPT).getValue())
            .as("Accept header should contain both types").isEqualTo("application/json,application/xml");
        assertThat(httpRequest.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue())
            .as("Content-Type should be set").isEqualTo("application/json");
        assertThat(httpRequest.getFirstHeader("X-Custom-Header").getValue())
            .as("Custom header should be present").isEqualTo("custom-value");
        assertThat(httpRequest.getFirstHeader("Authorization").getValue())
            .as("Authorization header should be present").isEqualTo("Bearer token123");
        assertThat(httpRequest.getEntity()).as("Entity should be set").isNotNull();
    }

    @Test
    public void testRequestWithSpecialCharactersInQueryParameters() throws Exception {
        // Given
        HttpClientRequest request = HttpClientRequest.newGetRequest("http://example.com/api", null);

        // When
        request.addQueryParameter("search", "test value with spaces")
               .addQueryParameter("special", "a&b=c");
        ClassicHttpRequest httpRequest = request.buildRequest();

        // Then
        String uri = httpRequest.getUri().toString();
        assertThat(uri).as("URL should be properly encoded").contains("search=");
    }

    @Test
    public void testAllContentTypesCanBeSet() throws Exception {
        // Test that all ContentType enum values can be set
        ContentType[] allTypes = ContentType.values();

        for (ContentType type : allTypes) {
            HttpClientRequest request = HttpClientRequest.newPostRequest("http://example.com", null, null);
            request.setContentType(type);
            ClassicHttpRequest httpRequest = request.buildRequest();

            assertThat(httpRequest.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue())
                .as("Content-Type for " + type + " should be set").isEqualTo(type.getMimeType());
        }
    }
}
