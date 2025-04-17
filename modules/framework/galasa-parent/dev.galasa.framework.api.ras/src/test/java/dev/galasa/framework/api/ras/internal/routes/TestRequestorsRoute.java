/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.teststructure.TestStructure;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.RandomStringUtils;

import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.ras.internal.RasServlet;
import dev.galasa.framework.api.ras.internal.RasServletTest;
import dev.galasa.framework.api.ras.internal.mocks.*;
import dev.galasa.framework.mocks.MockRunResult;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import java.util.regex.Pattern;

import dev.galasa.framework.spi.utils.GalasaGson;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestRequestorsRoute extends RasServletTest{

    static final GalasaGson gson = new GalasaGson();

    public List<IRunResult> generateTestData (int resSize){
		List<IRunResult> mockInputRunResults = new ArrayList<IRunResult>();
		// Build the results the DB will return.
		for(int c =0 ; c < resSize; c++){
			String runId = RandomStringUtils.insecure().nextAlphanumeric(16);
			TestStructure testStructure = new TestStructure();
			switch (c % 5){
                case 0: testStructure.setRequestor("galasa");
					break;
				case 1: testStructure.setRequestor("mickey");
					break;
				case 2: testStructure.setRequestor("user");
					break;
                case 3: testStructure.setRequestor("UNKNOWN");
					break;
                case 4: testStructure.setRequestor("jindex");
					break;
			}
			IRunResult result = new MockRunResult( runId, testStructure, null , null);
			mockInputRunResults.add(result);
		}
		return mockInputRunResults;
	}

    private String generateExpectedJSON (List<IRunResult> mockInputRunResults, boolean reverse) throws ResultArchiveStoreException{
        List<String> requestors = new ArrayList<>();
		for (IRunResult run : mockInputRunResults){
            String result  = run.getTestStructure().getRequestor().toString();
            if (!requestors.contains(result)){
                requestors.add(result);
            }
		}
        Collections.sort(requestors);
        if (reverse == true) {
            Collections.reverse(requestors);
        }
		JsonElement jsonResultsArray = gson.toJsonTree(requestors);
		JsonObject json = new JsonObject();
		json.add("requestors", jsonResultsArray);
		return json.toString();
    }

    /*
     * Tests 
     */

	/*
     * Regex Path
     */

	@Test
	public void testPathRegexExpectedPathReturnsTrue(){
		//Given...
		String expectedPath = RequestorRoute.path;
		String inputPath = "/requestors";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void testPathRegexExpectedPathWithQueryReturnsFalse(){
		//Given...
		String expectedPath = RequestorRoute.path;
		String inputPath = "/requestors?requestor=Mickey";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void testPathRegexExpectedPathWithNumbersReturnsFalse(){
		//Given...
		String expectedPath = RequestorRoute.path;
		String inputPath = "/request0rs";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void testPathRegexLowerCasePathReturnsTrue(){
		//Given...
		String expectedPath = RequestorRoute.path;
		String inputPath = "/requestors";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}
	
	@Test
	public void testPathRegexExpectedPathWithCapitalLeadingLetterReturnsFalse(){
		//Given...
		String expectedPath = RequestorRoute.path;
		String inputPath = "/Requestors";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}
	
	@Test
	public void testPathRegexUpperCasePathReturnsFalse(){
		//Given...
		String expectedPath = RequestorRoute.path;
		String inputPath = "/REQUESTORS";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}
 
	 @Test
	 public void testPathRegexExpectedPathWithLeadingNumberReturnsFalse(){
		 //Given...
		 String expectedPath = RequestorRoute.path;
		 String inputPath = "/0rewustors";
 
		 //When...
		 boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();
 
		 //Then...
		 assertThat(matches).isFalse();
	 }
 
	 @Test
	 public void testPathRegexExpectedPathWithTrailingForwardSlashReturnsTrue(){
		 //Given...
		 String expectedPath = RequestorRoute.path;
		 String inputPath = "/requestors/";
 
		 //When...
		 boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();
 
		 //Then...
		 assertThat(matches).isTrue();
	 }
 
	 @Test
	 public void testPathRegexNumberPathReturnsFalse(){
		 //Given...
		 String expectedPath = RequestorRoute.path;
		 String inputPath = "/requestors1234";
 
		 //When...
		 boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();
 
		 //Then...
		 assertThat(matches).isFalse();
	 }
 
	 @Test
	 public void testPathRegexUnexpectedPathReturnsFalse(){
		 //Given...
		 String expectedPath = RequestorRoute.path;
		 String inputPath = "/requestor";
 
		 //When...
		 boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();
 
		 //Then...
		 assertThat(matches).isFalse();
	 }
 
	 @Test
	 public void testPathRegexEmptyPathReturnsFalse(){
		 //Given...
		 String expectedPath = RequestorRoute.path;
		 String inputPath = "";
 
		 //When...
		 boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();
 
		 //Then...
		 assertThat(matches).isFalse();
	 }
 
	 @Test
	 public void testPathRegexSpecialCharacterPathReturnsFalse(){
		 //Given...
		 String expectedPath = RequestorRoute.path;
		 String inputPath = "/requestors/?";
 
		 //When...
		 boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();
 
		 //Then...
		 assertThat(matches).isFalse();
	 }
 
	 @Test
	 public void testPathRegexMultipleForwardSlashPathReturnsFalse(){
		 //Given...
		 String expectedPath = RequestorRoute.path;
		 String inputPath = "/requestors//////";
 
		 //When...
		 boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();
 
		 //Then...
		 assertThat(matches).isFalse();
	 } 

	/*
	 * Tests - GET Requests
	 */

    @Test
	public void testRequestorsWithOneTestNoURLQueryReturnsSingleResult() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(1);
		//Build Http query parameters

        Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/requestors");
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
        System.out.println(expectedJson);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("application/json");
	}

    @Test
	public void testRequestorsWithFiveTestNoURLQueryReturnsFiveResult() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(5);
		//Build Http query parameters

        Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/requestors");
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
        System.out.println(expectedJson);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("application/json");
	}

    @Test
	public void testRequestorsWithTenTestNoURLQueryReturnsFiveResult() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(10);
		//Build Http query parameters

        Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/requestors");
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
        System.out.println(expectedJson);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("application/json");
	}

    @Test
	public void testRequestorsWithFiveTestSortAscReturnsFiveResult() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(5);
		//Build Http query parameters

        Map<String, String[]> parameterMap = new HashMap<String,String[]>();
        parameterMap.put("sort", new String[] {"requestor:asc"});
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/requestors");
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
        System.out.println(expectedJson);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("application/json");
	}

    @Test
	public void testRequestorsWithTenTestSortAscReturnsFiveResult() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(10);
		//Build Http query parameters

        Map<String, String[]> parameterMap = new HashMap<String,String[]>();
        parameterMap.put("sort", new String[] {"requestor:asc"});
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/requestors");
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
        System.out.println(expectedJson);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("application/json");
	}

    @Test
	public void testRequestorsWithFiveTestSortDescReturnsFiveResult() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(5);
		//Build Http query parameters

        Map<String, String[]> parameterMap = new HashMap<String,String[]>();
        parameterMap.put("sort", new String[] {"requestor:desc"});
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/requestors");
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
        System.out.println(expectedJson);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("application/json");
	}

    @Test
	public void testRequestorsWithTenTestSortDescReturnsFiveResult() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(10);
		//Build Http query parameters

        Map<String, String[]> parameterMap = new HashMap<String,String[]>();
        parameterMap.put("sort", new String[] {"requestor:desc"});
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/requestors");
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
        System.out.println(expectedJson);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testRequestorsWithAcceptHeaderTenTestSortDescReturnsFiveResult() throws Exception {
		//Given..
		List<IRunResult> mockInputRunResults = generateTestData(10);
		//Build Http query parameters

        Map<String, String[]> parameterMap = new HashMap<String,String[]>();
        parameterMap.put("sort", new String[] {"requestor:desc"});

		Map<String, String> headerMap = new HashMap<String,String>();
        headerMap.put("Accept", "application/json");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/requestors", headerMap);
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
        System.out.println(expectedJson);
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat( outStream.toString() ).isEqualTo(expectedJson);
		assertThat( resp.getContentType()).isEqualTo("application/json");
	}
}