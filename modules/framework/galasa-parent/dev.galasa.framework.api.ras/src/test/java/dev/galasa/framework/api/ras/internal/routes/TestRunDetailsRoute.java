/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import org.junit.Test;

import dev.galasa.framework.api.ras.internal.RasServlet;
import dev.galasa.framework.api.ras.internal.RasServletTest;
import dev.galasa.framework.api.ras.internal.mocks.MockArchiveStore;
import dev.galasa.framework.api.ras.internal.mocks.MockRasServletEnvironment;
import dev.galasa.framework.api.ras.internal.mocks.MockResultArchiveStoreDirectoryService;
import dev.galasa.framework.api.ras.internal.mocks.MockRunResult;
import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.api.ras.RasRunResult;
import dev.galasa.api.ras.RasTestStructure;
import dev.galasa.framework.api.common.HttpMethod;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockIFrameworkRuns;
import dev.galasa.framework.api.common.mocks.MockIRun;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.rbac.Action;

import static org.assertj.core.api.Assertions.*;
import static dev.galasa.framework.spi.rbac.BuiltInAction.*;

import java.util.*;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class TestRunDetailsRoute extends RasServletTest {

    public String generateExpectedJson (String runId, String runName ){

		RasTestStructure testStructure = new RasTestStructure(runName, null, null, null, "galasa", null, "Passed", null, null, null, Collections.emptyList(), "none");
		RasRunResult rasRunResult = new RasRunResult(runId, Collections.emptyList(), testStructure);

		testStructure.setRunName(runName);
		testStructure.setRequestor("galasa");
		testStructure.setResult("Passed");
		testStructure.setGroup("none");
		testStructure.setMethods(Collections.emptyList());

		rasRunResult.setRunId(runId);
		rasRunResult.setArtifacts(Collections.emptyList());
		rasRunResult.setTestStructure(testStructure);

		return gson.toJson(rasRunResult);
    }

	public String generateStatusUpdateJson(String status, String result) {
		return
		"{\n" +
	    "  \"status\": \"" +  status + "\",\n" +
		"  \"result\": \"" + result + "\"\n" +
		"}";
	}

	/*
     * Regex Path
     */

	@Test
	public void testPathRegexExpectedLocalPathReturnsTrue(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/runs/lcl-abcd-1234.run/";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void testPathRegexExpectedCouchDBPathReturnsTrue(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/runs/cdb-efgh-5678.run/";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void testPathRegexExpectedNoTrailingForwardSlashReturnsTrue(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/runs/cdb-efgh-5678.run";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void testPathRegexLowerCasePathReturnsTrue(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/runs/cdbstoredrun/";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}
	
	@Test
	public void testPathRegexExpectedPathWithCapitalLeadingLetterReturnsTrue(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/runs/ABC-DEFG-5678.run/";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}
	
	@Test
	public void testPathRegexUpperCasePathReturnsFalse(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/RUNS/cdb-EFGH-5678.run/";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}
 
	@Test
	public void testPathRegexExpectedPathWithLeadingNumberReturnsFalse(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/1runs/cdb-EFGH-5678.run/";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}
 
	@Test
	public void testPathRegexExpectedPathWithTrailingForwardSlashReturnsFalse(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run//";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}
 
	@Test
	public void testPathRegexNumberPathReturnsFalse(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/1";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void testPathRegexUnexpectedPathReturnsFalse(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/randompath";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void testPathRegexEmptyPathReturnsFalse(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void testPathRegexSpecialCharacterPathReturnsFalse(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/?";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void testPathRegexMultipleForwardSlashPathReturnsFalse(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run///////";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	} 


	@Test
	public void testRegexWithMaliciousHtmlInsideFailsRegexMatching(){
		//Given...
		String expectedPath = RunDetailsRoute.path;
		String inputPath = "/runs/ABC-<href>/";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).as("malicious regex containing html was not treated as invalid input.").isFalse();
	}

	/*
	 * GET Requests
	 */

    @Test
    public void testGoodRunIdReturnsOK() throws Exception {
		//Given..
		String runId = "xx12345xx";
        String runName = "U123";

		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

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
		String expectedJson = generateExpectedJson(runId, runName);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("application/json");
	}

	@Test
    public void testGoodRunIdAcceptHeaderReturnsOK() throws Exception {
		//Given..
		String runId = "xx12345xx";
        String runName = "U123";

		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		Map<String, String> headerMap = new HashMap<String,String>();
        headerMap.put("Accept", "application/json");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId, headerMap);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		String expectedJson = generateExpectedJson(runId, runName);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo(expectedJson);
		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

    @Test
    public void testBadRunIdReturnsError() throws Exception {
		//Given..
		String runId = "badRunId";

		List<IRunResult> mockInputRunResults = generateTestData("OtherRunId", "R123", null);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

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
        assertThat(resp.getStatus()).isEqualTo(404);
        checkErrorStructure(outStream.toString() , 5002 , "GAL5002E", runId );
        assertThat( resp.getContentType()).isEqualTo("application/json");
	}

    @Test
    public void testNoRunReturnsError() throws Exception {
		//Given..
		String runId = "badRunId";

		List<IRunResult> mockInputRunResults = generateTestData(null, null, null);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

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
        assertThat(resp.getStatus()).isEqualTo(500);
        checkErrorStructure(outStream.toString() , 5000 , "GAL5000E" );
        assertThat( resp.getContentType()).isEqualTo("application/json");
	}

	/*
	 * PUT Requests
	 */

	@Test
	public void testRequestToResetRunReturnsOK() throws Exception {
		// Given...
		String runId = "xx12345xx";
		String runName = "U123";

		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);

		String content = generateStatusUpdateJson("queued", "");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest("/runs/" + runId, content, "PUT");
		
		List<IRun> runs = new ArrayList<IRun>();
		runs.add(new MockIRun(runName, "type1", "requestor1", "test1", "BUILDING", "bundle1", "testClass1", "group1"));
		IFrameworkRuns frameworkRuns = new MockIFrameworkRuns(runs);
		MockResultArchiveStoreDirectoryService mockrasService = new MockResultArchiveStoreDirectoryService(mockInputRunResults);
		List<IResultArchiveStoreDirectoryService> directoryServices = new ArrayList<IResultArchiveStoreDirectoryService>();
		directoryServices.add(mockrasService);
		MockFramework mockFramework = new MockFramework(new MockArchiveStore(directoryServices), frameworkRuns);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockFramework, mockInputRunResults, mockRequest);

		RasServlet servlet = mockServletEnvironment.getRasServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doPut(req, resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(202);
		assertThat(outStream.toString()).isEqualTo("The request to reset run " + runName + " has been received.");
	}

	@Test
	public void testRequestToResetRunAcceptHeaderReturnsOK() throws Exception {
		// Given...
		String runId = "xx12345xx";
		String runName = "U123";

		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);

		String content = generateStatusUpdateJson("queued", "");
		Map<String, String> headerMap = new HashMap<String,String>();
        headerMap.put("Accept", "*/*");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest("/runs/" + runId, content, "PUT",headerMap);
		
		List<IRun> runs = new ArrayList<IRun>();
		runs.add(new MockIRun(runName, "type1", "requestor1", "test1", "BUILDING", "bundle1", "testClass1", "group1"));
		IFrameworkRuns frameworkRuns = new MockIFrameworkRuns(runs);
		MockResultArchiveStoreDirectoryService mockrasService = new MockResultArchiveStoreDirectoryService(mockInputRunResults);
		List<IResultArchiveStoreDirectoryService> directoryServices = new ArrayList<IResultArchiveStoreDirectoryService>();
		directoryServices.add(mockrasService);
		MockFramework mockFramework = new MockFramework(new MockArchiveStore(directoryServices), frameworkRuns);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockFramework, mockInputRunResults, mockRequest);

		RasServlet servlet = mockServletEnvironment.getRasServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doPut(req, resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(202);
		assertThat(outStream.toString()).isEqualTo("The request to reset run " + runName + " has been received.");
	}

	@Test
	public void testRequestToCancelRunReturnsOK() throws Exception {
		// Given...
		String runId = "xx12345xx";
		String runName = "U123";

		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);

		String content = generateStatusUpdateJson("finished", "cancelled");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest("/runs/" + runId, content, "PUT");
		
		List<IRun> runs = new ArrayList<IRun>();
		runs.add(new MockIRun(runName, "type1", "requestor1", "test1", "BUILDING", "bundle1", "testClass1", "group1"));
		IFrameworkRuns frameworkRuns = new MockIFrameworkRuns(runs);
		MockResultArchiveStoreDirectoryService mockrasService = new MockResultArchiveStoreDirectoryService(mockInputRunResults);
		List<IResultArchiveStoreDirectoryService> directoryServices = new ArrayList<IResultArchiveStoreDirectoryService>();
		directoryServices.add(mockrasService);
		MockFramework mockFramework = new MockFramework(new MockArchiveStore(directoryServices), frameworkRuns);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockFramework, mockInputRunResults, mockRequest);

		RasServlet servlet = mockServletEnvironment.getRasServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doPut(req, resp);

		// IRunResult run = mockrasService.getRunById(runId);
		// String result = run.getTestStructure().getResult();
		// String status = run.getTestStructure().getStatus();

		// Then...
		assertThat(resp.getStatus()).isEqualTo(202);
		assertThat(outStream.toString()).isEqualTo("The request to cancel run " + runName + " has been received.");

		// assertThat(result).isEqualTo("Cancelled");
		// assertThat(status).isEqualTo("finished");
	}

	@Test
	public void testRequestToCancelRunCapitalCaseValuesReturnsOK() throws Exception {
		// Given...
		String runId = "xx12345xx";
		String runName = "U123";

		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);

		String content = generateStatusUpdateJson("FINISHED", "CANCELLED");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest("/runs/" + runId, content, "PUT");
		
		List<IRun> runs = new ArrayList<IRun>();
		runs.add(new MockIRun(runName, "type1", "requestor1", "test1", "BUILDING", "bundle1", "testClass1", "group1"));
		IFrameworkRuns frameworkRuns = new MockIFrameworkRuns(runs);
		MockResultArchiveStoreDirectoryService mockrasService = new MockResultArchiveStoreDirectoryService(mockInputRunResults);
		List<IResultArchiveStoreDirectoryService> directoryServices = new ArrayList<IResultArchiveStoreDirectoryService>();
		directoryServices.add(mockrasService);
		MockFramework mockFramework = new MockFramework(new MockArchiveStore(directoryServices), frameworkRuns);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockFramework, mockInputRunResults, mockRequest);

		RasServlet servlet = mockServletEnvironment.getRasServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doPut(req, resp);

		// IRunResult run = mockrasService.getRunById(runId);
		// String result = run.getTestStructure().getResult();
		// String status = run.getTestStructure().getStatus();

		// Then...
		assertThat(resp.getStatus()).isEqualTo(202);
		assertThat(outStream.toString()).isEqualTo("The request to cancel run " + runName + " has been received.");

		// assertThat(result).isEqualTo("Cancelled");
		// assertThat(status).isEqualTo("finished");
	}

	@Test
	public void testRequestToUpdateRunStatusWithInvalidStatusReturnsError() throws Exception {
		// Given...
		String runId = "xx12345xx";
		String runName = "U123";

		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);

		String content = generateStatusUpdateJson("submitted", "");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest("/runs/" + runId, content, "PUT");
		
		List<IRun> runs = new ArrayList<IRun>();
		runs.add(new MockIRun(runName, "type1", "requestor1", "test1", "BUILDING", "bundle1", "testClass1", "group1"));
		IFrameworkRuns frameworkRuns = new MockIFrameworkRuns(runs);
		MockResultArchiveStoreDirectoryService mockrasService = new MockResultArchiveStoreDirectoryService(mockInputRunResults);
		List<IResultArchiveStoreDirectoryService> directoryServices = new ArrayList<IResultArchiveStoreDirectoryService>();
		directoryServices.add(mockrasService);
		MockFramework mockFramework = new MockFramework(new MockArchiveStore(directoryServices), frameworkRuns);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockFramework, mockInputRunResults, mockRequest);

		RasServlet servlet = mockServletEnvironment.getRasServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doPut(req, resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(400);
		checkErrorStructure(outStream.toString(), 
			5045, 
			"E: Error occurred. The field 'status' in the request body is invalid. The 'status' value 'submitted' supplied is not supported. Supported values are: 'queued' and 'finished'.");
	}
	
	@Test
	public void testRequestToResetRunFailsReturnsError() throws Exception {
		// Given...
		String runId = "xx12345xx";
		String runName = "U123";

		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);

		String content = generateStatusUpdateJson("queued", "");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest("/runs/" + runId, content, "PUT");
		
		List<IRun> runs = new ArrayList<IRun>();
		runs.add(new MockIRun(runName, "type1", "requestor1", "test1", "BUILDING", "bundle1", "testClass1", "group1"));
		IFrameworkRuns frameworkRuns = new MockIFrameworkRuns(runs) {
			@Override
    		public boolean reset(String runname) throws DynamicStatusStoreException {
        		throw new DynamicStatusStoreException();
			}
		};
		MockResultArchiveStoreDirectoryService mockrasService = new MockResultArchiveStoreDirectoryService(mockInputRunResults);
		List<IResultArchiveStoreDirectoryService> directoryServices = new ArrayList<IResultArchiveStoreDirectoryService>();
		directoryServices.add(mockrasService);
		MockFramework mockFramework = new MockFramework(new MockArchiveStore(directoryServices), frameworkRuns);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockFramework, mockInputRunResults, mockRequest);

		RasServlet servlet = mockServletEnvironment.getRasServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doPut(req, resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(500);
		checkErrorStructure(outStream.toString(), 
			5047, 
			"E: Error occurred when trying to reset the run 'U123'. Report the problem to your Galasa Ecosystem owner.");
	}

	@Test
	public void testRequestToCancelRunFailsReturnsError() throws Exception {
		// Given...
		String runId = "xx12345xx";
		String runName = "U123";

		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);

		String content = generateStatusUpdateJson("finished", "cancelled");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest("/runs/" + runId, content, "PUT");
		
		List<IRun> runs = new ArrayList<IRun>();
		runs.add(new MockIRun(runName, "type1", "requestor1", "test1", "BUILDING", "bundle1", "testClass1", "group1"));
		IFrameworkRuns frameworkRuns = new MockIFrameworkRuns(runs) {
			@Override
			public boolean delete(String runname) throws DynamicStatusStoreException {
        		throw new DynamicStatusStoreException();
			}
		};
		MockResultArchiveStoreDirectoryService mockrasService = new MockResultArchiveStoreDirectoryService(mockInputRunResults);
		List<IResultArchiveStoreDirectoryService> directoryServices = new ArrayList<IResultArchiveStoreDirectoryService>();
		directoryServices.add(mockrasService);
		MockFramework mockFramework = new MockFramework(new MockArchiveStore(directoryServices), frameworkRuns);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockFramework, mockInputRunResults, mockRequest);

		RasServlet servlet = mockServletEnvironment.getRasServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doPut(req, resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(500);
		checkErrorStructure(outStream.toString(), 
			5048, 
			"E: Error occurred when trying to cancel the run 'U123'. Report the problem to your Galasa Ecosystem owner.");
	}
	
	@Test
	public void testRequestToResetRunNoLongerProcessingReturnsError() throws Exception {
		// Given...
		String runId = "xx12345xx";
		String runName = "U123";

		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);

		String content = generateStatusUpdateJson("queued", runName);
		MockHttpServletRequest mockRequest = new MockHttpServletRequest("/runs/" + runId, content, "PUT");
		
		List<IRun> runs = new ArrayList<IRun>();
		runs.add(new MockIRun(runName, "type1", "requestor1", "test1", "BUILDING", "bundle1", "testClass1", "group1"));
		IFrameworkRuns frameworkRuns = new MockIFrameworkRuns(runs) {
			@Override
    		public boolean reset(String runname) throws DynamicStatusStoreException {
        		return false;
			}
		};
		MockResultArchiveStoreDirectoryService mockrasService = new MockResultArchiveStoreDirectoryService(mockInputRunResults);
		List<IResultArchiveStoreDirectoryService> directoryServices = new ArrayList<IResultArchiveStoreDirectoryService>();
		directoryServices.add(mockrasService);
		MockFramework mockFramework = new MockFramework(new MockArchiveStore(directoryServices), frameworkRuns);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockFramework, mockInputRunResults, mockRequest);

		RasServlet servlet = mockServletEnvironment.getRasServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doPut(req, resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(400);
		checkErrorStructure(outStream.toString(), 
			5049, 
			"E: Error occurred when trying to reset the run 'U123'. The run has already completed.");
	}

	@Test
	public void testRequestToCancelRunNoLongerProcessingReturnsError() throws Exception {
		// Given...
		String runId = "xx12345xx";
		String runName = "U123";

		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);

		String content = generateStatusUpdateJson("finished", "cancelled");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest("/runs/" + runId, content, "PUT");
		
		List<IRun> runs = new ArrayList<IRun>();
		runs.add(new MockIRun(runName, "type1", "requestor1", "test1", "BUILDING", "bundle1", "testClass1", "group1"));
		IFrameworkRuns frameworkRuns = new MockIFrameworkRuns(runs) {
			@Override
			public boolean delete(String runname) throws DynamicStatusStoreException {
        		return false;
			}
		};
		MockResultArchiveStoreDirectoryService mockrasService = new MockResultArchiveStoreDirectoryService(mockInputRunResults);
		List<IResultArchiveStoreDirectoryService> directoryServices = new ArrayList<IResultArchiveStoreDirectoryService>();
		directoryServices.add(mockrasService);
		MockFramework mockFramework = new MockFramework(new MockArchiveStore(directoryServices), frameworkRuns);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockFramework, mockInputRunResults, mockRequest);

		RasServlet servlet = mockServletEnvironment.getRasServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doPut(req, resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(400);
		checkErrorStructure(outStream.toString(), 
			5050, 
			"E: Error occurred when trying to cancel the run 'U123'. The run has already completed.");
	}

	@Test
	public void testRequestToCancelRunBadResultReturnsError() throws Exception {
		// Given...
		String runId = "xx12345xx";
		String runName = "U123";

		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);

		String content = generateStatusUpdateJson("finished", "deleted");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest("/runs/" + runId, content, "PUT");
		
		List<IRun> runs = new ArrayList<IRun>();
		runs.add(new MockIRun(runName, "type1", "requestor1", "test1", "BUILDING", "bundle1", "testClass1", "group1"));
		IFrameworkRuns frameworkRuns = new MockIFrameworkRuns(runs);
		MockResultArchiveStoreDirectoryService mockrasService = new MockResultArchiveStoreDirectoryService(mockInputRunResults);
		List<IResultArchiveStoreDirectoryService> directoryServices = new ArrayList<IResultArchiveStoreDirectoryService>();
		directoryServices.add(mockrasService);
		MockFramework mockFramework = new MockFramework(new MockArchiveStore(directoryServices), frameworkRuns);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockFramework, mockInputRunResults, mockRequest);

		RasServlet servlet = mockServletEnvironment.getRasServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doPut(req, resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(400);
		checkErrorStructure(outStream.toString(), 
			5046, 
			"E: Error occurred when trying to cancel the run 'U123'. The 'result' 'deleted' supplied is not supported. Supported values are: 'cancelled'.");
	}


	//
	// DELETE Requests
	//

	@Test
	public void testDeleteRunNoReqPayloadWithGoodRunIdReturnsOK() throws Exception {
		// Given...
		String runId = "xx12345xx";
		String runName = "U123";

		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);
		MockRasServletEnvironment mockServletEnvironment = setUpDeleteByRunIdMockServices( runId , mockInputRunResults);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();

		// When...
		servlet.init();
		servlet.doDelete(req,resp);

		// Then...
        MockRunResult deletedRun = (MockRunResult) mockInputRunResults.get(0);
		assertThat(resp.getStatus()).isEqualTo(204);
		assertThat(deletedRun.isLoadingArtifactsEnabled()).as("The fake run result's artifacts should not have been loaded.").isFalse();
		assertThat(deletedRun.isDiscarded()).as("The fake run result has not been discarded.").isTrue();
	}

	@Test
	public void testDeleteRunWithMissingRunsDeleteOtherUsersPermissionReturnsForbidden() throws Exception {
		// Given...
		String runId = "xx12345xx";
		String runName = "U123";
		String existingRunRequestor = "another-user";

		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null, existingRunRequestor);
		MockResultArchiveStoreDirectoryService mockRasService = new MockResultArchiveStoreDirectoryService(mockInputRunResults);
		List<IResultArchiveStoreDirectoryService> directoryServices = new ArrayList<IResultArchiveStoreDirectoryService>();
		directoryServices.add(mockRasService);

        List<Action> permittedActions = List.of(GENERAL_API_ACCESS.getAction());
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME, permittedActions);

		MockFramework mockFramework = new MockFramework(new MockArchiveStore(directoryServices));
		mockFramework.setRBACService(mockRbacService);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest("/runs/" + runId, null, HttpMethod.DELETE.toString());
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockFramework, mockInputRunResults, mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		// When...
		servlet.init();
		servlet.doDelete(req,resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(403);
		checkErrorStructure(outStream.toString(), 5125, "GAL5125E", "RUNS_DELETE_OTHER_USERS");
	}


	@Test
	public void testDeleteRunFailsWhenRunDoesNotExist() throws Exception {
		// Given...
		String runId = "xx12345xx";

		List<IRunResult> mockInputRunResults = new ArrayList<>(); // Note: There are no runs returned when the servlet looks for matching runs.
		MockRasServletEnvironment mockServletEnvironment = setUpDeleteByRunIdMockServices( runId , mockInputRunResults);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();

		// When...
		servlet.init();
		servlet.doDelete(req,resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
		checkErrorStructure(resp.getOutputStream().toString(), 
		5091,
		"GAL5091E",runId);
	}

	private MockRasServletEnvironment setUpDeleteByRunIdMockServices(String runId, List<IRunResult> mockInputRunResults) {
	
		MockResultArchiveStoreDirectoryService mockrasService = new MockResultArchiveStoreDirectoryService(mockInputRunResults);
		List<IResultArchiveStoreDirectoryService> directoryServices = new ArrayList<IResultArchiveStoreDirectoryService>();
		directoryServices.add(mockrasService);

		MockHttpServletRequest mockRequest = new MockHttpServletRequest("/runs/" + runId, null, HttpMethod.DELETE.toString());
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockrasService);

		return mockServletEnvironment;
	}

	@Test
	public void testDeleteRunFailsWhenRunIdIsNotFormedCorrectly() throws Exception {
		// Given...
		String runId = "cdb-<href>";
		String runName = "U123";

		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);
		
		MockRasServletEnvironment mockServletEnvironment = setUpDeleteByRunIdMockServices( runId , mockInputRunResults);
	
		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();

		// When...
		servlet.init();
		servlet.doDelete(req,resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
		checkErrorStructure(resp.getOutputStream().toString(), 
		5404,
		"GAL5404E");
	}
}