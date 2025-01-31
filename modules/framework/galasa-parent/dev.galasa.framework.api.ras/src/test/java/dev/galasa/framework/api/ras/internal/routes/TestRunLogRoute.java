/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import dev.galasa.framework.api.ras.internal.RasServlet;
import dev.galasa.framework.api.ras.internal.RasServletTest;
import dev.galasa.framework.api.ras.internal.mocks.MockRasServletEnvironment;
import dev.galasa.framework.api.common.ServletErrorMessage;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.ras.internal.mocks.MockRunResult;
import dev.galasa.framework.mocks.MockPath;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class TestRunLogRoute extends RasServletTest {

	public List<IRunResult> generateTestData(String runId, String runName, String runLog) {
		List<IRunResult> mockInputRunResults = new ArrayList<IRunResult>();

		// Build the results the DB will return.
		String requestor = RandomStringUtils.insecure().nextAlphanumeric(8);

		TestStructure testStructure = new TestStructure();
		testStructure.setRunName(runName);
		testStructure.setRequestor(requestor);
		testStructure.setResult("Passed");

		Path artifactRoot = new MockPath("/" + runName + "/artifacts",null);
		IRunResult result = new MockRunResult(runId, testStructure, artifactRoot, runLog);
		mockInputRunResults.add(result);

		return mockInputRunResults;
	}


	/*
     * Regex Path
     */

	@Test
	public void testPathRegexExpectedLocalPathReturnsTrue(){
		//Given...
		String expectedPath = RunLogRoute.path;
		String inputPath = "/runs/lcl-abcd-1234.run/runlog";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void testPathRegexExpectedCouchDBPathReturnsTrue(){
		//Given...
		String expectedPath = RunLogRoute.path;
		String inputPath = "/runs/cdb-efgh-5678.run/runlog";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void testPathRegexLowerCasePathReturnsTrue(){
		//Given...
		String expectedPath = RunLogRoute.path;
		String inputPath = "/runs/cdbstoredrun/runlog";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}
	
	@Test
	public void testPathRegexExpectedPathWithCapitalLeadingLetterReturnsTrue(){
		//Given...
		String expectedPath = RunLogRoute.path;
		String inputPath = "/runs/ABC-DEFG-5678.run/runlog";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}
	
	@Test
	public void testPathRegexUpperCasePathReturnsFalse(){
		//Given...
		String expectedPath = RunLogRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/RUNLOG";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}
 
	@Test
	public void testPathRegexExpectedPathWithLeadingNumberReturnsFalse(){
		//Given...
		String expectedPath = RunLogRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/1runlog";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void testPathRegexExpectedPathWithTrailingForwardSlashReturnsTrue(){
		//Given...
		String expectedPath = RunLogRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/runlog/";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void testPathRegexNumberPathReturnsFalse(){
		//Given...
		String expectedPath = RunLogRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/runlog1";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void testPathRegexUnexpectedPathReturnsFalse(){
		//Given...
		String expectedPath = RunLogRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/runawaylog";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void testPathRegexEmptyPathReturnsFalse(){
		//Given...
		String expectedPath = RunLogRoute.path;
		String inputPath = "";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void testPathRegexSpecialCharacterPathReturnsFalse(){
		//Given...
		String expectedPath = RunLogRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/runlog/?";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void testPathRegexMultipleForwardSlashPathReturnsFalse(){
		//Given...
		String expectedPath = RunLogRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/runlog//////";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	} 

	/*
	 * GET Requests
	 */
	
	@Test
	public void testRunResultWithLogReturnsOK() throws Exception {
		//Given..
		String runId = "runA";
        String runLog = "hello world";
		List<IRunResult> mockRunResults = generateTestData(runId, "testName", runLog);
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(new HashMap<>(), "/runs/" + runId + "/runlog");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockRunResults, mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(runLog);
		assertThat(resp.getContentType()).isEqualTo("text/plain");
	}

	@Test
	public void testRunResultWithLogAcceptHeaderReturnsOK() throws Exception {
		//Given..
		String runId = "runA";
        String runLog = "hello world";
		List<IRunResult> mockRunResults = generateTestData(runId, "testName", runLog);
		Map<String, String> headerMap = new HashMap<String,String>();
        headerMap.put("Accept", "text/plain");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(new HashMap<>(), "/runs/" + runId + "/runlog", headerMap);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockRunResults, mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(runLog);
		assertThat(resp.getContentType()).isEqualTo("text/plain");
	}

	@Test
	public void testRunResultWithLogAcceptHeaderMultipleReturnsOK() throws Exception {
		//Given..
		String runId = "runA";
        String runLog = "hello world";
		List<IRunResult> mockRunResults = generateTestData(runId, "testName", runLog);
		Map<String, String> headerMap = new HashMap<String,String>();
        headerMap.put("Accept", "text/plain");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(new HashMap<>(), "/runs/" + runId + "/runlog", headerMap);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockRunResults, mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(runLog);
		assertThat(resp.getContentType()).isEqualTo("text/plain");
	}

	@Test
	public void testRunResultWithEmptyLogReturnsEmptyLogOK() throws Exception {
		//Given..
		String runId = "runA";
		List<IRunResult> mockRunResults = generateTestData(runId, "testName", "");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(new HashMap<>(), "/runs/" + runId + "/runlog");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockRunResults, mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEmpty();
		assertThat(resp.getContentType()).isEqualTo("text/plain");
	}

	@Test
	public void testRunResultWithNullLogReturnsNotFoundError() throws Exception {
		//Given..
		String runId = "runA";
		List<IRunResult> mockRunResults = generateTestData(runId, "testName", null);
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(new HashMap<>(), "/runs/" + runId + "/runlog");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockRunResults, mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// {
		//   "error_code" : 5002,
		//   "error_message" : "GAL5002E: Error retrieving ras run from identifier 'runA'.""
		// }
		assertThat(resp.getStatus()).isEqualTo(404);
		checkErrorStructure(outStream.toString() , 5002 , "GAL5002E", "runA");
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testBadRunIdGivesNotFoundError() throws Exception {
		//Given..
		String runId = "badRunId";
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/runlog");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(null, mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// {
		//   "error_code" : 5002,
		//   "error_message" : "GAL5002E: Error retrieving ras run from identifier 'badRunId'.""
		// }
		assertThat(resp.getStatus()).isEqualTo(404);
		checkErrorStructure(outStream.toString() , ServletErrorMessage.GAL5091_ERROR_RUN_NOT_FOUND_BY_ID.getTemplateNumber() , "GAL5091E", "badRunId" );
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}
}