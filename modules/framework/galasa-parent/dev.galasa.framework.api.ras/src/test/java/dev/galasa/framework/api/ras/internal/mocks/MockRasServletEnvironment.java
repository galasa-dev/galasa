/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.mocks;

import java.util.List;

import dev.galasa.framework.api.common.mocks.IServletUnderTest;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.ras.internal.RasServlet;
import dev.galasa.framework.mocks.MockFileSystem;
import dev.galasa.framework.mocks.MockResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;

public class MockRasServletEnvironment extends MockServletBaseEnvironment {

	public MockRasServletEnvironment(MockFramework mockFramework, List<IRunResult> mockInpResults, MockHttpServletRequest mockRequest) {
    	super(mockFramework, mockInpResults, mockRequest, new MockResultArchiveStoreDirectoryService(mockInpResults));
	}

	public MockRasServletEnvironment(List<IRunResult> mockInpResults, MockHttpServletRequest mockRequest) {
    	super(mockInpResults, mockRequest, new MockResultArchiveStoreDirectoryService(mockInpResults));
	}

	public MockRasServletEnvironment(List<IRunResult> mockInpResults, MockHttpServletRequest mockRequest, MockFileSystem mockFileSystem ) {
		super(mockInpResults, mockRequest, mockFileSystem);
	}

	public MockRasServletEnvironment(List<IRunResult> mockInpResults, MockHttpServletRequest mockRequest, MockResultArchiveStoreDirectoryService rasStore ) {
		super(mockInpResults, mockRequest, rasStore);
	}

	public RasServlet getServlet() {
		return super.getRasServlet();
	}

	@Override
	public IServletUnderTest createServlet() {
    	return new MockRasServlet();
	}
}
