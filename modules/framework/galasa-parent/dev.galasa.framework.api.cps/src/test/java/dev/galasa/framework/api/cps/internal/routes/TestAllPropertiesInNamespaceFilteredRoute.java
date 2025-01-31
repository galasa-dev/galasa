/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import dev.galasa.framework.api.common.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.api.cps.internal.CpsServletTest;
import dev.galasa.framework.api.cps.internal.mocks.MockCpsServlet;

public class TestAllPropertiesInNamespaceFilteredRoute extends CpsServletTest {

    /*
     * Regex Path
     */

    @Test
    public void testPathRegexExpectedPathReturnsTrue(){
        //Given...
        String expectedPath = AllPropertiesInNamespaceFilteredRoute.path;
        String inputPath = "/namespace/name/prefix/prop/suffix/erty";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isTrue();
    }

    @Test
    public void testPathRegexExpectedPathWithTrailingSlashReturnsTrue(){
        //Given...
        String expectedPath = AllPropertiesInNamespaceFilteredRoute.path;
        String inputPath = "/namespace/name/prefix/prop/suffix/erty/";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isTrue();
    }

	@Test
    public void testPathRegexExpectedPathWithAtSymbolReturnsTrue(){
        //Given...
        String expectedPath = AllPropertiesInNamespaceFilteredRoute.path;
        String inputPath = "/namespace/name/prefix/Galasadelivery@ibm.com/suffix/erty/";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isTrue();
    }

    @Test
    public void testPathRegexNoSuffixInPathReturnsFalse(){
        //Given...
        String expectedPath = AllPropertiesInNamespaceFilteredRoute.path;
        String inputPath = "/namespace/name/prefix/prop/suffix";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void testPathRegexIncompletePathReturnsFalse(){
        //Given...
        String expectedPath = AllPropertiesInNamespaceFilteredRoute.path;
        String inputPath = "/namespace/name/";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

	@Test
    public void testPathRegexHalfPathReturnsFalse(){
        //Given...
        String expectedPath = AllPropertiesInNamespaceFilteredRoute.path;
        String inputPath = "/namespace/";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void testPathRegexLowerCasePathReturnsFalse(){
        //Given...
        String expectedPath = AllPropertiesInNamespaceFilteredRoute.path;
        String inputPath = "/thisisapath";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void testPathRegexUpperCasePathReturnsFalse(){
        //Given...
        String expectedPath = AllPropertiesInNamespaceFilteredRoute.path;
        String inputPath = "/ALLCAPITALS";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void testPathRegexNumberPathReturnsFalse(){
        //Given...
        String expectedPath = AllPropertiesInNamespaceFilteredRoute.path;
        String inputPath = "/1234";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void testPathRegexUnexpectedPathReturnsTrue(){
        //Given...
        String expectedPath = AllPropertiesInNamespaceFilteredRoute.path;
        String inputPath = "/incorrect-?ID_1234";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void testPathRegexEmptyPathReturnsFalse(){
        //Given...
        String expectedPath = AllPropertiesInNamespaceFilteredRoute.path;
        String inputPath = "";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void testPathRegexDotPathReturnsFalse(){
        //Given...
        String expectedPath = AllPropertiesInNamespaceFilteredRoute.path;
        String inputPath = "/random.String";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void testPathRegexSpecialCharacterPathReturnsFalse(){
        //Given...
        String expectedPath = AllPropertiesInNamespaceFilteredRoute.path;
        String inputPath = "/?";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void testPathRegexMultipleForwardSlashPathReturnsFalse(){
        //Given...
        String expectedPath = AllPropertiesInNamespaceFilteredRoute.path;
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
	public void testGetNamespacesWithFrameworkNoDataReturnsNotFound() throws Exception{
		// Given...
		setServlet("/namespace/framework/prefix/prop/suffix/erty","empty",new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	

		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(404);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		checkErrorStructure(
			outStream.toString(),
			5016,
			": Error occurred when trying to access namespace 'framework'. The namespace provided is invalid."
		);
    }

	@Test
	public void testGetNamespacesWithFrameworkWithDataNoMatchReturnsOk() throws Exception{
		// Given...
		setServlet("/namespace/framework/prefix/prop/suffix/erty","framework",new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	

		// When...
		servlet.init();
		servlet.doGet(req,resp);
	
		// Then...
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(outStream.toString()).isEqualTo("{}");
	}

	@Test
	public void testGetNamespacesWithFrameworkWithDataNoMatchAcceptHeaderReturnsOk() throws Exception{
		// Given...
		Map<String, String> headerMap = new HashMap<String,String>();
        headerMap.put("Accept", "application/json");
		setServlet("/namespace/framework/prefix/prop/suffix/erty","framework",null, "GET", new MockIConfigurationPropertyStoreService("framework"), headerMap);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	

		// When...
		servlet.init();
		servlet.doGet(req,resp);
	
		// Then...
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(outStream.toString()).isEqualTo("{}");
	}

    @Test
	public void testGetNamespacesWithFrameworkWithDataReturnsValue() throws Exception{
		// Given...
		setServlet("/namespace/framework/prefix/property/suffix/1","framework",new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	

		// When...
		servlet.init();
		servlet.doGet(req,resp);
	
		// Then...
		assertThat(resp.getStatus()).isEqualTo(200);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		assertThat(outStream.toString()).isEqualTo("{\n  \"name\": \"property.1\",\n  \"value\": \"value1\"\n}");
	}
   
    @Test
	public void testGetNamespacesWithFrameworkBadPathReturnsError() throws Exception{
		// Given...
		setServlet(".","framework",new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// We expect an error back, because the API server has thrown a ConfigurationPropertyStoreException
		assertThat(resp.getStatus()).isEqualTo(404);
		assertThat(resp.getContentType()).isEqualTo("application/json");

		checkErrorStructure(
			outStream.toString(),
			5404,
			"E: Error occurred when trying to identify the endpoint '.'."
		);
    }

    /*
	 * TEST - HANDLE PUT REQUEST - should error as this method is not supported by this API end-point
	 */
	@Test
	public void testGetNamespacesPUTRequestReturnsError() throws Exception{
		// Given...
		setServlet("/namespace/framework/prefix/prop/suffix/erty","framework", null , "PUT");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doPut(req,resp);

		// Then...
		// We expect an error back, because the API server has thrown a ConfigurationPropertyStoreException
		assertThat(resp.getStatus()).isEqualTo(405);
		assertThat(resp.getContentType()).isEqualTo("application/json");

		checkErrorStructure(
			outStream.toString(),
			5405,
			"E: Error occurred when trying to access the endpoint '/namespace/framework/prefix/prop/suffix/erty'. The method 'PUT' is not allowed."
		);
    }

	/*
	 * TEST - HANDLE POST REQUEST - should error as this method is not supported by this API end-point
	 */
	@Test
	public void testGetNamespacesPOSTRequestReturnsError() throws Exception{
		// Given...
		setServlet("/namespace/framework/prefix/prop/suffix/erty","framework",null, "POST");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doPost(req,resp);

		// Then...
		// We expect an error back, because the API server has thrown a ConfigurationPropertyStoreException
		assertThat(resp.getStatus()).isEqualTo(405);
		assertThat(resp.getContentType()).isEqualTo("application/json");

		checkErrorStructure(
			outStream.toString(),
			5405,
			"E: Error occurred when trying to access the endpoint '/namespace/framework/prefix/prop/suffix/erty'. The method 'POST' is not allowed."
		);
    }

	/*
	 * TEST - HANDLE DELETE REQUEST - should error as this method is not supported by this API end-point
	 */
	@Test
	public void testGetNamespacesDELETERequestReturnsError() throws Exception{
		// Given...
		setServlet("/namespace/framework/prefix/prop/suffix/erty","framework",null, "DELETE");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doDelete(req,resp);

		// Then...
		// We expect an error back, because the API server has thrown a ConfigurationPropertyStoreException
		assertThat(resp.getStatus()).isEqualTo(405);
		assertThat(resp.getContentType()).isEqualTo("application/json");

		checkErrorStructure(
			outStream.toString(),
			5405,
			"E: Error occurred when trying to access the endpoint '/namespace/framework/prefix/prop/suffix/erty'. The method 'DELETE' is not allowed."
		);
    }

}
