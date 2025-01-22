/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import dev.galasa.framework.api.common.mocks.FilledMockEnvironment;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.rbac.BuiltInAction;

import static org.assertj.core.api.Assertions.*;

public class TestBaseServlet extends BaseServletTest {

    private ResponseBuilder mockResponseBuilder = new ResponseBuilder(new MockEnvironment());

    class MockRoute implements IRoute {

        @Override
        public Pattern getPathRegex() {
            return Pattern.compile("\\/");
        }

        @Override
        public SupportedQueryParameterNames getSupportedQueryParameterNames() {
            return SupportedQueryParameterNames.NO_QUERY_PARAMETERS_SUPPORTED;
        }

        @Override
        public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
                HttpRequestContext requestContext, HttpServletResponse response
        ) throws ServletException, IOException, FrameworkException {
            return writeMockResponse(requestContext, response);
        }

        @Override
        public HttpServletResponse handlePutRequest(String pathInfo,
                HttpRequestContext requestContext, HttpServletResponse response
        ) throws ServletException, IOException, FrameworkException {
            return writeMockResponse(requestContext, response);
        }

        @Override
        public HttpServletResponse handlePostRequest(String pathInfo,
                HttpRequestContext requestContext, HttpServletResponse response
        ) throws ServletException, IOException, FrameworkException {
            return writeMockResponse(requestContext, response);
        }

        @Override
        public HttpServletResponse handleDeleteRequest(String pathInfo,
                HttpRequestContext requestContext, HttpServletResponse response
        ) throws ServletException, IOException, FrameworkException {
            return writeMockResponse(requestContext, response);
        }

        private HttpServletResponse writeMockResponse(HttpRequestContext requestContext, HttpServletResponse response) throws IOException {
            return mockResponseBuilder.buildResponse(requestContext.getRequest(), response, HttpServletResponse.SC_OK);
        }

        @Override
        public boolean isActionPermitted(BuiltInAction action, String loginId) throws InternalServletException {
            return true;
        }
    }


    @Test
    public void testBaseServletMatchesRouteGetRequestOk() throws Exception {
        // Given...
        BaseServlet servlet = new BaseServlet(FilledMockEnvironment.createTestEnvironment());
        MockRoute mockRoute = new MockRoute();
        servlet.addRoute(mockRoute);

        MockHttpServletRequest req = new MockHttpServletRequest("/");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        req.setMethod(HttpMethod.GET.toString());

        // When...
        servlet.doGet(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    public void testBaseServletMatchesRoutePostRequestOk() throws Exception {
        // Given...
        BaseServlet servlet = new BaseServlet(FilledMockEnvironment.createTestEnvironment());
        MockRoute mockRoute = new MockRoute();
        servlet.addRoute(mockRoute);

        MockHttpServletRequest req = new MockHttpServletRequest("{}", "/");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        req.setMethod(HttpMethod.POST.toString());

        // When...
        servlet.doPost(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    public void testBaseServletMatchesRouteDeleteRequestOk() throws Exception {
        // Given...
        BaseServlet servlet = new BaseServlet(FilledMockEnvironment.createTestEnvironment());
        MockRoute mockRoute = new MockRoute();
        servlet.addRoute(mockRoute);

        MockHttpServletRequest req = new MockHttpServletRequest("/");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        req.setMethod(HttpMethod.DELETE.toString());

        // When...
        servlet.doDelete(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    public void testBaseServletMatchesRoutePutRequestOk() throws Exception {
        // Given...
        BaseServlet servlet = new BaseServlet(FilledMockEnvironment.createTestEnvironment());
        MockRoute mockRoute = new MockRoute();
        servlet.addRoute(mockRoute);

        MockHttpServletRequest req = new MockHttpServletRequest("/");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        req.setMethod(HttpMethod.PUT.toString());

        // When...
        servlet.doPut(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    public void testBaseServletThrowsErrorWhenGivenUnsupportedRequestMethod() throws Exception {
        // Given...
        BaseServlet servlet = new BaseServlet(FilledMockEnvironment.createTestEnvironment());
        MockRoute mockRoute = new MockRoute();
        servlet.addRoute(mockRoute);

        MockHttpServletRequest req = new MockHttpServletRequest("/");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        String method = "badmethod";
        req.setMethod(method);

        // When...
        servlet.doGet(req, resp);

        // Then...
        OutputStream outputStream = resp.getOutputStream();
        assertThat(resp.getStatus()).isEqualTo(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        checkErrorStructure(outputStream.toString(), 5405, "GAL5405E", method);
    }

    @Test
    public void testBaseServletWithRouteThatThrowsInternalServletErrorReturnsError() throws Exception {
        // Given...
        BaseServlet servlet = new BaseServlet(FilledMockEnvironment.createTestEnvironment());
        MockRoute mockRoute = new MockRoute() {
            @Override
            public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
                    HttpRequestContext requestContext, HttpServletResponse response
            ) throws ServletException, IOException, FrameworkException {
                // Simulate an unauthorised error coming back from the route
                ServletError error = new ServletError(ServletErrorMessage.GAL5401_UNAUTHORIZED);
                throw new InternalServletException(error, HttpServletResponse.SC_UNAUTHORIZED);
            }
        };
        servlet.addRoute(mockRoute);

        MockHttpServletRequest req = new MockHttpServletRequest("/");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        req.setMethod(HttpMethod.GET.toString());

        // When...
        servlet.doGet(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testBaseServletWithRouteThatThrowsErrorReturnsInternalServerError() throws Exception {
        // Given...
        BaseServlet servlet = new BaseServlet(FilledMockEnvironment.createTestEnvironment());
        MockRoute mockRoute = new MockRoute() {
            @Override
            public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
                    HttpRequestContext requestContext, HttpServletResponse response
            ) throws ServletException, IOException, FrameworkException {
                throw new FrameworkException("simulating a failed framework operation");
            }
        };
        servlet.addRoute(mockRoute);

        MockHttpServletRequest req = new MockHttpServletRequest("/");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        req.setMethod(HttpMethod.GET.toString());

        // When...
        servlet.doGet(req, resp);

        // Then...
        OutputStream outputStream = resp.getOutputStream();
        assertThat(resp.getStatus()).isEqualTo(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        checkErrorStructure(outputStream.toString(),
            5000,
            "GAL5000E",
            "Error occurred when trying to access the endpoint"
        );
    }

    @Test
    public void testBaseServletWithNonMatchingRouteRequestReturnsNotFoundError() throws Exception {
        // Given...
        BaseServlet servlet = new BaseServlet(FilledMockEnvironment.createTestEnvironment());
        MockRoute mockRoute = new MockRoute() {
            @Override
            public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
                    HttpRequestContext requestContext, HttpServletResponse response
            ) throws ServletException, IOException, FrameworkException {
                throw new FrameworkException("simulating a failed framework operation");
            }
        };
        servlet.addRoute(mockRoute);

        MockHttpServletRequest req = new MockHttpServletRequest("not-a-matching-route");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        req.setMethod(HttpMethod.GET.toString());

        // When...
        servlet.doGet(req, resp);

        // Then...
        OutputStream outputStream = resp.getOutputStream();
        assertThat(resp.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
        checkErrorStructure(outputStream.toString(),
            5404,
            "GAL5404E",
            "Error occurred when trying to identify the endpoint"
        );
    }
}
