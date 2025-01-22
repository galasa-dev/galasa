/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.mocks.FilledMockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.spi.rbac.BuiltInAction;

import static dev.galasa.framework.api.common.MimeType.APPLICATION_JSON;
import static dev.galasa.framework.api.common.MimeType.TEXT_PLAIN;
import static org.assertj.core.api.Assertions.*;

public class TestBaseRoute {

    public class MockBaseRoute extends BaseRoute {

        public MockBaseRoute() {
            super(new ResponseBuilder(), "/");
        }

        @Override
        public boolean isActionPermitted(BuiltInAction action, String loginId) throws InternalServletException {
            return true;
        }
    }

    @Test
    public void TestBaseRouteHandleGetReturnsMethodNotAllowed() throws Exception {
        // Given...
        BaseRoute route = new MockBaseRoute();
        MockHttpServletRequest request = new MockHttpServletRequest("");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpRequestContext requestContext = new HttpRequestContext(request, FilledMockEnvironment.createTestEnvironment());

        // When...
         Throwable thrown = catchThrowable( () -> { 
            route.handleGetRequest("",null,requestContext,response);
         });

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5405","E: Error occurred when trying to access the endpoint ''. The method 'GET' is not allowed");
    }

    @Test
    public void TestBaseRouteHandlePutReturnsMethodNotAllowed() throws Exception {
        // Given...
        BaseRoute route = new MockBaseRoute();
        MockHttpServletRequest request = new MockHttpServletRequest("", "", "PUT");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpRequestContext requestContext = new HttpRequestContext(request, FilledMockEnvironment.createTestEnvironment());

        // When...
         Throwable thrown = catchThrowable( () -> { 
            route.handlePutRequest("",requestContext,response);
         });

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5405","E: Error occurred when trying to access the endpoint ''. The method 'PUT' is not allowed");
    }
    
    @Test
    public void TestBaseRouteHandlePostReturnsMethodNotAllowed() throws Exception {
        // Given...
        BaseRoute route = new MockBaseRoute();
        MockHttpServletRequest request = new MockHttpServletRequest("", "", "POST");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpRequestContext requestContext = new HttpRequestContext(request, FilledMockEnvironment.createTestEnvironment());

        // When...
         Throwable thrown = catchThrowable( () -> { 
            route.handlePostRequest("",requestContext,response);
         });

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5405","E: Error occurred when trying to access the endpoint ''. The method 'POST' is not allowed");
    }

    @Test
    public void TestBaseRouteHandleDeleteReturnsMethodNotAllowed() throws Exception {
        // Given...
        BaseRoute route = new MockBaseRoute();
        MockHttpServletRequest request = new MockHttpServletRequest("", "", "DELETE");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpRequestContext requestContext = new HttpRequestContext(request, FilledMockEnvironment.createTestEnvironment());

        // When...
         Throwable thrown = catchThrowable( () -> { 
            route.handleDeleteRequest("",requestContext,response);
         });

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5405","E: Error occurred when trying to access the endpoint ''. The method 'DELETE' is not allowed");
    }
    
    @Test
    public void TestCheckRequestHasContentReturnsError() throws Exception {
        // Given...
        BaseRoute route = new MockBaseRoute();
        MockHttpServletRequest request = new MockHttpServletRequest("", "", "DELETE");

        // When...
        Throwable thrown = catchThrowable( () -> { 
            route.checkRequestHasContent(request);
        });

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5411","E: Error occurred when trying to access the endpoint ''. The request body is empty.");
    }

    @Test
    public void TestCheckRequestHasContentNullPointerReturnsError() throws Exception {
        // Given...
        BaseRoute route = new MockBaseRoute();
        MockHttpServletRequest request = new MockHttpServletRequest("");

        // When...
        Throwable thrown = catchThrowable( () -> { 
            route.checkRequestHasContent(request);
        });

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5411","E: Error occurred when trying to access the endpoint ''. The request body is empty.");
    }

    @Test
    public void TestCheckRequestHasContentReturnsTrue() throws Exception {
        // Given...
        BaseRoute route = new MockBaseRoute();
        MockHttpServletRequest request = new MockHttpServletRequest("", "my content", "DELETE");

        // When...
        Boolean valid = route.checkRequestHasContent(request);

        // Then...
        assertThat(valid).isTrue();
    }

    @Test
    public void TestCheckJsonElementIsValidJsonValidJsonReturnsNoError() throws Exception {
        // Given...
        BaseRoute route = new MockBaseRoute();
        JsonObject json = new JsonObject();
        json.addProperty("property", "value");

        // When...
        Throwable thrown = catchThrowable( () -> { 
            route.checkJsonElementIsValidJSON(json);
        });

        // Then...
        assertThat(thrown).isNull();
    }

    @Test
    public void TestCheckJsonElementIsValidJsonEmptyJsonReturnsError() throws Exception {
        // Given...
        BaseRoute route = new MockBaseRoute();
        JsonObject json = new JsonObject();

        // When...
        Throwable thrown = catchThrowable( () -> { 
            route.checkJsonElementIsValidJSON(json);
        });

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5068","E: Error occurred. The JSON element for a resource can not be empty. Please check the request format, or check with your Ecosystem administrator.");
    }

    @Test
    public void TestCheckJsonElementIsValidJsonNullJsonReturnsError() throws Exception {
        // Given...
        BaseRoute route = new MockBaseRoute();
        JsonElement json = JsonNull.INSTANCE;
        // When...
        Throwable thrown = catchThrowable( () -> { 
            route.checkJsonElementIsValidJSON(json);
        });

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5067","E: Error occurred. A 'NULL' value is not a valid resource. Please check the request format, or check with your Ecosystem administrator.");
    }
    
    @Test
    public void TestparseRequestBodyReturnsContent() throws Exception {
        // Given...
        String content = "{\"my\":\"content\"}";
        BaseRoute route = new MockBaseRoute();
        MockHttpServletRequest request = new MockHttpServletRequest("", content, "DELETE");

        // When...
        JsonObject output = route.parseRequestBody(request,JsonObject.class);

        // Then...
        JsonObject expectedJsonObject = new JsonObject();
        expectedJsonObject.addProperty("my", "content");
        assertThat(output).isNotNull();
        assertThat(output).isEqualTo(expectedJsonObject);
    }

    @Test
    public void TestCheckRequestorAcceptContentNoHeaderReturnsOK() throws Exception {
        // Given...
        String content = "{\"my\":\"content\"}";
        BaseRoute route = new MockBaseRoute();
        MockHttpServletRequest request = new MockHttpServletRequest("/mypath", content, "PUT");
 
        // When...
        Throwable thrown = catchThrowable( () -> { 
            route.validateAcceptHeader(request);
        });

        // Then...
        assertThat(thrown).isNull();
    }

    @Test
    public void TestCheckRequestorAcceptContentAllowAllHeaderReturnsOK() throws Exception {
        // Given...
        String content = "{\"my\":\"content\"}";
        BaseRoute route = new MockBaseRoute();
        MockHttpServletRequest request = new MockHttpServletRequest("/mypath", content, "PUT");
        request.setHeader("Accept", "*/*");
 
        // When...
        Throwable thrown = catchThrowable( () -> { 
            route.validateAcceptHeader(request, MimeType.APPLICATION_JSON);
        });

        // Then...
        assertThat(thrown).isNull();
    }

    @Test
    public void TestCheckRequestorAcceptContentApplicationAllHeaderReturnsOK() throws Exception {
        // Given...
        String content = "{\"my\":\"content\"}";
        BaseRoute route = new MockBaseRoute();
        MockHttpServletRequest request = new MockHttpServletRequest("/mypath", content, "PUT");
        request.setHeader("Accept", "application/*");
 
        // When...
        Throwable thrown = catchThrowable( () -> { 
            route.validateAcceptHeader(request, APPLICATION_JSON);
        });

        // Then...
        assertThat(thrown).isNull();
    }

    @Test
    public void TestCheckRequestorAcceptContentApplicationJsonHeaderReturnsOK() throws Exception {
        // Given...
        String content = "{\"my\":\"content\"}";
        BaseRoute route = new MockBaseRoute();
        MockHttpServletRequest request = new MockHttpServletRequest("/mypath", content, "PUT");
        request.setHeader("Accept", "application/json");
 
        // When...
        Throwable thrown = catchThrowable( () -> { 
            route.validateAcceptHeader(request, APPLICATION_JSON);
        });

        // Then...
        assertThat(thrown).isNull();
    }

    @Test
    public void TestCheckRequestorAcceptContentTextPlainHeaderReturnsOK() throws Exception {
        // Given...
        String content = "{\"my\":\"content\"}";
        BaseRoute route = new MockBaseRoute();
        MockHttpServletRequest request = new MockHttpServletRequest("/mypath", content, "GET");
        request.setHeader("Accept", "text/plain");
 
        // When...
        Throwable thrown = catchThrowable( () -> { 
            route.validateAcceptHeader(request, MimeType.TEXT_PLAIN);
        });

        // Then...
        assertThat(thrown).isNull();
    }

    @Test
    public void TestCheckRequestorAcceptContentMultipleHeaderReturnsOK() throws Exception {
        // Given...
        String content = "{\"my\":\"content\"}";
        BaseRoute route = new MockBaseRoute();
        MockHttpServletRequest request = new MockHttpServletRequest("/mypath", content, "GET");
        request.setHeader("Accept", "text/plain , application/json");
 
        // When...
        Throwable thrown = catchThrowable( () -> { 
            route.validateAcceptHeader(request, APPLICATION_JSON);
        });

        // Then...
        assertThat(thrown).isNull();
    }

    @Test
    public void TestCheckRequestorAcceptContentMultipleWeightedHeaderReturnsOK() throws Exception {
        // Given...
        String content = "{\"my\":\"content\"}";
        BaseRoute route = new MockBaseRoute();
        MockHttpServletRequest request = new MockHttpServletRequest("/mypath", content, "GET");
        request.setHeader("Accept", "text/plain;q=0.9 , application/json;q=0.8");
 
        // When...
        Throwable thrown = catchThrowable( () -> { 
            route.validateAcceptHeader(request, APPLICATION_JSON);
        });

        // Then...
        assertThat(thrown).isNull();
    }

    @Test
    public void TestCheckRequestorAcceptContentJsonHeaderWithTextPlainReturnsError() throws Exception {
        // Given...
        String content = "{\"my\":\"content\"}";
        BaseRoute route = new MockBaseRoute();
        MockHttpServletRequest request = new MockHttpServletRequest("/mypath", content, "GET");
        request.setHeader("Accept", "application/json");
 
        // When...
        Throwable thrown = catchThrowable( () -> { 
            route.validateAcceptHeader(request, MimeType.TEXT_PLAIN);
        });

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5406",
            "E: Unsupported 'Accept' header value set. Supported response types are: [text/plain]. Ensure the 'Accept' header in your request contains a valid value and try again");
    }

    @Test
    public void TestCheckRequestorAcceptContentApplicationYamlHeaderReturnsException() throws Exception {
        // Given...
        String content = "{\"my\":\"content\"}";
        BaseRoute route = new MockBaseRoute();
        MockHttpServletRequest request = new MockHttpServletRequest("/mypath", content, "PUT");
        request.setHeader("Accept", "application/yaml");
 
        // When...
        Throwable thrown = catchThrowable( () -> { 
            route.validateAcceptHeader(request, APPLICATION_JSON);
        });

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5406",
            "E: Unsupported 'Accept' header value set. Supported response types are: [application/json]. Ensure the 'Accept' header in your request contains a valid value and try again");
    }

    @Test
    public void TestCheckRequestorAcceptContentAnyHeaderReturnsOK() throws Exception {
        // Given...
        String content = "{\"my\":\"content\"}";
        BaseRoute route = new MockBaseRoute();
        MockHttpServletRequest request = new MockHttpServletRequest("/mypath", content, "PUT");
        request.setHeader("Accept", "*/*");
 
        // When...
        Throwable thrown = catchThrowable( () -> { 
            route.validateAcceptHeader(request, APPLICATION_JSON);
        });

        // Then...
        assertThat(thrown).isNull();
    }

    @Test
    public void TestCheckRequestorAcceptContentTextHeaderAnyAllowedReturnsOK() throws Exception {
        // Given...
        String content = "{\"my\":\"content\"}";
        BaseRoute route = new MockBaseRoute();
        MockHttpServletRequest request = new MockHttpServletRequest("/mypath", content, "PUT");
        request.setHeader("Accept", "text/plain , */*");
 
        // When...
        Throwable thrown = catchThrowable( () -> { 
            route.validateAcceptHeader(request, TEXT_PLAIN);
        });

        // Then...
        assertThat(thrown).isNull();
    }

    @Test
    public void TestCheckRequestorAcceptContentTextHeaderExplicitAnyReturnsError() throws Exception {
        // Given...
        String content = "{\"my\":\"content\"}";
        BaseRoute route = new MockBaseRoute();
        MockHttpServletRequest request = new MockHttpServletRequest("/mypath", content, "PUT");
        request.setHeader("Accept", "text/*");
 
        // When...
        Throwable thrown = catchThrowable( () -> { 
            route.validateAcceptHeader(request, APPLICATION_JSON);
        });

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5406",
            "E: Unsupported 'Accept' header value set. Supported response types are: [application/json]. Ensure the 'Accept' header in your request contains a valid value and try again");
    }
}
