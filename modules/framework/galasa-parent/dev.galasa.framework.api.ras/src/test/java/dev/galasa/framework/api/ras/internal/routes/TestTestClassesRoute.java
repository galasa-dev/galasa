/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.RasTestClass;
import dev.galasa.framework.spi.teststructure.TestStructure;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.ras.internal.RasServlet;
import dev.galasa.framework.api.ras.internal.RasServletTest;
import dev.galasa.framework.api.ras.internal.mocks.*;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.spi.utils.GalasaGson;

public class TestTestClassesRoute extends RasServletTest{

    static final GalasaGson gson = new GalasaGson();

    public List<IRunResult> generateTestData (int resSize){
		List<IRunResult> mockInputRunResults = new ArrayList<IRunResult>();
		// Build the results the DB will return.
		for(int c =0 ; c < resSize; c++){
			String runId = RandomStringUtils.randomAlphanumeric(16);
			TestStructure testStructure = new TestStructure();
			switch (c % 5){
				//testStructure.getBundle()+"/"+testStructure.getTestName();
                case 0: 
					testStructure.setBundle("dev.galasa");
					testStructure.setTestName("com.runId");
					break;
				case 1: 
					testStructure.setBundle("dev.galasa");
					testStructure.setTestName("com.mickey");
					break;
				case 2: 
					testStructure.setBundle("dev.galasa");
					testStructure.setTestName("com.user");
					break;
                case 3: 
					testStructure.setBundle("dev.galasa");
					testStructure.setTestName("com.UNKNOWN");
					break;
                case 4: 
					testStructure.setBundle("dev.galasa");
					testStructure.setTestName("dev.jindex");
					break;
			}
			IRunResult result = new MockRunResult( runId, testStructure, null , null);
			mockInputRunResults.add(result);
		}
		return mockInputRunResults;
	}

    private String generateExpectedJSON (List<IRunResult> mockInputRunResults, boolean reverse) throws ResultArchiveStoreException{

        HashMap<String,RasTestClass> tests = new HashMap<>();
        String key;
        for (IRunResult run : mockInputRunResults){
			TestStructure testStructure = run.getTestStructure();
			key = testStructure.getBundle()+"/"+testStructure.getTestName();
			if(!tests.containsKey(key)){
				tests.put(key,new RasTestClass(testStructure.getTestName(), testStructure.getBundle()));
			}
        }
        List<RasTestClass> testClasses = new ArrayList<>(tests.values());
        
        testClasses.sort(Comparator.comparing(RasTestClass::getTestClass));
        if (reverse == true) {
            testClasses.sort(Comparator.comparing(RasTestClass::getTestClass).reversed());
        }
		JsonElement jsonResultsArray = gson.toJsonTree(testClasses);
		JsonObject json = new JsonObject();
		json.add("testclasses", jsonResultsArray);
		return json.toString();
    }

    /*
     * Tests 
     */

	 /*
     * Regex Path
     */

	@Test
	public void TestPathRegexExpectedPathReturnsTrue(){
		//Given...
		String expectedPath = TestClassesRoute.path;
		String inputPath = "/testclasses";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void TestPathRegexExpectedPathWithQueryReturnsFalse(){
		//Given...
		String expectedPath = TestClassesRoute.path;
		String inputPath = "/testclasses?requestor=Mickey";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void TestPathRegexExpectedPathWithNumbersReturnsFalse(){
		//Given...
		String expectedPath = TestClassesRoute.path;
		String inputPath = "/t3stclass3s";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void TestPathRegexLowerCasePathReturnsTrue(){
		//Given...
		String expectedPath = TestClassesRoute.path;
		String inputPath = "/testclasses";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}
	
	@Test
	public void TestPathRegexExpectedPathWithCapitalLeadingLetterReturnsFalse(){
		//Given...
		String expectedPath = TestClassesRoute.path;
		String inputPath = "/Testclasses";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}
	
	@Test
	public void TestPathRegexUpperCasePathReturnsFalse(){
		//Given...
		String expectedPath = TestClassesRoute.path;
		String inputPath = "/TESTCLASSES";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}
 
	@Test
	public void TestPathRegexExpectedPathWithLeadingNumberReturnsFalse(){
		//Given...
		String expectedPath = TestClassesRoute.path;
		String inputPath = "/0testclasses";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void TestPathRegexExpectedPathWithTrailingForwardSlashReturnsTrue(){
		//Given...
		String expectedPath = TestClassesRoute.path;
		String inputPath = "/testclasses/";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void TestPathRegexNumberPathReturnsFalse(){
		//Given...
		String expectedPath = TestClassesRoute.path;
		String inputPath = "/testclasses1234";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void TestPathRegexUnexpectedPathReturnsFalse(){
		//Given...
		String expectedPath = TestClassesRoute.path;
		String inputPath = "/testclass";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void TestPathRegexEmptyPathReturnsFalse(){
		//Given...
		String expectedPath = TestClassesRoute.path;
		String inputPath = "";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void TestPathRegexSpecialCharacterPathReturnsFalse(){
		//Given...
		String expectedPath = TestClassesRoute.path;
		String inputPath = "/testclasses/?";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void TestPathRegexMultipleForwardSlashPathReturnsFalse(){
		//Given...
		String expectedPath = TestClassesRoute.path;
		String inputPath = "/testclasses//////";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	} 

	/*
	 * Tests - GET Requests
	 */

    @Test
	public void testTestClassesWithOneTestReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(1);
		//Build Http query parameters

        Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/testclasses");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		String expectedJson = generateExpectedJSON(mockInputRunResults, false);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testTestClassesWithTenTestReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(10);
		//Build Http query parameters

        Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/testclasses");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
		String expectedJson = generateExpectedJSON(mockInputRunResults, true);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("application/json");
	}

    @Test
	public void testTestClassesWithTenTestsWithSortDescendingReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(10);
		//Build Http query parameters

        Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		parameterMap.put("sort", new String[] {"testclass:desc"});
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/testclasses");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		String expectedJson = generateExpectedJSON(mockInputRunResults, true);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("application/json");
	}

    @Test
	public void testTestClassesWithTenTestsFiveResultsWithSortAscendingReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(10);
		//Build Http query parameters

        Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		parameterMap.put("sort", new String[] {"testclass:asc"});
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/testclasses");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		String expectedJson = generateExpectedJSON(mockInputRunResults, false);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("application/json");
	}

    @Test
	public void testTestClassesWithBadSortReturnsError() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(10);
		//Build Http query parameters

        Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		parameterMap.put("sort", new String[] {"testclass:jindex"});
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/testclasses");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
        //[
        //  GAL5011E: Error parsing the query parameters. sort value 'resultnames' not recognised.
		//  Expected query parameter in the format sort={fieldName}:{order} where order is asc for ascending or desc for descending.
        //]
		checkErrorStructure(
			outStream.toString(),
			5011,
			"GAL5011E: ",
			"testclass"
		);
	}

	@Test
	public void testTestClassesWithNoResultsReturnsError() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = new ArrayList<IRunResult>();
		// Build the results the DB will return.
			String runId = RandomStringUtils.randomAlphanumeric(16);
			TestStructure testStructure = new TestStructure();
			testStructure.setBundle("ForceException");
			testStructure.setTestName("ForceException");
			IRunResult result = new MockRunResult( runId, testStructure, null , null);
			mockInputRunResults.add(result);
		//Build Http query parameters

        Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/testclasses");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
        //[
        //  GAL5011E: Error parsing the query parameters. sort value 'resultnames' not recognised.
		//  Expected query parameter in the format sort={fieldName}:{order} where order is asc for ascending or desc for descending.
        //]
		checkErrorStructure(
			outStream.toString(),
			5000,
			"GAL5000E: ",
			"Error occurred when trying to access the endpoint. Report the problem to your Galasa Ecosystem owner."
		);
	}

	@Test
	public void testTestClassesWithZeroTestsReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(0);
		//Build Http query parameters

        Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/testclasses");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		// Expecting:
        //[
		//  "EnvFail",
		//  "Failed",
		//  "Ignored",
        //  "Passed"
        //]
		String expectedJson = generateExpectedJSON(mockInputRunResults, false);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testTestClassesWithTenTestAcceptHeaderReturnsOK() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(10);
		//Build Http query parameters
		Map<String, String> headerMap = new HashMap<String,String>();
		headerMap.put("Accept","application/json");
        Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/testclasses", headerMap);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment( mockInputRunResults,mockRequest);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		//Then...
		String expectedJson = generateExpectedJSON(mockInputRunResults, true);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("application/json");
	}
}
