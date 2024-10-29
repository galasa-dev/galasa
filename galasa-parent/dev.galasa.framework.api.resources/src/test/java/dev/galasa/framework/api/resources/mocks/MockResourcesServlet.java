/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.mocks;

import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.api.common.mocks.IServletUnderTest;
import dev.galasa.framework.api.resources.ResourcesServlet;
import dev.galasa.framework.spi.IFramework;

public class MockResourcesServlet extends ResourcesServlet implements IServletUnderTest{

	@Override
	public void setFramework(IFramework framework) {
		super.setFramework(framework);
	}

	@Override
	public void setFileSystem(IFileSystem fileSystem) {
		throw new UnsupportedOperationException("Unimplemented method 'setFileSystem'");
	}

	public IFramework getFramework() {
		return super.getFramework();
	}
    
}
