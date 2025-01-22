/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.mocks;

import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.mocks.MockTimeService;
import dev.galasa.framework.api.resources.ResourcesServlet;
import dev.galasa.framework.spi.IFramework;

public class MockResourcesServlet extends ResourcesServlet {

    public MockResourcesServlet(IFramework framework, MockEnvironment env, MockTimeService timeService) {
		super(env, timeService);
        this.framework = framework;
    }

	@Override
	public void setFramework(IFramework framework) {
		super.setFramework(framework);
	}

	public IFramework getFramework() {
		return super.getFramework();
	}
    
}
