/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.runs.RunsServletTest;
import dev.galasa.framework.api.runs.mocks.MockRunsServlet;

public class TestGroupRunsRoute extends RunsServletTest{
    
    private String jwt = BaseServletTest.DUMMY_JWT; // Mock JWT, not a secret //pragma: allowlist secret
    private Map<String, String> headerMap = Map.of("Authorization", "Bearer "+jwt);

    /*
     * Regex Path
     */

    @Test
    public void TestPathRegexExpectedPathReturnsTrue(){
        //Given...
        String expectedPath = GroupRunsRoute.path;
        String inputPath = "/correct-ID_1234";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isTrue();
    }

    @Test
    public void TestPathRegexLowerCasePathReturnsTrue(){
        //Given...
        String expectedPath = GroupRunsRoute.path;
        String inputPath = "/thisisavalidpath";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isTrue();
    }

    @Test
    public void TestPathRegexUpperCasePathReturnsTrue(){
        //Given...
        String expectedPath = GroupRunsRoute.path;
        String inputPath = "/ALLCAPITALS";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isTrue();
    }

    @Test
    public void TestPathRegexNumberPathReturnsTrue(){
        //Given...
        String expectedPath = GroupRunsRoute.path;
        String inputPath = "/1234";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isTrue();
    }

    @Test
    public void TestPathRegexUnexpectedPathReturnsTrue(){
        //Given...
        String expectedPath = GroupRunsRoute.path;
        String inputPath = "/incorrect-?ID_1234";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexEmptyPathReturnsFalse(){
        //Given...
        String expectedPath = GroupRunsRoute.path;
        String inputPath = "";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexDotPathReturnsFalse(){
        //Given...
        String expectedPath = GroupRunsRoute.path;
        String inputPath = "/random.String";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexSpecialCharacterPathReturnsFalse(){
        //Given...
        String expectedPath = GroupRunsRoute.path;
        String inputPath = "/?";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexMultipleForwardSlashPathReturnsFalse(){
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
    public void TestGetRunsNoFrameworkReturnsError() throws Exception {
        //Given...
        setServlet("/group", null, null);
        MockRunsServlet servlet = getServlet();
        HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

        //When...
        servlet.init();
		servlet.doGet(req,resp);

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
    public void TestGetRunsWithInvalidGroupNameReturnsError() throws Exception {
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
        assertThat(resp.getStatus()).isEqualTo(404);

		checkErrorStructure(
			outStream.toString(),
			5019, "E: Unable to retrieve runs for Run Group: 'invalid'."
		);
    }

    @Test
    public void TestGetRunsWithValidGroupNameWithNullRunsReturnsError() throws Exception {
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
    public void TestGetRunsWithEmptyGroupNameReturnsOK() throws Exception {
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
    public void TestGetRunsWithValidGroupNameReturnsOk() throws Exception {
        // Given...
		String groupName = "framework";
        addRun("name1", "type1", "requestor1", "test1", "FINISHED","bundle1", "testClass1", groupName);
        setServlet("/"+groupName, groupName, this.runs);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, "true");
        assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    @Test
    public void TestGetRunsWithValidGroupNameReturnsMultiple() throws Exception {
        // Given...
		String groupName = "framework";
        addRun("name1", "type1", "requestor1", "test1", "BUILDING","bundle1", "testClass1", groupName);
        addRun("name2", "type2", "requestor2", "test2", "BUILDING","bundle2", "testClass2", groupName);
        setServlet("/"+groupName, groupName, this.runs);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, "false");
        assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

     @Test
    public void TestGetRunsWithValidGroupNameMultipleWithFinishedRunReturnsCompleteFalse() throws Exception {
        // Given...
		String groupName = "framework";
        addRun("name1", "type1", "requestor1", "test1", "BUILDING","bundle1", "testClass1", groupName);
        addRun("name2", "type2", "requestor2", "test2", "BUILDING","bundle2", "testClass2", groupName);
        addRun("name3", "type3", "requestor3", "test3", "FINISHED","bundle3", "testClass3", groupName);
        addRun("name4", "type4", "requestor4", "test4", "UP","bundle4", "testClass4", groupName);
        addRun("name5", "type6", "requestor5", "test5", "DISCARDED","bundle5", "testClass6", groupName);
        addRun("name6", "type6", "requestor6", "test6", "BUILDING","bundle6", "testClass6", groupName);
        addRun("name7", "type7", "requestor7", "test7", "BUILDING","bundle7", "testClass7", groupName);
        addRun("name8", "type8", "requestor8", "test8", "BUILDING","bundle8", "testClass8", groupName);
        addRun("name9", "type9", "requestor9", "test9", "BUILDING","bundle9", "testClass9", groupName);
        addRun("name10", "type10", "requestor10", "test10", "BUILDING","bundle10", "testClass10", groupName);
        setServlet("/"+groupName, groupName, this.runs);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, "false");
        assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    @Test
    public void TestGetRunsWithUUIDGroupNameMultipleWithFinishedRunReturnsCompleteFalse() throws Exception {
        // Given...
		String groupName = "8149dc91-dabc-461a-b9e8-6f11a4455f59";
        addRun("name1", "type1", "requestor1", "test1", "BUILDING","bundle1", "testClass1", groupName);
        addRun("name2", "type2", "requestor2", "test2", "BUILDING","bundle2", "testClass2", groupName);
        addRun("name3", "type3", "requestor3", "test3", "FINISHED","bundle3", "testClass3", groupName);
        addRun("name4", "type4", "requestor4", "test4", "UP","bundle4", "testClass4", groupName);
        addRun("name5", "type6", "requestor5", "test5", "DISCARDED","bundle5", "testClass6", groupName);
        addRun("name6", "type6", "requestor6", "test6", "BUILDING","bundle6", "testClass6", groupName);
        addRun("name7", "type7", "requestor7", "test7", "BUILDING","bundle7", "testClass7", groupName);
        addRun("name8", "type8", "requestor8", "test8", "BUILDING","bundle8", "testClass8", groupName);
        addRun("name9", "type9", "requestor9", "test9", "BUILDING","bundle9", "testClass9", groupName);
        addRun("name10", "type10", "requestor10", "test10", "BUILDING","bundle10", "testClass10", groupName);
        setServlet("/"+groupName, groupName, this.runs);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, "false");
        assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    /*
     * POST requests
     */

    @Test
    public void TestPostRunsNoFrameworkReturnsError() throws Exception {
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
    public void TestPostRunsWithNoBodyReturnsError() throws Exception {
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
    public void TestPostRunsWithInvalidBodyReturnsError() throws Exception {
        // Given...
		String groupName = "valid";
        String value = "Invalid";
        setServlet("/"+groupName, groupName, value, "POST");
;		MockRunsServlet servlet = getServlet();
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
    public void TestPostRunsWithBadBodyReturnsError() throws Exception {
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
    public void TestPostRunsWithValidBodyBadEnvPhaseReturnsError() throws Exception {
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
    public void TestPostRunsWithValidBodyGoodEnvPhaseReturnsOK() throws Exception {
        // Given...
		String groupName = "valid";
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
        addRun("runnamename", "requestorType", "user1", "name", "submitted",
               "Class", "java", groupName);

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
        String expectedJson = generateExpectedJson(runs, "false");
        assertThat(resp.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    @Test
    public void TestPostRunsWithValidBodyReturnsOK() throws Exception {
        // Given...
		String groupName = "valid";
        String[] classes = new String[]{"Class/name"};
        String payload = generatePayload(classes, "requestorType", "user1", "this.test.stream", groupName, null);

        setServlet("/"+groupName, groupName, payload, "POST");
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, "false");
        assertThat(resp.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    @Test
    public void TestPostRunsWithEmptyDetailsBodyReturnsError() throws Exception {
        // Given...
		String groupName = "valid";
        String[] classes = new String[]{"Class/name"};
        String payload = generatePayload(classes, "requestorType", "user1", null, groupName, null);

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
    public void TestPostRunsWithValidBodyAndMultipleClassesReturnsOK() throws Exception {
        // Given...
		String groupName = "valid";
        String[] classes = new String[]{"Class1/name", "Class2/name"};
        String payload = generatePayload(classes, "requestorType", "user1", "this.test.stream", groupName, null);

        setServlet("/"+groupName, groupName, payload, "POST");
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, "false");
        assertThat(resp.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    @Test
    public void TestPostUUIDGroupNameRunsWithValidBodyAndMultipleClassesReturnsOK() throws Exception {
        // Given...
		String groupName = "8149dc91-dabc-461a-b9e8-6f11a4455f59";
        String[] classes = new String[]{"Class1/name", "Class2/name"};
        String payload = generatePayload(classes, "requestorType", "user1", "this.test.stream", groupName, null);

        setServlet("/"+groupName, groupName, payload, "POST");
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, "false");
        assertThat(resp.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    /*
     * Authorization Tests
     */

    @Test
    public void TestPostRunsWithValidBodyGoodEnvPhaseAndJWTReturnsOKWithRequestorFromJWT() throws Exception {
        // Given...
		String groupName = "valid";
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

        addRun("runnamename", "requestorType", "testRequestor", "name", "submitted",
               "Class", "java", groupName);

        setServlet("/"+groupName, groupName, payload, "POST", headerMap);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        assertThat(resp.getStatus()).isEqualTo(201);
        String expectedJson = generateExpectedJson(runs, "false");
        assertThat(resp.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    @Test
    public void TestPostRunsWithValidBodyAndJWTReturnsOKWithRequestorFromJWT() throws Exception {
        // Given...
		String groupName = "valid";
        String[] classes = new String[]{"Class/name"};
        String payload = generatePayload(classes, "requestorType", "user1", "this.test.stream", groupName, "testRequestor");

        setServlet("/"+groupName, groupName, payload, "POST", headerMap);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, "false");
        assertThat(resp.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    @Test
    public void TestPostRunsWithValidBodyAndMultipleClassesReturnsWithRequestorFromJWT() throws Exception {
        // Given...
		String groupName = "valid";
        String[] classes = new String[]{"Class1/name", "Class2/name"};
        String payload = generatePayload(classes, "requestorType", "user1", "this.test.stream", groupName, "testRequestor");

        setServlet("/"+groupName, groupName, payload, "POST", headerMap);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, "false");
        assertThat(resp.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    @Test
    public void TestPostUUIDGroupNameRunsWithValidBodyAndMultipleClassesReturnsWithRequestorFromJWT() throws Exception {
        // Given...
		String groupName = "8149dc91-dabc-461a-b9e8-6f11a4455f59";
        String[] classes = new String[]{"Class1/name", "Class2/name"};
        String payload = generatePayload(classes, "requestorType", "user1", "this.test.stream", groupName, "testRequestor");

        setServlet("/"+groupName, groupName, payload, "POST", headerMap);
		MockRunsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(req, resp);

        // Then...
        String expectedJson = generateExpectedJson(runs, "false");
        assertThat(resp.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }
}
