/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import dev.galasa.framework.api.runs.RunsServletTest;
import dev.galasa.framework.api.runs.mocks.MockRunsServlet;

public class TestGroupRunsRoute extends RunsServletTest{

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
        setServlet("/"+groupName, groupName, this.runs);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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
        setServlet("/"+groupName, groupName, this.runs);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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
        setServlet("/"+groupName, groupName, this.runs);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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
        addRun("name1", "type1", "requestor1", "test1", "FINISHED","bundle1", "testClass1", groupName, submissionId,tags);
        setServlet("/"+groupName, groupName, this.runs);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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
        addRun("name1", "type1", "requestor1", "test1", "BUILDING","bundle1", "testClass1", groupName, submissionId,tags);
        addRun("name2", "type2", "requestor2", "test2", "BUILDING","bundle2", "testClass2", groupName, submissionId,tags);
        setServlet("/"+groupName, groupName, this.runs);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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
        addRun("name1", "type1", "requestor1", "test1", "BUILDING","bundle1", "testClass1", groupName, submissionId,tags);
        addRun("name2", "type2", "requestor2", "test2", "BUILDING","bundle2", "testClass2", groupName, submissionId,tags);
        addRun("name3", "type3", "requestor3", "test3", "FINISHED","bundle3", "testClass3", groupName, submissionId,tags);
        addRun("name4", "type4", "requestor4", "test4", "UP","bundle4", "testClass4", groupName, submissionId,tags);
        addRun("name5", "type6", "requestor5", "test5", "DISCARDED","bundle5", "testClass6", groupName, submissionId,tags);
        addRun("name6", "type6", "requestor6", "test6", "BUILDING","bundle6", "testClass6", groupName, submissionId,tags);
        addRun("name7", "type7", "requestor7", "test7", "BUILDING","bundle7", "testClass7", groupName, submissionId,tags);
        addRun("name8", "type8", "requestor8", "test8", "BUILDING","bundle8", "testClass8", groupName, submissionId,tags);
        addRun("name9", "type9", "requestor9", "test9", "BUILDING","bundle9", "testClass9", groupName, submissionId,tags);
        addRun("name10", "type10", "requestor10", "test10", "BUILDING","bundle10", "testClass10", groupName, submissionId,tags);
        setServlet("/"+groupName, groupName, this.runs);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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
        addRun("name1", "type1", "requestor1", "test1", "BUILDING","bundle1", "testClass1", groupName, submissionId,tags);
        addRun("name2", "type2", "requestor2", "test2", "BUILDING","bundle2", "testClass2", groupName, submissionId,tags);
        addRun("name3", "type3", "requestor3", "test3", "FINISHED","bundle3", "testClass3", groupName, submissionId,tags);
        addRun("name4", "type4", "requestor4", "test4", "UP","bundle4", "testClass4", groupName, submissionId,tags);
        addRun("name5", "type6", "requestor5", "test5", "DISCARDED","bundle5", "testClass6", groupName, submissionId,tags);
        addRun("name6", "type6", "requestor6", "test6", "BUILDING","bundle6", "testClass6", groupName, submissionId,tags);
        addRun("name7", "type7", "requestor7", "test7", "BUILDING","bundle7", "testClass7", groupName, submissionId,tags);
        addRun("name8", "type8", "requestor8", "test8", "BUILDING","bundle8", "testClass8", groupName, submissionId,tags);
        addRun("name9", "type9", "requestor9", "test9", "BUILDING","bundle9", "testClass9", groupName, submissionId,tags);
        addRun("name10", "type10", "requestor10", "test10", "BUILDING","bundle10", "testClass10", groupName, submissionId,tags);
        setServlet("/"+groupName, groupName, this.runs);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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


        setServlet("/group", null, payload, "POST");
        MockRunsServlet servlet = getServlet();
        HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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
        setServlet("/"+groupName, groupName, value, "POST");
;		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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
        setServlet("/"+groupName, groupName, value, "POST");
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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

        setServlet("/"+groupName, groupName, payload, "POST");
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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

        setServlet("/"+groupName, groupName, payload, "POST");
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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
        addRun("runnamename", "requestorType", JWT_USERNAME, "name", "submitted",
               "Class", "java", groupName, submissionId,tags);

        setServlet("/"+groupName, groupName, payload, "POST");
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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
        String[] classes = new String[]{"Class/name"};
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();
        String payload = generatePayload(classes, "requestorType", JWT_USERNAME, "this.test.stream", groupName, null, submissionId,tags);

        setServlet("/"+groupName, groupName, payload, "POST");
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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
    public void testPostRunsWithEmptyDetailsBodyReturnsError() throws Exception {
        // Given...
		String groupName = "valid";
        String[] classes = new String[]{"Class/name"};
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();
        String payload = generatePayload(classes, "requestorType", JWT_USERNAME, null, groupName, null, submissionId,tags);

        setServlet("/"+groupName, groupName, payload, "POST");
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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
        String[] classes = new String[]{"Class1/name", "Class2/name"};
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();
        String payload = generatePayload(classes, "requestorType", JWT_USERNAME, "this.test.stream", groupName, null, submissionId,tags);

        setServlet("/"+groupName, groupName, payload, "POST");
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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
        String[] classes = new String[]{"Class1/name", "Class2/name"};
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();
        String payload = generatePayload(classes, "requestorType", JWT_USERNAME, "this.test.stream", groupName, null, submissionId, tags);

        setServlet("/"+groupName, groupName, payload, "POST");
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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
        addRun("runnamename", "requestorType", JWT_USERNAME, "name", "submitted",
               "Class", "java", groupName, submissionId,tags);

        setServlet("/"+groupName, groupName, payload, "POST");
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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
        String[] classes = new String[]{"Class/name"};
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();
        String payload = generatePayload(classes, "requestorType", JWT_USERNAME, "this.test.stream", groupName, "testRequestor", submissionId, tags);

        setServlet("/"+groupName, groupName, payload, "POST");
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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
        String[] classes = new String[]{"Class1/name", "Class2/name"};
        Set<String> tags = new HashSet<>();
        String payload = generatePayload(classes, "requestorType", JWT_USERNAME, "this.test.stream", groupName, "testRequestor", submissionId,tags);

        setServlet("/"+groupName, groupName, payload, "POST");
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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
        String[] classes = new String[]{"Class1/name", "Class2/name"};
        String submissionId = "submission1";
        Set<String> tags = new HashSet<>();
        String payload = generatePayload(classes, "requestorType", JWT_USERNAME, "this.test.stream", groupName, "testRequestor", submissionId,tags);

        setServlet("/"+groupName, groupName, payload, "POST");
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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
    public void testUpdateRunStatusByGroupIdWhenNoActiveRunsExistReturnsOK() throws Exception {
        // Given...
		String groupName = "8149dc91-dabc-461a-b9e8-6f11a4455f59";
        String payload = generateStatusUpdateJson("cancelled");

        setServlet("/"+groupName, groupName, payload, "PUT");
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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
        addRun("name1", "type1", "requestor1", "test1", "BUILDING","bundle1", "testClass1", groupName, submissionId,tags);
        addRun("name2", "type2", "requestor2", "test2", "BUILDING","bundle2", "testClass2", groupName, submissionId,tags);
        addRun("name3", "type3", "requestor3", "test3", "FINISHED","bundle3", "testClass3", groupName, submissionId,tags);
        addRun("name4", "type4", "requestor4", "test4", "UP","bundle4", "testClass4", groupName, submissionId,tags);
        addRun("name5", "type6", "requestor5", "test5", "DISCARDED","bundle5", "testClass6", groupName, submissionId,tags);
        addRun("name6", "type6", "requestor6", "test6", "FINISHED","bundle6", "testClass6", groupName, submissionId,tags);
        addRun("name7", "type7", "requestor7", "test7", "FINISHED","bundle7", "testClass7", groupName, submissionId,tags);
        addRun("name8", "type8", "requestor8", "test8", "BUILDING","bundle8", "testClass8", groupName, submissionId,tags);
        addRun("name9", "type9", "requestor9", "test9", "BUILDING","bundle9", "testClass9", groupName, submissionId,tags);
        addRun("name10", "type10", "requestor10", "test10", "BUILDING","bundle10", "testClass10", groupName, submissionId,tags);

        setServlet("/" + groupName, groupName, payload, "PUT", this.runs);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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
        addRun("name1", "type1", "requestor1", "test1", "BUILDING","bundle1", "testClass1", groupName, submissionId,tags);
        addRun("name2", "type2", "requestor2", "test2", "BUILDING","bundle2", "testClass2", groupName, submissionId,tags);
        addRun("name3", "type3", "requestor3", "test3", "BUILDING","bundle3", "testClass3", groupName, submissionId,tags);
        addRun("name4", "type4", "requestor4", "test4", "BUILDING","bundle4", "testClass4", groupName, submissionId,tags);
        addRun("name5", "type6", "requestor5", "test5", "BUILDING","bundle5", "testClass6", groupName, submissionId,tags);
        addRun("name6", "type6", "requestor6", "test6", "BUILDING","bundle6", "testClass6", groupName, submissionId,tags);
        addRun("name7", "type7", "requestor7", "test7", "BUILDING","bundle7", "testClass7", groupName, submissionId,tags);
        addRun("name8", "type8", "requestor8", "test8", "BUILDING","bundle8", "testClass8", groupName, submissionId,tags);
        addRun("name9", "type9", "requestor9", "test9", "BUILDING","bundle9", "testClass9", groupName, submissionId,tags);
        addRun("name10", "type10", "requestor10", "test10", "BUILDING","bundle10", "testClass10", groupName, submissionId,tags);

        setServlet("/" + groupName, groupName, payload, "PUT", this.runs);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
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

        setServlet("/" + groupName, groupName, payload, "PUT");
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(),5431, "Error occurred. The field 'result' in the request body is invalid");
    }

}
