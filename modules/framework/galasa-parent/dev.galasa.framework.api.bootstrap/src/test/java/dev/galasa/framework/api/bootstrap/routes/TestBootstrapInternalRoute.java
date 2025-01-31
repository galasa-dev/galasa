/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.bootstrap.routes;

import java.util.HashMap;
import java.util.Map;

import dev.galasa.framework.api.bootstrap.BootstrapServletTest;
import dev.galasa.framework.api.bootstrap.mocks.MockBootstrapServlet;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.FilledMockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.spi.FrameworkException;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
public class TestBootstrapInternalRoute extends BootstrapServletTest {

    @Test
    public void testBootstrapInternalRouteHandleGetRequestReturnsOK() throws ServletException, IOException, FrameworkException{
        // Given...
        Map<String, Object> properties = Map.of(
            "framework.config.store", "mystore",
            "framework.extra.bundles", "more.bundles",
            "framework.testcatalog.url", "myeco.dev/testcatalog"
        );

        String pathInfo = "/bootstrap";
        ResponseBuilder responseBuilder = new ResponseBuilder();
        BootstrapInternalRoute route = new BootstrapInternalRoute(responseBuilder);
        HttpServletRequest req = (HttpServletRequest) new MockHttpServletRequest(pathInfo);
        HttpServletResponse response = (HttpServletResponse) new MockHttpServletResponse();
        ServletOutputStream outStream = response.getOutputStream();

        HttpRequestContext requestContext = new HttpRequestContext(req, FilledMockEnvironment.createTestEnvironment());

        // When...
        route.onModified(properties);
        response = route.handleGetRequest(pathInfo, null, requestContext, response);

        // Then...
        String output = outStream.toString();
        assertThat(output).contains("#Galasa Bootstrap Properties\n",
            "framework.config.store=mystore\n",
            "framework.extra.bundles=more.bundles\n",
            "framework.testcatalog.url=myeco.dev/testcatalog\n");
    }

    @Test
    public void testBootstrapInternalRequestReturnsProperties() throws Exception {
        // Given...
        Map<String, Object> properties = Map.of(
            "framework.config.store", "mystore",
            "framework.testcatalog.url", "myeco.dev/testcatalog"
        );

        setServlet("");
        MockBootstrapServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.activate(properties);
        servlet.doGet(req, resp);


        // Then...
        Integer status = resp.getStatus();
        String output = outStream.toString();
        assertThat(status).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("text/plain");
        assertThat(output).contains("#Galasa Bootstrap Properties\n",
            "framework.config.store=mystore\n",
            "framework.testcatalog.url=myeco.dev/testcatalog\n");
    }

    @Test
    public void testBootstrapInternalRequestWithGoodAcceptHeaderReturnsProperties() throws Exception {
        // Given...
        Map<String, Object> properties = Map.of(
            "framework.config.store", "mystore",
            "framework.testcatalog.url", "myeco.dev/testcatalog"
        );

        setServlet();
        MockBootstrapServlet servlet = getServlet();
		MockHttpServletRequest req = new MockHttpServletRequest("");

        req.setHeader("Accept", "text/plain");

		MockHttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.activate(properties);
        servlet.doGet(req, resp);


        // Then...
        Integer status = resp.getStatus();
        String output = outStream.toString();
        assertThat(status).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("text/plain");
        assertThat(output).contains("#Galasa Bootstrap Properties\n",
            "framework.config.store=mystore\n",
            "framework.testcatalog.url=myeco.dev/testcatalog\n");
    }

    @Test
    public void testBootstrapInternalRequestWithNoPropertiesReturnsHeaderOnly() throws Exception {
        // Given...
        Map<String, Object> properties = new HashMap<>();

        setServlet("");
        MockBootstrapServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.activate(properties);
        servlet.doGet(req, resp);

        // Then...
        Integer status = resp.getStatus();
        String output = outStream.toString();
        assertThat(status).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("text/plain");
        assertThat(output).contains("#Galasa Bootstrap Properties");
        assertThat(output).doesNotContain("framework.", "=");
    }
}