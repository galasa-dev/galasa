/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.bootstrap.routes;

import dev.galasa.framework.api.bootstrap.BootstrapServlet;
import dev.galasa.framework.api.bootstrap.BootstrapServletTest;
import dev.galasa.framework.api.bootstrap.mocks.MockBootstrapServlet;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
public class TestBootstrapExternalRoute extends BootstrapServletTest {

    @Test
    public void testBootstrapExternalRouteHandleGetRequestReturnsOK() throws Exception {
        // Given...
        setServlet();
        BootstrapServlet servlet = getServlet();

        String pathInfo = "/external";
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpServletRequest req = new MockHttpServletRequest(pathInfo);
        ServletOutputStream outStream = response.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(req, response);

        // Then...
        String output = outStream.toString();
        assertThat(output).contains("#Galasa Bootstrap Properties");
        assertThat(output).doesNotContain("framework");
    }

    @Test
    public void testGetBootstrapExternalRouteWithGoodAcceptHeaderReturnsOK() throws Exception {
        // Given...
        setServlet();
        BootstrapServlet servlet = getServlet();

        String pathInfo = "/external";
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpServletRequest req = new MockHttpServletRequest(pathInfo);
        ServletOutputStream outStream = response.getOutputStream();

        req.setHeader("Accept", "text/plain");

        // When...
        servlet.init();
        servlet.doGet(req, response);

        // Then...
        String output = outStream.toString();
        assertThat(output).contains("#Galasa Bootstrap Properties");
        assertThat(output).doesNotContain("framework");
    }

    @Test
    public void testBootstrapInternalRequestReturnsProperties() throws Exception{
        // Given...
        setServlet("/external");
        MockBootstrapServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        Integer status = resp.getStatus();
        String output = outStream.toString();
        assertThat(status).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("text/plain");
        assertThat(output).contains("#Galasa Bootstrap Properties");
        assertThat(output).doesNotContain("framework.config.store=mystore\n",
            "framework.extra.bundles=more.bundles\n",
            "framework.testcatalog.url=myeco.dev/testcatalog\n");
    }
}