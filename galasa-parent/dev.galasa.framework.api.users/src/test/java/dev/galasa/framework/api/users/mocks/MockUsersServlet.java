/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.users.mocks;

import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.users.UsersServlet;

public class MockUsersServlet extends UsersServlet{

	public MockUsersServlet(MockFramework framework, MockEnvironment env) {
        this.framework = framework;
        this.env = env;
        setResponseBuilder(new ResponseBuilder(env));
    }
}
