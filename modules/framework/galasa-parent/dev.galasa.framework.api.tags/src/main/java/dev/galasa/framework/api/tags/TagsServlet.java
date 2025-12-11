/*
 * Copyright contributors to the Galasa project
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.tags;

import dev.galasa.framework.api.common.BaseServlet;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.SystemEnvironment;
import dev.galasa.framework.api.tags.internal.routes.TagsRoute;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.tags.ITagsService;
import dev.galasa.framework.spi.tags.TagsException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;


@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
    "osgi.http.whiteboard.servlet.pattern=/tags/*" }, name = "Galasa Tags microservice")
public class TagsServlet extends BaseServlet {

    @Reference
    protected IFramework framework;

    private static final long serialVersionUID = 1L;
    private Log logger = LogFactory.getLog(getClass());

    protected ITagsService tagsService;
    protected RBACService rbacService;

    public TagsServlet() {
        this(new SystemEnvironment());
    }

    public TagsServlet(Environment env) {
        super(env);
    }

    @Override
    public void init() throws ServletException {
        logger.info("Galasa Tags API initialising");

        try {
            rbacService = framework.getRBACService();
            tagsService = framework.getTagsService();

            String externalApiServerUrl = env.getenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL);

            addRoute(new TagsRoute(getResponseBuilder(), externalApiServerUrl, tagsService, rbacService));
            
        } catch (RBACException | TagsException ex) {
            throw new ServletException("Failed to initialise Tags service",ex);
        }

        logger.info("Galasa Tags API initialised");
    }
    
}
