/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal.routes;

import static org.assertj.core.api.Assertions.*;
import static dev.galasa.framework.spi.rbac.BuiltInAction.*;

import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.junit.Test;

import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.api.cps.internal.CpsServletTest;
import dev.galasa.framework.api.cps.internal.mocks.MockCpsServlet;
import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.rbac.Action;

public class TestAddPropertyInNamespaceRoute extends CpsServletTest {

    /*
     * Regex Path
     */

    @Test
    public void TestPathRegexExpectedPathReturnsTrue(){
        //Given...
        String expectedPath = AddPropertyInNamespaceRoute.path;
        String inputPath = "/namespace/name/property/property.name";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isTrue();
    }

    @Test
    public void TestPathRegexExpectedPathWithAtSymbolReturnsTrue(){
        //Given...
        String expectedPath = AddPropertyInNamespaceRoute.path;
        String inputPath = "/namespace/name/property/GalasaDelivery@ibm.com";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isTrue();
    }

    @Test
    public void TestPathRegexExpectedPathWithTrailingSlashReturnsTrue(){
        //Given...
        String expectedPath = AddPropertyInNamespaceRoute.path;
        String inputPath = "/namespace/name/property/property.name/";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isTrue();
    }

    @Test
    public void TestPathRegexNoSuffixInPathReturnsFalse(){
        //Given...
        String expectedPath = AddPropertyInNamespaceRoute.path;
        String inputPath = "/namespace/name/prefix/prop/suffix";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexIncompletePathReturnsFalse(){
        //Given...
        String expectedPath = AddPropertyInNamespaceRoute.path;
        String inputPath = "/namespace/name/";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

	@Test
    public void TestPathRegexHalfPathReturnsFalse(){
        //Given...
        String expectedPath = AddPropertyInNamespaceRoute.path;
        String inputPath = "/namespace/";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexLowerCasePathReturnsFalse(){
        //Given...
        String expectedPath = AddPropertyInNamespaceRoute.path;
        String inputPath = "/thisisapath";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexUpperCasePathReturnsFalse(){
        //Given...
        String expectedPath = AddPropertyInNamespaceRoute.path;
        String inputPath = "/ALLCAPITALS";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexNumberPathReturnsFalse(){
        //Given...
        String expectedPath = AddPropertyInNamespaceRoute.path;
        String inputPath = "/1234";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexUnexpectedPathReturnsTrue(){
        //Given...
        String expectedPath = AddPropertyInNamespaceRoute.path;
        String inputPath = "/incorrect-?ID_1234";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexEmptyPathReturnsFalse(){
        //Given...
        String expectedPath = AddPropertyInNamespaceRoute.path;
        String inputPath = "";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexDotPathReturnsFalse(){
        //Given...
        String expectedPath = AddPropertyInNamespaceRoute.path;
        String inputPath = "/random.String";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexSpecialCharacterPathReturnsFalse(){
        //Given...
        String expectedPath = AddPropertyInNamespaceRoute.path;
        String inputPath = "/?";

        //When...
        boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

        //Then...
        assertThat(matches).isFalse();
    }

    @Test
    public void TestPathRegexMultipleForwardSlashPathReturnsFalse(){
        //Given...
        String expectedPath = AddPropertyInNamespaceRoute.path;
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
	public void TestGetNamespacesWithFrameworkWithDataReturnsOk() throws Exception{
		// Given...
		setServlet("/namespace/framework/property/property.456","framework",new HashMap<String,String[]>());
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	

		// When...
		servlet.init();
		servlet.doGet(req,resp);
	
		// Then...
		assertThat(resp.getStatus()).isEqualTo(405);
		assertThat(resp.getContentType()).isEqualTo("application/json");
		checkErrorStructure(
			outStream.toString(),
			5405,
			"E: Error occurred when trying to access the endpoint '/namespace/framework/property/property.456'. The method 'GET' is not allowed."
		);
	}

    /*
	 * TEST - HANDLE PUT REQUEST - should error as this method is not supported by this API end-point
	 */

    @Test
    public void TestGetNamespacesPUTRequestWithAcceptHeaderReturnsOK() throws Exception{
        // Given...
        String namespace = "framework";
        String propertyName = "property.6";
        String value = "value6";
        String json = "{\"name\":\""+propertyName+"\", \"value\":\""+value+"\"}";
        Map<String, String> headerMap = new HashMap<String,String>();
        headerMap.put("Accept", "application/json");
        MockIConfigurationPropertyStoreService store = new MockIConfigurationPropertyStoreService(namespace){
            @Override
            public @Null String getProperty(@NotNull String prefix, @NotNull String suffix, String... infixes)
            throws ConfigurationPropertyStoreException {
            for (Map.Entry<String,String> property : properties.entrySet()){
                String key = property.getKey();
                String match = prefix+"."+suffix;
                if (key.contains(match)){
                    return property.getValue();
                }
            }
            return null;
            }
        };
        setServlet("/namespace/framework/property/"+propertyName, namespace, json, "PUT", store, headerMap);
        MockCpsServlet servlet = getServlet();
        HttpServletRequest req = getRequest();
        HttpServletResponse resp = getResponse();
        ServletOutputStream outStream = resp.getOutputStream();	
                
        // When...
        servlet.init();
        servlet.doPut(req,resp);

        // Then...
        Integer status = resp.getStatus();
        String output = outStream.toString();
        assertThat(status).isEqualTo(200);
        assertThat(resp.getContentType()).isEqualTo("application/json");
        assertThat(output).isEqualTo("{\n  \"name\": \""+propertyName+"\",\n  \"value\": \""+value+"\"\n}");
        assertThat(checkNewPropertyInNamespace(namespace, propertyName, value)).isTrue();      
    }

	@Test
	public void TestGetNamespacesPUTRequestReturnsOK() throws Exception{
		// Given...
        String namespace = "framework";
        String propertyName = "property.6";
        String value = "value6";
        String json = "{\"name\":\""+propertyName+"\", \"value\":\""+value+"\"}";
        MockIConfigurationPropertyStoreService store = new MockIConfigurationPropertyStoreService(namespace){
            @Override
            public @Null String getProperty(@NotNull String prefix, @NotNull String suffix, String... infixes)
            throws ConfigurationPropertyStoreException {
            for (Map.Entry<String,String> property : properties.entrySet()){
                String key = property.getKey();
                String match = prefix+"."+suffix;
                if (key.contains(match)){
                    return property.getValue();
                }
            }
            return null;
            }
        };
		setServlet("/namespace/framework/property/"+propertyName, namespace, json, "PUT", store);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doPut(req,resp);

		// Then...
        Integer status = resp.getStatus();
        String output = outStream.toString();
        assertThat(status).isEqualTo(200);
        assertThat(resp.getContentType()).isEqualTo("application/json");
        assertThat(output).isEqualTo("{\n  \"name\": \""+propertyName+"\",\n  \"value\": \""+value+"\"\n}");
        assertThat(checkNewPropertyInNamespace(namespace, propertyName, value)).isTrue();      
    }

    @Test
	public void TestCreatePropertyWithMissingPermissionsReturnsForbiddenError() throws Exception{
		// Given...
        String namespace = "framework";
        String propertyName = "property.6";
        String value = "value6";
        String json = "{\"name\":\""+propertyName+"\", \"value\":\""+value+"\"}";
        MockIConfigurationPropertyStoreService store = new MockIConfigurationPropertyStoreService(namespace);

        List<Action> actions = List.of(GENERAL_API_ACCESS.getAction());
        MockRBACService rbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME, actions);

        MockFramework mockFramework = new MockFramework(store);
        mockFramework.setRBACService(rbacService);

		setServlet("/namespace/framework/property/"+propertyName, namespace, json, "PUT", mockFramework);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doPut(req,resp);

		// Then...
        assertThat(resp.getStatus()).isEqualTo(403);
        assertThat(resp.getContentType()).isEqualTo("application/json");
        checkErrorStructure(outStream.toString(), 5125, "GAL5125E", "CPS_PROPERTIES_SET");
    }

    @Test
	public void TestGetPropertyWithAtSymbolPUTRequestReturnsOK() throws Exception{
		// Given...
        String namespace = "framework";
        String propertyName = "Galasadelivery@ibm.com";
        String value = "value6";
        String json = "{\"name\":\""+propertyName+"\", \"value\":\""+value+"\"}";
        MockIConfigurationPropertyStoreService store = new MockIConfigurationPropertyStoreService(namespace){
            @Override
            public @Null String getProperty(@NotNull String prefix, @NotNull String suffix, String... infixes)
            throws ConfigurationPropertyStoreException {
            for (Map.Entry<String,String> property : properties.entrySet()){
                String key = property.getKey();
                String match = prefix+"."+suffix;
                if (key.contains(match)){
                    return property.getValue();
                }
            }
            return null;
            }
        };
		setServlet("/namespace/framework/property/"+propertyName, namespace, json, "PUT", store);
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doPut(req,resp);

		// Then...
        Integer status = resp.getStatus();
        String output = outStream.toString();
        assertThat(status).isEqualTo(200);
        assertThat(resp.getContentType()).isEqualTo("application/json");
        assertThat(output).isEqualTo("{\n  \"name\": \""+propertyName+"\",\n  \"value\": \""+value+"\"\n}");
        assertThat(checkNewPropertyInNamespace(namespace, propertyName, value)).isTrue();      
    }

    @Test
	public void TestPropertyNamrMismatchPUTRequestReturnsError() throws Exception{
		// Given...
        String namespace = "framework";
        String propertyName = "property.6";
        String value = "value6";
        String json = "{\"name\":\""+propertyName+"\", \"value\":\""+value+"\"}";
		setServlet("/namespace/framework/property/property.2", namespace, json, "PUT");
		MockCpsServlet servlet = getServlet();
		HttpServletRequest req = getRequest();
		HttpServletResponse resp = getResponse();
		ServletOutputStream outStream = resp.getOutputStream();	
				
		// When...
		servlet.init();
		servlet.doPut(req,resp);

		// Then...
        Integer status = resp.getStatus();
        assertThat(status).isEqualTo(404);
        assertThat(resp.getContentType()).isEqualTo("application/json");
        checkErrorStructure(
			outStream.toString(),
			5029,
			"E: The GalasaProperty name 'property.6' must match the url namespace 'property.2'."
		); 
    }

	/*
	 * TEST - HANDLE POST REQUEST - should error as this method is not supported by this API end-point
	 */
	@Test
	public void TestGetNamespacesPOSTRequestReturnsError() throws Exception{
		// Given...
		setServlet("/namespace/framework/property/property.456","framework",null, "POST");
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
			"E: Error occurred when trying to access the endpoint '/namespace/framework/property/property.456'. The method 'POST' is not allowed."
		);
    }

	/*
	 * TEST - HANDLE DELETE REQUEST - should error as this method is not supported by this API end-point
	 */
	@Test
	public void TestGetNamespacesDELETERequestReturnsError() throws Exception{
		// Given...
		setServlet("/namespace/framework/property/property.456","framework",null, "DELETE");
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
			"E: Error occurred when trying to access the endpoint '/namespace/framework/property/property.456'. The method 'DELETE' is not allowed."
		);
    }

}
