/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.routes;

import static dev.galasa.framework.spi.rbac.BuiltInAction.*;
import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.HttpMethod;
import dev.galasa.framework.api.common.MimeType;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.FilledMockEnvironment;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockIFrameworkRuns;
import dev.galasa.framework.api.common.mocks.MockIRun;
import dev.galasa.framework.api.runs.mocks.MockRunsServlet;
import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockIResultArchiveStore;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.rbac.Action;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class TestGroupRunsRoute extends BaseServletTest {

    private static final Map<String, String> REQUIRED_HEADERS = new HashMap<>(Map.of("Authorization", "Bearer " + DUMMY_JWT));

    public static final String NON_ADMIN_JWT_USERNAME = "joeTester";

    private String generateStatusUpdateJson(String result) {
		JsonObject statusUpdateJson = new JsonObject();
        statusUpdateJson.addProperty("result", result);

        return gson.toJson(statusUpdateJson);
	}

	private String generateExpectedJson(List<IRun> runs, boolean complete) {

        JsonObject expectedJsonObj = new JsonObject();

        expectedJsonObj.addProperty("complete", complete);

        JsonArray runsJsonArray = new JsonArray();

        for (IRun run : runs) {
            JsonObject runJson = new JsonObject();
            runJson.addProperty("name", run.getName());
            runJson.addProperty("heartbeat", "2023-10-12T12:16:49.832925Z");
            runJson.addProperty("type", run.getType());
            runJson.addProperty("group", run.getGroup());
            runJson.addProperty("submissionId", run.getSubmissionId());
            runJson.addProperty("test", run.getTestClassName());
            runJson.addProperty("bundleName", run.getTestBundleName());
            runJson.addProperty("testName", run.getTest());

            if (!run.getStatus().equals("submitted")) {
                runJson.addProperty("status", run.getStatus());
            }

            runJson.addProperty("result", "Passed");
            runJson.addProperty("queued", "2023-10-12T12:16:49.832925Z");
            runJson.addProperty("finished", "2023-10-12T12:16:49.832925Z");
            runJson.addProperty("waitUntil", "2023-10-12T12:16:49.832925Z");
            runJson.addProperty("requestor", run.getRequestor());
            runJson.addProperty("user", run.getUser());
            runJson.addProperty("isLocal", false);
            runJson.addProperty("isTraceEnabled", false);
            runJson.addProperty("rasRunId", "cdb-" + run.getName());

            JsonArray tagsArray = new JsonArray();
            for( String tag : run.getTags() ) {
                tagsArray.add(tag);
            }
            runJson.add("tags", tagsArray);

            String rasRunId = run.getRasRunId();
            runJson.addProperty("webUiUrl", "http://my-api.server/test-runs/" + rasRunId );
            runJson.addProperty("restApiUrl", "http://my-api.server/api/ras/runs/" + rasRunId );

            runsJsonArray.add(runJson);
        }

        expectedJsonObj.add("runs", runsJsonArray);

        String expectedJson = gson.toJson(expectedJsonObj);
        return expectedJson;
    }


    private String generatePayload(
        String[] classNames,
        String requestorType,
        String requestor,
        String user,
        String testStream,
        String groupName,
        String overrideExpectedRequestor,
        String submissionId,
        Set<String>tags
    ) {
        if (overrideExpectedRequestor != null) {
            requestor = overrideExpectedRequestor;
        }

        JsonArray classNamesArray = new JsonArray();
        for (String className : classNames) {
            classNamesArray.add(className);
        }

        JsonObject payloadJson = new JsonObject();
        payloadJson.add("classNames", classNamesArray);
        payloadJson.addProperty("requestorType", requestorType);
        payloadJson.addProperty("requestor", requestor);
        payloadJson.addProperty("user", user);

        payloadJson.addProperty("testStream", testStream);

        payloadJson.addProperty("obr", "this.obr");
        payloadJson.addProperty("mavenRepository", "this.maven.repo");
        payloadJson.addProperty("sharedEnvironmentRunTime", "envRunTime");
        payloadJson.add("overrides", new JsonObject());
        payloadJson.addProperty("trace", true);

        return gson.toJson(payloadJson);
    }

    /*
     * Regex Path
     */

    @Test
    public void testPathRegexExpectedPathReturnsTrue(){
        //Given...
        String expectedPath = GroupRunsRoute.path;
        String inputPath = "/correct-ID_1234";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isTrue();
    }

    @Test
    public void testPathRegexLowerCasePathReturnsTrue(){
        //Given...
        String expectedPath = GroupRunsRoute.path;
        String inputPath = "/thisisavalidpath";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isTrue();
    }

    @Test
    public void testPathRegexUpperCasePathReturnsTrue(){
        //Given...
        String expectedPath = GroupRunsRoute.path;
        String inputPath = "/ALLCAPITALS";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isTrue();
    }

    @Test
    public void testPathRegexNumberPathReturnsTrue(){
        //Given...
        String expectedPath = GroupRunsRoute.path;
        String inputPath = "/1234";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isTrue();
    }

    @Test
    public void testPathRegexUnexpectedPathReturnsTrue(){
        //Given...
        String expectedPath = GroupRunsRoute.path;
        String inputPath = "/incorrect-?ID_1234";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void testPathRegexEmptyPathReturnsFalse(){
        //Given...
        String expectedPath = GroupRunsRoute.path;
        String inputPath = "";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void testPathRegexDotPathReturnsFalse(){
        //Given...
        String expectedPath = GroupRunsRoute.path;
        String inputPath = "/random.String";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void testPathRegexSpecialCharacterPathReturnsFalse(){
        //Given...
        String expectedPath = GroupRunsRoute.path;
        String inputPath = "/?";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void testPathRegexMultipleForwardSlashPathReturnsFalse(){
        //Given...
        String expectedPath = GroupRunsRoute.path;
        String inputPath = "//////";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    /*
     * GET Requests
     */

    @Test
    public void testGetRunsWithInvalidGroupNameReturnsError() throws Exception {
        // Given...
        // /runs/empty is an empty runs set and should return an error as runs can not be null
		String groupName = "invalid";

        List<IRun> runs = new ArrayList<IRun>();
        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(500);

		checkErrorStructure(
			outStream.toString(),
			5019, "E: Unable to retrieve runs for Run Group: 'invalid'."
		);
    }

    @Test
    public void testGetRunsWithValidGroupNameWithNullRunsReturnsError() throws Exception {
        // Given...
        // /runs/empty is an empty runs set and should return an error as runs can not be null
		String groupName = "nullgroup";

        List<IRun> runs = new ArrayList<IRun>();
        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(500);

		checkErrorStructure(
			outStream.toString(),
			5019, "E: Unable to retrieve runs for Run Group: '/nullgroup'."
		);
    }

    @Test
    public void testGetRunsWithEmptyGroupNameReturnsOK() throws Exception {
        // Given...
        // /runs/empty is an empty runs set and should return an error as runs can not be null
		String groupName = "empty";

        List<IRun> runs = new ArrayList<IRun>();
        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(outStream.toString()).isEqualTo("{\n  \"complete\": true,\n  \"runs\": []\n}");
    }

    @Test
    public void testGetRunsWithValidGroupNameReturnsOk() throws Exception {
        // Given...
		String groupName = "framework";
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();
        List<IRun> runs = new ArrayList<IRun>();
        runs.add(new MockIRun("name1", "type1", "requestor1", "test1", "FINISHED","bundle1", "testClass1", groupName, submissionId,tags));

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, true);
        assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    @Test
    public void testGetRunsWithValidGroupNameReturnsMultiple() throws Exception {
        // Given...
		String groupName = "framework";
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();
        List<IRun> runs = new ArrayList<IRun>();
        runs.add(new MockIRun("name1", "type1", "requestor1", "test1", "BUILDING","bundle1", "testClass1", groupName, submissionId,tags));
        runs.add(new MockIRun("name2", "type2", "requestor2", "test2", "BUILDING","bundle2", "testClass2", groupName, submissionId,tags));

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, false);
        assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

     @Test
    public void testGetRunsWithValidGroupNameMultipleWithFinishedRunReturnsCompleteFalse() throws Exception {
        // Given...
		String groupName = "framework";
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();
        List<IRun> runs = new ArrayList<IRun>();
        runs.add(new MockIRun("name1", "type1", "requestor1", "test1", "BUILDING","bundle1", "testClass1", groupName, submissionId,tags));
        runs.add(new MockIRun("name2", "type2", "requestor2", "test2", "BUILDING","bundle2", "testClass2", groupName, submissionId,tags));
        runs.add(new MockIRun("name3", "type3", "requestor3", "test3", "FINISHED","bundle3", "testClass3", groupName, submissionId,tags));
        runs.add(new MockIRun("name4", "type4", "requestor4", "test4", "UP","bundle4", "testClass4", groupName, submissionId,tags));
        runs.add(new MockIRun("name5", "type6", "requestor5", "test5", "DISCARDED","bundle5", "testClass6", groupName, submissionId,tags));
        runs.add(new MockIRun("name6", "type6", "requestor6", "test6", "BUILDING","bundle6", "testClass6", groupName, submissionId,tags));
        runs.add(new MockIRun("name7", "type7", "requestor7", "test7", "BUILDING","bundle7", "testClass7", groupName, submissionId,tags));
        runs.add(new MockIRun("name8", "type8", "requestor8", "test8", "BUILDING","bundle8", "testClass8", groupName, submissionId,tags));
        runs.add(new MockIRun("name9", "type9", "requestor9", "test9", "BUILDING","bundle9", "testClass9", groupName, submissionId,tags));
        runs.add(new MockIRun("name10", "type10", "requestor10", "test10", "BUILDING","bundle10", "testClass10", groupName, submissionId,tags));

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, false);
        assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    @Test
    public void testGetRunsWithUUIDGroupNameMultipleWithFinishedRunReturnsCompleteFalse() throws Exception {
        // Given...
		String groupName = "8149dc91-dabc-461a-b9e8-6f11a4455f59";
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();
        List<IRun> runs = new ArrayList<IRun>();
        runs.add(new MockIRun("name1", "type1", "requestor1", "test1", "BUILDING","bundle1", "testClass1", groupName, submissionId,tags));
        runs.add(new MockIRun("name2", "type2", "requestor2", "test2", "BUILDING","bundle2", "testClass2", groupName, submissionId,tags));
        runs.add(new MockIRun("name3", "type3", "requestor3", "test3", "FINISHED","bundle3", "testClass3", groupName, submissionId,tags));
        runs.add(new MockIRun("name4", "type4", "requestor4", "test4", "UP","bundle4", "testClass4", groupName, submissionId,tags));
        runs.add(new MockIRun("name5", "type6", "requestor5", "test5", "DISCARDED","bundle5", "testClass6", groupName, submissionId,tags));
        runs.add(new MockIRun("name6", "type6", "requestor6", "test6", "BUILDING","bundle6", "testClass6", groupName, submissionId,tags));
        runs.add(new MockIRun("name7", "type7", "requestor7", "test7", "BUILDING","bundle7", "testClass7", groupName, submissionId,tags));
        runs.add(new MockIRun("name8", "type8", "requestor8", "test8", "BUILDING","bundle8", "testClass8", groupName, submissionId,tags));
        runs.add(new MockIRun("name9", "type9", "requestor9", "test9", "BUILDING","bundle9", "testClass9", groupName, submissionId,tags));
        runs.add(new MockIRun("name10", "type10", "requestor10", "test10", "BUILDING","bundle10", "testClass10", groupName, submissionId,tags));

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, false);
        assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    /*
     * POST requests
     */

    @Test
    public void testPostRunsNoFrameworkReturnsError() throws Exception {
        //Given...
        String payload = "{\"classNames\": [\"Class/name\"]," +
        "\"requestorType\": \"requestorType\"," +
        "\"requestor\": \"user1\"," +
        "\"testStream\": \"this is a test stream\"," +
        "\"obr\": \"this.obr\","+
        "\"mavenRepository\": \"this.maven.repo\"," +
        "\"sharedEnvironmentPhase\": \"BUILD\"," +
        "\"sharedEnvironmentRunTime\": \"envRunTime\"," +
        "\"overrides\": {}," +
        "\"trace\": true }";

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockFramework mockFramework = new MockFramework();
		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/group", payload, HttpMethod.POST.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        //When...
        servlet.init();
		servlet.doPost(req,resp);

        //Then...
        assertThat(resp.getStatus()).isEqualTo(500);
		assertThat(resp.getContentType()).isEqualTo("application/json");

		checkErrorStructure(
			outStream.toString(),
			5000,
			"GAL5000E: ",
			"Error occurred when trying to access the endpoint"
		);
    }

    @Test
    public void testPostRunsWithNoBodyReturnsError() throws Exception {
        // Given...
		String groupName = "valid";
        String value = "";
        List<IRun> runs = new ArrayList<IRun>();

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, value, HttpMethod.POST.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(411);

		checkErrorStructure(
			outStream.toString(),
			5411, "GAL5411E: Error occurred when trying to access the endpoint '/valid'. The request body is empty."
		);
    }

    @Test
    public void testPostRunsWithInvalidBodyReturnsError() throws Exception {
        // Given...
		String groupName = "valid";
        String value = "Invalid";
        List<IRun> runs = new ArrayList<IRun>();

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, value, HttpMethod.POST.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(400);

		checkErrorStructure(
			outStream.toString(),
			5020, "GAL5020E: Error occurred when trying to translate the payload into a run."
		);
    }

    @Test
    public void testPostRunsWithBadBodyReturnsError() throws Exception {
        // Given...
		String groupName = "valid";
        String payload = "{\"classNames\": [\"badClassName\"]," +
        "\"requestorType\": \"requestorType\"," +
        "\"requestor\": \"user1\"," +
        "\"testStream\": \"this is a test stream\"," +
        "\"obr\": \"this.obr\","+
        "\"mavenRepository\": \"this.maven.repo\"," +
        "\"sharedEnvironmentPhase\": \"envPhase\"," +
        "\"sharedEnvironmentRunTime\": \"envRunTime\"," +
        "\"overrides\": {}" +
        "\"trace\": true }";
        List<IRun> runs = new ArrayList<IRun>();

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, payload, HttpMethod.POST.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(400);
        checkErrorStructure(
			outStream.toString(),
			5020, "E: Error occurred when trying to translate the payload into a run."
		);
    }
    
    @Test
    public void testPostRunsWithValidBodyBadEnvPhaseReturnsError() throws Exception {
        // Given...
		String groupName = "valid";
        String payload = "{\"classNames\": [\"Class/name\"]," +
        "\"requestorType\": \"requestorType\"," +
        "\"requestor\": \"user1\"," +
        "\"testStream\": \"this is a test stream\"," +
        "\"obr\": \"this.obr\","+
        "\"mavenRepository\": \"this.maven.repo\"," +
        "\"sharedEnvironmentPhase\": \"envPhase\"," +
        "\"sharedEnvironmentRunTime\": \"envRunTime\"," +
        "\"overrides\": {}," +
        "\"trace\": true }";
        List<IRun> runs = new ArrayList<IRun>();

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, payload, HttpMethod.POST.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(500);
        checkErrorStructure(
			outStream.toString(),
			5022, "GAL5022E: Error occurred trying parse the sharedEnvironmentPhase 'envPhase'. Valid options are BUILD, DISCARD."
		);
    }

    @Test
    public void testPostRunsWithValidBodyGoodEnvPhaseReturnsOK() throws Exception {
        // Given...
		String groupName = "valid";
        String submissionId = "submission1";
        String payload = "{\"classNames\": [\"Class/name\"]," +
        "\"requestorType\": \"requestorType\"," +
        "\"requestor\": \"testRequestor\"," +
        "\"testStream\": \"this is a test stream\"," +
        "\"obr\": \"this.obr\","+
        "\"mavenRepository\": \"this.maven.repo\"," +
        "\"sharedEnvironmentPhase\": \"BUILD\"," +
        "\"sharedEnvironmentRunTime\": \"envRunTime\"," +
        "\"overrides\": {}," +
        "\"trace\": true }";
        Set<String> tags = new HashSet<>();
        List<IRun> runs = new ArrayList<IRun>();
        runs.add(new MockIRun("runname", "requestorType", JWT_USERNAME, "Class/name", "submitted",
               "Class", "name", groupName, submissionId,tags));

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockIResultArchiveStore mockRasStore = new MockIResultArchiveStore();

        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
        mockFramework.setResultArchiveStore(mockRasStore);

		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, payload, HttpMethod.POST.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(201);
        String expectedJson = generateExpectedJson(runs, false);
        assertThat(resp.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    @Test
    public void testPostRunsWithValidBodyReturnsOK() throws Exception {
        // Given...
		String groupName = "valid";
        String testName1 = "package.class";
        String class1 = "bundle/" + testName1;
        String[] classes = new String[]{class1};
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();
        List<IRun> runs = new ArrayList<IRun>();

        MockIRun run = new MockIRun("runname", "requestorType", JWT_USERNAME, class1, "submitted", class1.split("/")[0], testName1, groupName, submissionId, tags);
        runs.add(run);

        String payload = generatePayload(classes, "requestorType", JWT_USERNAME, null, "this.test.stream", groupName, null, submissionId,tags);

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockIResultArchiveStore mockRasStore = new MockIResultArchiveStore();

        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
        mockFramework.setResultArchiveStore(mockRasStore);

		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, payload, HttpMethod.POST.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, false);
        assertThat(resp.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEqualTo(expectedJson);

        List<TestStructure> testStructureHistory = mockRasStore.getTestStructureHistory();
        assertThat(testStructureHistory).hasSize(1);

        TestStructure testStructure = testStructureHistory.get(0);
        assertThat(testStructure.getRunName()).isEqualTo(run.getName());
        assertThat(testStructure.getBundle()).isEqualTo(run.getTestBundleName());
        assertThat(testStructure.getTestName()).isEqualTo(run.getTestClassName());
        assertThat(testStructure.getSubmissionId()).isEqualTo(run.getSubmissionId());
        assertThat(testStructure.getRequestor()).isEqualTo(run.getRequestor());
        assertThat(testStructure.getGroup()).isEqualTo(run.getGroup());
    }

    @Test
    public void testPostRunsWithEmptyDetailsBodyReturnsError() throws Exception {
        // Given...
		String groupName = "valid";
        String class1 = "Class/name";
        String[] classes = new String[]{class1};
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();
        List<IRun> runs = new ArrayList<IRun>();

        runs.add(new MockIRun("runname", "requestorType", JWT_USERNAME, "name", "submitted", class1.split("/")[0], "java", groupName, submissionId, tags));

        String payload = generatePayload(classes, "requestorType", JWT_USERNAME, null, "null", groupName, null, submissionId,tags);

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockIResultArchiveStore mockRasStore = new MockIResultArchiveStore();

        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
        mockFramework.setResultArchiveStore(mockRasStore);

		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, payload, HttpMethod.POST.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(500);
        checkErrorStructure(
			outStream.toString(),
			5021, "E: Error occurred when trying to submit run 'Class/name'."
		);
    }

    @Test
    public void testPostRunsWithValidBodyAndMultipleClassesReturnsOK() throws Exception {
        // Given...
		String groupName = "valid";
        String class1 = "Class1/name";
        String class2 = "Class2/name";
        String[] classes = new String[]{class1, class2};
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();
        List<IRun> runs = new ArrayList<IRun>();

        runs.add(new MockIRun("runname", "requestorType", JWT_USERNAME, class1, "submitted", class1.split("/")[0], "name", groupName, submissionId, tags));
        runs.add(new MockIRun("runname", "requestorType", JWT_USERNAME, class2, "submitted", class2.split("/")[0], "name", groupName, submissionId, tags));

        String payload = generatePayload(classes, "requestorType", JWT_USERNAME, null, "this.test.stream", groupName, null, submissionId,tags);

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockIResultArchiveStore mockRasStore = new MockIResultArchiveStore();

        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
        mockFramework.setResultArchiveStore(mockRasStore);

        MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, payload, HttpMethod.POST.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, false);
        assertThat(resp.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    @Test
    public void testPostUUIDGroupNameRunsWithValidBodyAndMultipleClassesReturnsOK() throws Exception {
        // Given...
		String groupName = "8149dc91-dabc-461a-b9e8-6f11a4455f59";
        String class1 = "Class1/name";
        String class2 = "Class2/name";
        String[] classes = new String[]{class1, class2};
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();
        List<IRun> runs = new ArrayList<IRun>();

        runs.add(new MockIRun("runname", "requestorType", JWT_USERNAME, class1, "submitted", class1.split("/")[0], "name", groupName, submissionId, tags));
        runs.add(new MockIRun("runname", "requestorType", JWT_USERNAME, class2, "submitted", class2.split("/")[0], "name", groupName, submissionId, tags));

        String payload = generatePayload(classes, "requestorType", JWT_USERNAME, null, "this.test.stream", groupName, null, submissionId, tags);

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockIResultArchiveStore mockRasStore = new MockIResultArchiveStore();

        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
        mockFramework.setResultArchiveStore(mockRasStore);

        MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, payload, HttpMethod.POST.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, false);
        assertThat(resp.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    /*
     * Authorization Tests
     */

    @Test
    public void testPostRunsWithValidBodyGoodEnvPhaseAndJWTReturnsOKWithRequestorFromJWT() throws Exception {
        // Given...
		String groupName = "valid";
        String submissionId = "submission1";
        String payload = "{\"classNames\": [\"Class/name\"]," +
        "\"requestorType\": \"requestorType\"," +
        "\"requestor\": \"testRequestor\"," +
        "\"testStream\": \"this is a test stream\"," +
        "\"obr\": \"this.obr\","+
        "\"mavenRepository\": \"this.maven.repo\"," +
        "\"sharedEnvironmentPhase\": \"BUILD\"," +
        "\"sharedEnvironmentRunTime\": \"envRunTime\"," +
        "\"overrides\": {}," +
        "\"trace\": true }";

        Set<String> tags = new HashSet<>();
        List<IRun> runs = new ArrayList<IRun>();
        runs.add(new MockIRun("runname", "requestorType", JWT_USERNAME, "Class/name", "submitted",
               "Class", "name", groupName, submissionId,tags));

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockIResultArchiveStore mockRasStore = new MockIResultArchiveStore();

        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
        mockFramework.setResultArchiveStore(mockRasStore);

        MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, payload, HttpMethod.POST.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(201);
        String expectedJson = generateExpectedJson(runs, false);
        assertThat(resp.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    @Test
    public void testPostRunsWithValidBodyAndJWTReturnsOKWithRequestorFromJWT() throws Exception {
        // Given...
		String groupName = "valid";
        String class1 = "Class/name";
        String[] classes = new String[]{class1};
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();
        List<IRun> runs = new ArrayList<IRun>();

        runs.add(new MockIRun("runname", "requestorType", JWT_USERNAME, class1, "submitted", class1.split("/")[0], "name", groupName, submissionId, tags));

        String payload = generatePayload(classes, "requestorType", JWT_USERNAME, null, "this.test.stream", groupName, "testRequestor", submissionId, tags);

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockIResultArchiveStore mockRasStore = new MockIResultArchiveStore();

        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
        mockFramework.setResultArchiveStore(mockRasStore);

        MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, payload, HttpMethod.POST.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, false);
        assertThat(resp.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    @Test
    public void testPostRunsWithValidBodyAndMultipleClassesReturnsWithRequestorFromJWT() throws Exception {
        // Given...
		String groupName = "valid";
        String submissionId = "submission1";
        String class1 = "Class1/name";
        String class2 = "Class2/name";
        String[] classes = new String[]{class1, class2};
        Set<String> tags = new HashSet<>();
        List<IRun> runs = new ArrayList<IRun>();

        runs.add(new MockIRun("runname", "requestorType", JWT_USERNAME, class1, "submitted", class1.split("/")[0], "name", groupName, submissionId, tags));
        runs.add(new MockIRun("runname", "requestorType", JWT_USERNAME, class2, "submitted", class2.split("/")[0], "name", groupName, submissionId, tags));

        String payload = generatePayload(classes, "requestorType", JWT_USERNAME, null, "this.test.stream", groupName, "testRequestor", submissionId,tags);

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockIResultArchiveStore mockRasStore = new MockIResultArchiveStore();

        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
        mockFramework.setResultArchiveStore(mockRasStore);

        MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, payload, HttpMethod.POST.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, false);
        assertThat(resp.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    @Test
    public void testPostUUIDGroupNameRunsWithValidBodyAndMultipleClassesReturnsWithRequestorFromJWT() throws Exception {
        // Given...
		String groupName = "8149dc91-dabc-461a-b9e8-6f11a4455f59";
        String class1 = "Class1/name";
        String class2 = "Class2/name";
        String[] classes = new String[]{class1, class2};
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();
        List<IRun> runs = new ArrayList<IRun>();

        runs.add(new MockIRun("runname", "requestorType", JWT_USERNAME, class1, "submitted", class1.split("/")[0], "name", groupName, submissionId, tags));
        runs.add(new MockIRun("runname", "requestorType", JWT_USERNAME, class2, "submitted", class2.split("/")[0], "name", groupName, submissionId, tags));

        String payload = generatePayload(classes, "requestorType", JWT_USERNAME, null, "this.test.stream", groupName, "testRequestor", submissionId,tags);

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockIResultArchiveStore mockRasStore = new MockIResultArchiveStore();

        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
        mockFramework.setResultArchiveStore(mockRasStore);

        MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, payload, HttpMethod.POST.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, false);
        assertThat(resp.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    @Test
    public void testPostRunsWithValidBodyAdminRequestorCanSetUserOK() throws Exception {
        // Given...
		String groupName = "valid";
        String testName1 = "package.class";
        String class1 = "bundle/" + testName1;
        String[] classes = new String[]{class1};
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();
        List<IRun> runs = new ArrayList<IRun>();

        MockIRun run = new MockIRun("runname", "requestorType", JWT_USERNAME, NON_ADMIN_JWT_USERNAME, class1, "submitted", class1.split("/")[0], testName1, groupName, submissionId, tags);
        runs.add(run);

        String payload = generatePayload(classes, "requestorType", JWT_USERNAME, NON_ADMIN_JWT_USERNAME, "this.test.stream", groupName, null, submissionId,tags);

        // Set up permissions - 
        // The JWT_USERNAME has all actions including TEST_RUN_SET_USER.
        // The NON_ADMIN_JWT_USERNAME has GENERAL_API_ACCESS and TEST_RUN_LAUNCH.
        List<Action> permittedActions = List.of(GENERAL_API_ACCESS.getAction(), TEST_RUN_LAUNCH.getAction());
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithAdminAndTestUser(JWT_USERNAME, NON_ADMIN_JWT_USERNAME, permittedActions);

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockIResultArchiveStore mockRasStore = new MockIResultArchiveStore();

        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
        mockFramework.setResultArchiveStore(mockRasStore);
        mockFramework.setRBACService(mockRbacService);

		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, payload, HttpMethod.POST.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, false);
        assertThat(resp.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEqualTo(expectedJson);

        List<TestStructure> testStructureHistory = mockRasStore.getTestStructureHistory();
        assertThat(testStructureHistory).hasSize(1);

        TestStructure testStructure = testStructureHistory.get(0);
        assertThat(testStructure.getRequestor()).isEqualTo(run.getRequestor());
        assertThat(testStructure.getUser()).isEqualTo(run.getUser());
        assertThat(testStructure.getRunName()).isEqualTo(run.getName());
        assertThat(testStructure.getBundle()).isEqualTo(run.getTestBundleName());
        assertThat(testStructure.getTestName()).isEqualTo(run.getTestClassName());
        assertThat(testStructure.getSubmissionId()).isEqualTo(run.getSubmissionId());
        assertThat(testStructure.getGroup()).isEqualTo(run.getGroup());
    }

    @Test
    public void testPostRunsWithValidBodyNonAdminRequestorCannotSetUserButUserDefaultsToRequestorOK() throws Exception {
        // Given...
		String groupName = "valid";
        String testName1 = "package.class";
        String class1 = "bundle/" + testName1;
        String[] classes = new String[]{class1};
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();
        List<IRun> runs = new ArrayList<IRun>();

        MockIRun run = new MockIRun("runname", "requestorType", JWT_USERNAME, JWT_USERNAME, class1, "submitted", class1.split("/")[0], testName1, groupName, submissionId, tags);
        runs.add(run);

        String payload = generatePayload(classes, "requestorType", JWT_USERNAME, "overrideuser", "this.test.stream", groupName, null, submissionId,tags);

        // Set up permissions without the TEST_RUN_LAUNCH action
        List<Action> permittedActions = List.of(GENERAL_API_ACCESS.getAction(), TEST_RUN_LAUNCH.getAction());
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME, permittedActions);

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockIResultArchiveStore mockRasStore = new MockIResultArchiveStore();

        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
        mockFramework.setResultArchiveStore(mockRasStore);
        mockFramework.setRBACService(mockRbacService);

		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, payload, HttpMethod.POST.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, false);
        assertThat(resp.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEqualTo(expectedJson);

        List<TestStructure> testStructureHistory = mockRasStore.getTestStructureHistory();
        assertThat(testStructureHistory).hasSize(1);

        TestStructure testStructure = testStructureHistory.get(0);
        assertThat(testStructure.getRequestor()).isEqualTo(run.getRequestor());
        assertThat(testStructure.getUser()).isEqualTo(run.getUser());
        assertThat(testStructure.getRunName()).isEqualTo(run.getName());
        assertThat(testStructure.getBundle()).isEqualTo(run.getTestBundleName());
        assertThat(testStructure.getTestName()).isEqualTo(run.getTestClassName());
        assertThat(testStructure.getSubmissionId()).isEqualTo(run.getSubmissionId());
        assertThat(testStructure.getGroup()).isEqualTo(run.getGroup());
    }

    @Test
    public void testPostRunsWithValidBodyAdminRequestorCanSetUserButUserCantLaunchTestsReturnsError() throws Exception {
        // Given...
		String groupName = "valid";
        String testName1 = "package.class";
        String class1 = "bundle/" + testName1;
        String[] classes = new String[]{class1};
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();

        String payload = generatePayload(classes, "requestorType", JWT_USERNAME, NON_ADMIN_JWT_USERNAME, "this.test.stream", groupName, null, submissionId,tags);

        // Set up permissions - 
        // The JWT_USERNAME has all actions including TEST_RUN_SET_USER.
        // The NON_ADMIN_JWT_USERNAME has GENERAL_API_ACCESS but does not have TEST_RUN_LAUNCH.
        List<Action> permittedActions = List.of(GENERAL_API_ACCESS.getAction());
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithAdminAndTestUser(JWT_USERNAME, NON_ADMIN_JWT_USERNAME, permittedActions);

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();

        MockFramework mockFramework = new MockFramework();
        mockFramework.setRBACService(mockRbacService);

		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, payload, HttpMethod.POST.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(403);
        assertThat(resp.getContentType()).isEqualTo(MimeType.APPLICATION_JSON.toString());
        checkErrorStructure(outStream.toString(), 5125, "GAL5125E", "TEST_RUN_LAUNCH");
    }

    /*
     * PUT requests
     */

    @Test
    public void testUpdateRunStatusByGroupIdWhenNoActiveRunsExistReturnsOK() throws Exception {
        // Given...
		String groupName = "8149dc91-dabc-461a-b9e8-6f11a4455f59";
        String payload = generateStatusUpdateJson("cancelled");
        List<IRun> runs = new ArrayList<IRun>();

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, payload, HttpMethod.PUT.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(req, resp);

        // Then...
        String expectedString = "Info: When trying to cancel the run group '8149dc91-dabc-461a-b9e8-6f11a4455f59', no recent active (unfinished) test runs were found which are part of that group. Archived test runs may be part of that group, which can be queried separately from the Result Archive Store.";
        assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(expectedString);
    }

    @Test
    public void testUpdateRunStatusByGroupIdWithFewFinishedRunsReturnsAccepted() throws Exception {
        // Given...
		String groupName = "8149dc91-dabc-461a-b9e8-6f11a4455f59";
        String payload = generateStatusUpdateJson("cancelled");
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();
        List<IRun> runs = new ArrayList<IRun>();
        runs.add(new MockIRun("name1", "type1", "requestor1", "test1", "BUILDING","bundle1", "testClass1", groupName, submissionId,tags));
        runs.add(new MockIRun("name2", "type2", "requestor2", "test2", "BUILDING","bundle2", "testClass2", groupName, submissionId,tags));
        runs.add(new MockIRun("name3", "type3", "requestor3", "test3", "FINISHED","bundle3", "testClass3", groupName, submissionId,tags));
        runs.add(new MockIRun("name4", "type4", "requestor4", "test4", "UP","bundle4", "testClass4", groupName, submissionId,tags));
        runs.add(new MockIRun("name5", "type6", "requestor5", "test5", "DISCARDED","bundle5", "testClass6", groupName, submissionId,tags));
        runs.add(new MockIRun("name6", "type6", "requestor6", "test6", "FINISHED","bundle6", "testClass6", groupName, submissionId,tags));
        runs.add(new MockIRun("name7", "type7", "requestor7", "test7", "FINISHED","bundle7", "testClass7", groupName, submissionId,tags));
        runs.add(new MockIRun("name8", "type8", "requestor8", "test8", "BUILDING","bundle8", "testClass8", groupName, submissionId,tags));
        runs.add(new MockIRun("name9", "type9", "requestor9", "test9", "BUILDING","bundle9", "testClass9", groupName, submissionId,tags));
        runs.add(new MockIRun("name10", "type10", "requestor10", "test10", "BUILDING","bundle10", "testClass10", groupName, submissionId,tags));

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, payload, HttpMethod.PUT.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(req, resp);

        // Then...
        String expectedString = "The request to cancel run with group id '8149dc91-dabc-461a-b9e8-6f11a4455f59' has been received.";
        assertThat(resp.getStatus()).isEqualTo(202);
        assertThat(outStream.toString()).isEqualTo(expectedString);
    }

    @Test
    public void testUpdateRunStatusByGroupIdWithAllActiveRunsReturnsAccepted() throws Exception {
        // Given...
		String groupName = "8149dc91-dabc-461a-b9e8-6f11a4455f59";
        String payload = generateStatusUpdateJson("cancelled");
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();
        List<IRun> runs = new ArrayList<IRun>();
        runs.add(new MockIRun("name1", "type1", "requestor1", "test1", "BUILDING","bundle1", "testClass1", groupName, submissionId,tags));
        runs.add(new MockIRun("name2", "type2", "requestor2", "test2", "BUILDING","bundle2", "testClass2", groupName, submissionId,tags));
        runs.add(new MockIRun("name3", "type3", "requestor3", "test3", "BUILDING","bundle3", "testClass3", groupName, submissionId,tags));
        runs.add(new MockIRun("name4", "type4", "requestor4", "test4", "BUILDING","bundle4", "testClass4", groupName, submissionId,tags));
        runs.add(new MockIRun("name5", "type6", "requestor5", "test5", "BUILDING","bundle5", "testClass6", groupName, submissionId,tags));
        runs.add(new MockIRun("name6", "type6", "requestor6", "test6", "BUILDING","bundle6", "testClass6", groupName, submissionId,tags));
        runs.add(new MockIRun("name7", "type7", "requestor7", "test7", "BUILDING","bundle7", "testClass7", groupName, submissionId,tags));
        runs.add(new MockIRun("name8", "type8", "requestor8", "test8", "BUILDING","bundle8", "testClass8", groupName, submissionId,tags));
        runs.add(new MockIRun("name9", "type9", "requestor9", "test9", "BUILDING","bundle9", "testClass9", groupName, submissionId,tags));
        runs.add(new MockIRun("name10", "type10", "requestor10", "test10", "BUILDING","bundle10", "testClass10", groupName, submissionId,tags));

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, payload, HttpMethod.PUT.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(req, resp);

        // Then...
        String expectedString = "The request to cancel run with group id '8149dc91-dabc-461a-b9e8-6f11a4455f59' has been received.";
        assertThat(resp.getStatus()).isEqualTo(202);
        assertThat(outStream.toString()).isEqualTo(expectedString);
    }

    @Test
    public void testUpdateRunStatusByGroupIdWithInvalidRequestReturnsBadRequest() throws Exception {
        // Given...
		String groupName = "8149dc91-dabc-461a-b9e8-6f11a4455f59";
        String payload = generateStatusUpdateJson("some-fake-status");
        List<IRun> runs = new ArrayList<IRun>();

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(groupName, runs);
        MockFramework mockFramework = new MockFramework(mockFrameworkRuns);
		MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, payload, HttpMethod.PUT.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(),5431, "Error occurred. The field 'result' in the request body is invalid");
    }

    @Test
    public void testLaunchTestWithMissingPermissionsReturnsForbidden() throws Exception {
        // Given...
		String groupName = "valid";
        String class1 = "Class/name";
        String[] classes = new String[]{class1};
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();

        String payload = generatePayload(classes, "requestorType", JWT_USERNAME, null, "this.test.stream", groupName, "testRequestor", submissionId, tags);

        // Set up permissions without the TEST_RUN_LAUNCH action
        List<Action> permittedActions = List.of(GENERAL_API_ACCESS.getAction());
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME, permittedActions);

        MockFramework mockFramework = new MockFramework();
        mockFramework.setRBACService(mockRbacService);

        MockEnvironment mockEnv = FilledMockEnvironment.createTestEnvironment();
        MockRunsServlet servlet = new MockRunsServlet(mockEnv, mockFramework);
        servlet.setResponseBuilder(new ResponseBuilder(mockEnv));

		HttpServletRequest req = new MockHttpServletRequest("/"+groupName, payload, HttpMethod.POST.toString(), REQUIRED_HEADERS);
		HttpServletResponse resp = new MockHttpServletResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(403);
        assertThat(resp.getContentType()).isEqualTo(MimeType.APPLICATION_JSON.toString());
        checkErrorStructure(outStream.toString(), 5125, "GAL5125E", "TEST_RUN_LAUNCH");
    }
}
