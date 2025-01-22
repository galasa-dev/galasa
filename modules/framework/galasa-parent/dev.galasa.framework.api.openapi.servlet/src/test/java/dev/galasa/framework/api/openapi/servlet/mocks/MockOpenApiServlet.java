/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.openapi.servlet.mocks;

import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.openapi.servlet.OpenApiServlet;

public class MockOpenApiServlet extends OpenApiServlet {

    public MockOpenApiServlet(Environment env) {
        super(env);
        setResponseBuilder(new ResponseBuilder(env));
    }
}
