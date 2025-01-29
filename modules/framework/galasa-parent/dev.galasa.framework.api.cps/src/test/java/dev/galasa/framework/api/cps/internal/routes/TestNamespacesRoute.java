/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;
import dev.galasa.framework.api.common.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.api.cps.internal.CpsServletTest;
import dev.galasa.framework.api.cps.internal.mocks.MockCpsServlet;
import dev.galasa.framework.api.cps.internal.routes.TestNamespacesRoute;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

public class TestNamespacesRoute extends CpsServletTest{

	/*
     * Regex Path
     */

    @Test
    public void TestPathRegexExpectedPathReturnsTrue(){
        //Given...
        String expectedPath = NamespacesRoute.path;
        String inputPath = "/";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isTrue();
    }

    @Test
    public void TestPathRegexLowerCasePathReturnsFalse(){
        //Given...
        String expectedPath = NamespacesRoute.path;
        String inputPath = "/thisisapath";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexUpperCasePathReturnsFalse(){
        //Given...
        String expectedPath = NamespacesRoute.path;
        String inputPath = "/ALLCAPITALS";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexNumberPathReturnsFalse(){
        //Given...
        String expectedPath = NamespacesRoute.path;
        String inputPath = "/1234";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexUnexpectedPathReturnsTrue(){
        //Given...
        String expectedPath = NamespacesRoute.path;
        String inputPath = "/incorrect-?ID_1234";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexEmptyPathReturnsTrue(){
        //Given...
        String expectedPath = NamespacesRoute.path;
        String inputPath = "";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isTrue();
    }

    @Test
    public void TestPathRegexDotPathReturnsFalse(){
        //Given...
        String expectedPath = NamespacesRoute.path;
        String inputPath = "/random.String";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexSpecialCharacterPathReturnsFalse(){
        //Given...
        String expectedPath = NamespacesRoute.path;
        String inputPath = "/?";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexMultipleForwardSlashPathReturnsFalse(){
        //Given...
        String expectedPath = NamespacesRoute.path;
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
	public void TestGetNamespacesWithFrameworkNoDataReturnsDefaults() throws Exception{
		// Given...
		setServlet("/","empty",new HashMap<String,String[]>());
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
		assertThat(outStream.toString()).isEqualTo("[\n"+
		"  {\n    \"name\": \"framework\",\n    \"propertiesUrl\": \"/framework/properties\",\n    \"type\": \"NORMAL\"\n  },\n"+
		"  {\n    \"name\": \"secure\",\n    \"propertiesUrl\": \"/secure/properties\",\n    \"type\": \"SECURE\"\n  }"+
		"\n]");	}

	@Test
	public void TestGetNamespacesWithFrameworkWithDataReturnsOk() throws Exception{
		// Given...
		setServlet("/","framework",new HashMap<String,String[]>());
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
		assertThat(outStream.toString()).isEqualTo("[\n"+
		"  {\n    \"name\": \"framework\",\n    \"propertiesUrl\": \"/framework/properties\",\n    \"type\": \"NORMAL\"\n  },\n"+
		"  {\n    \"name\": \"secure\",\n    \"propertiesUrl\": \"/secure/properties\",\n    \"type\": \"SECURE\"\n  }"+
		"\n]");
	}

	@Test
	public void TestGetNamespacesWithFrameworkWithDataAcceptHeaderReturnsOk() throws Exception{
		// Given...
		Map<String, String> headerMap = new HashMap<String,String>();
        headerMap.put("Accept", "application/json");
		setServlet("/","framework",null, "GET", new MockIConfigurationPropertyStoreService("framework"), headerMap);
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
		assertThat(outStream.toString()).isEqualTo("[\n"+
		"  {\n    \"name\": \"framework\",\n    \"propertiesUrl\": \"/framework/properties\",\n    \"type\": \"NORMAL\"\n  },\n"+
		"  {\n    \"name\": \"secure\",\n    \"propertiesUrl\": \"/secure/properties\",\n    \"type\": \"SECURE\"\n  }"+
		"\n]");
	}

	@Test
	public void TestGetNamespacesWithFrameworkWithDataEmptyPathReturnsOk() throws Exception{
		// Given...
		setServlet("","framework",new HashMap<String,String[]>());
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
		assertThat(outStream.toString()).isEqualTo("[\n"+
		"  {\n    \"name\": \"framework\",\n    \"propertiesUrl\": \"/framework/properties\",\n    \"type\": \"NORMAL\"\n  },\n"+
		"  {\n    \"name\": \"secure\",\n    \"propertiesUrl\": \"/secure/properties\",\n    \"type\": \"SECURE\"\n  }"+
		"\n]");
	}

	@Test
	public void TestGetNamespacesWithFrameworkNullNamespacesReturnsError() throws Exception{
		// Given...
		setServlet("/","error",new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// We expect an error back, because the API server has thrown a ConfigurationPropertyStoreException
		assertThat(resp.getStatus()).isEqualTo(500);
		assertThat(resp.getContentType()).isEqualTo("application/json");

		checkErrorStructure(
			outStream.toString(),
			5015,
			"E: Error occurred when trying to access the Configuration Property Store.",
			" Report the problem to your Galasa Ecosystem owner."
		);
    }

	@Test
	public void TestGetNamespacesWithFrameworkBadPathReturnsError() throws Exception{
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

	@Test
	public void TestGetNamespacesWithFrameworkBadPathWithSlashReturnsError() throws Exception{
		// Given...
		setServlet("/.","framework",new HashMap<String,String[]>());
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
			"E: Error occurred when trying to identify the endpoint '/.'."
		);
    }

	/*
	 * TEST - HANDLE PUT REQUEST - should error as this method is not supported by this API end-point
	 */
	@Test
	public void TestGetNamespacesPUTRequestReturnsError() throws Exception{
		// Given...
		setServlet("/","framework", null , "PUT");
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
			"E: Error occurred when trying to access the endpoint '/'. The method 'PUT' is not allowed."
		);
    }

	/*
	 * TEST - HANDLE POST REQUEST - should error as this method is not supported by this API end-point
	 */
	@Test
	public void TestGetNamespacesPOSTRequestReturnsError() throws Exception{
		// Given...
		setServlet("/","framework",null, "POST");
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
			"E: Error occurred when trying to access the endpoint '/'. The method 'POST' is not allowed."
		);
    }

	/*
	 * TEST - HANDLE DELETE REQUEST - should error as this method is not supported by this API end-point
	 */
	@Test
	public void TestGetNamespacesDELETERequestReturnsError() throws Exception{
		// Given...
		setServlet("/","framework",null, "DELETE");
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
			"E: Error occurred when trying to access the endpoint '/'. The method 'DELETE' is not allowed."
		);
    }
}