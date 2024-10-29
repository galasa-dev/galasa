/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.health.internal;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import dev.galasa.framework.spi.IFramework;

/**
 * Simple servlet to check the Galasa framework is initialised.
 *
 * Does not require authentication
 *
 *  
 *
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
        "osgi.http.whiteboard.servlet.pattern=/health" }, name = "Galasa Health")
public class Health extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Log logger = LogFactory.getLog(getClass());

    @Reference
    public IFramework         framework;            // NOSONAR

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (this.framework == null) {
            resp.setStatus(503);
            resp.setContentType("text/plain");
            resp.getWriter().write("Galasa framework service is not installed"); // NOSONAR //TODO catch this as SQ says
            return;
        }

        if (!this.framework.isInitialised()) {
            resp.setStatus(503);
            resp.setContentType("text/plain");
            resp.getWriter().write("Galasa framework is not initialised");// NOSONAR
            return;
        }

        // All check complete, we are good to go
        resp.setStatus(200);
        resp.setContentType("text/plain");
        resp.getWriter().write("Ok");// NOSONAR
    }
    
    @Activate
    void activate(Map<String, Object> properties) {
        logger.info("Galasa Health API activated");
    }

}