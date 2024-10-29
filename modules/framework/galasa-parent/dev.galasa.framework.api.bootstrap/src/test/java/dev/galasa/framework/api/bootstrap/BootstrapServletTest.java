/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.bootstrap;

import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.bootstrap.mocks.MockBootstrapServlet;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.api.common.mocks.MockServletOutputStream;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;

public class BootstrapServletTest extends BaseServletTest {
    

	MockBootstrapServlet servlet;
	HttpServletRequest req;
	HttpServletResponse resp;

	protected void setServlet() throws ConfigurationPropertyStoreException{
		this.servlet = new MockBootstrapServlet();
        servlet.setResponseBuilder(new ResponseBuilder(new MockEnvironment()));

		IConfigurationPropertyStoreService cpsstore;
		cpsstore = new MockIConfigurationPropertyStoreService("bootstrap");
        cpsstore.setProperty("framework.config.store","mystore");
        cpsstore.setProperty("framework.extra.bundles","more.bundles");
        cpsstore.setProperty("framework.testcatalog.url", "myeco.dev/testcatalog");
		IFramework framework = new MockFramework(cpsstore);
		this.servlet.setFramework(framework);
	}
	
	protected void setServlet(String path) throws ConfigurationPropertyStoreException{
		setServlet();
		ServletOutputStream outStream = new MockServletOutputStream();
        PrintWriter writer = new PrintWriter(outStream);
		this.req = new MockHttpServletRequest(path);
		this.resp = new MockHttpServletResponse(writer, outStream);
	}

	protected MockBootstrapServlet getServlet(){
		return this.servlet;
	}

	protected HttpServletRequest getRequest(){
		return this.req;
	}

	protected HttpServletResponse getResponse(){
	return this.resp;
	}
}