/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal;

import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.mocks.MockFileSystem;
import dev.galasa.framework.mocks.MockPath;
import dev.galasa.framework.mocks.MockRunResult;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class RasServletTest extends BaseServletTest {

	protected MockFileSystem mockFileSystem;

	protected List<IRunResult> generateTestData(String runId, TestStructure testStructure, String runLog) {
		List<IRunResult> mockInputRunResults = new ArrayList<IRunResult>();

		String runName = testStructure.getRunName();
		Path artifactRoot = new MockPath("/" + runName, this.mockFileSystem);
		IRunResult result = new MockRunResult( runId, testStructure, artifactRoot, runLog);
		mockInputRunResults.add(result);

		return mockInputRunResults;
	}

	protected List<IRunResult> generateTestData(String runId, String runName, String runLog) {
		return generateTestData(runId, runName, runLog, "galasa");
	}

	protected List<IRunResult> generateTestData(String runId, String runName, String runLog, String requestor) {
		TestStructure testStructure = new TestStructure();
		testStructure.setRunName(runName);
		testStructure.setRequestor(requestor);
		testStructure.setResult("Passed");

		return generateTestData(runId, testStructure, runLog);
	}

	protected List<IRunResult> generateTestData(String runId, String runName, String runLog, String requestor, String groupId) {
		TestStructure testStructure = new TestStructure();
		testStructure.setRunName(runName);
		testStructure.setRequestor(requestor);
		testStructure.setGroup(groupId);
		testStructure.setResult("Passed");

		return generateTestData(runId, testStructure, runLog);
	}
}