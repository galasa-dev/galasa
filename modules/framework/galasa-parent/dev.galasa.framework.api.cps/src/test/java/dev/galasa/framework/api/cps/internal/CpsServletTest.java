/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import dev.galasa.framework.api.cps.internal.mocks.MockCpsServlet;
import dev.galasa.framework.api.beans.GalasaProperty;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.api.common.mocks.MockServletOutputStream;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGson;

import static org.assertj.core.api.Assertions.*;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CpsServletTest extends BaseServletTest {

	static final GalasaGson gson = new GalasaGson();

	MockCpsServlet servlet;
	HttpServletRequest req;
	HttpServletResponse resp;

	protected void setServlet(String namespace){
		this.servlet = new MockCpsServlet();
        servlet.setResponseBuilder(new ResponseBuilder(new MockEnvironment()));

		if (namespace != null){
			IConfigurationPropertyStoreService cpsstore = new MockIConfigurationPropertyStoreService(namespace);
			IFramework framework = new MockFramework(cpsstore);
			this.servlet.setFramework(framework);
		}
	}
	
	protected void setServlet(String path,String namespace, Map<String, String[]> parameterMap){
		setServlet(namespace);
		ServletOutputStream outStream = new MockServletOutputStream();
		PrintWriter writer = new PrintWriter(outStream);
		this.req = new MockHttpServletRequest(parameterMap,path);
		this.resp = new MockHttpServletResponse(writer, outStream);
	}

	protected void setServlet( String path,String namespace, String value, String method){
		setServlet(namespace);
		ServletOutputStream outStream = new MockServletOutputStream();
        PrintWriter writer = new PrintWriter(outStream);
		this.req = new MockHttpServletRequest(path, value, method);
		this.resp = new MockHttpServletResponse(writer, outStream);
	}

	protected void setServlet( String path,String namespace, String value, String method, MockIConfigurationPropertyStoreService store){
		setServlet(namespace);
		ServletOutputStream outStream = new MockServletOutputStream();
        PrintWriter writer = new PrintWriter(outStream);
		IFramework framework = new MockFramework(store);
		this.servlet.setFramework(framework);
		this.req = new MockHttpServletRequest(path, value, method);
		this.resp = new MockHttpServletResponse(writer, outStream);
	}
	protected void setServlet( String path,String namespace, String value, String method, MockIConfigurationPropertyStoreService store, Map<String,String> headerMap){
		setServlet(path,namespace, value, method, store);
		this.req = new MockHttpServletRequest(path, value, method, headerMap);
	}

	protected MockCpsServlet getServlet(){
		return this.servlet;
	}

	protected HttpServletRequest getRequest(){
		return this.req;
	}

	protected HttpServletResponse getResponse(){
	return this.resp;
	}

	@Override
	protected void checkJsonArrayStructure(String jsonString, Map<String, String> properties) throws Exception {

		JsonElement jsonElement = JsonParser.parseString(jsonString);
		assertThat(jsonElement).isNotNull().as("Failed to parse the body to a json object.");

		JsonArray jsonArray = jsonElement.getAsJsonArray();
		assertThat(jsonArray).isNotNull().as("Json parsed is not a json array.");

		List<GalasaProperty> expectedProperties = new ArrayList<GalasaProperty>();
		for (Entry<String, String> entry : properties.entrySet()) {
			String[] nameParts = entry.getKey().split("[.]", 2);
    	    String namespaceName = nameParts[0];
    		String propertyName = nameParts[1];
			expectedProperties.add( new GalasaProperty(namespaceName, propertyName,entry.getValue()));
		}

		List<GalasaProperty> jsonProperties = new ArrayList<GalasaProperty>();
		for (JsonElement element : jsonArray) {
			String expected = element.toString();
            jsonProperties.add(gson.fromJson(expected, GalasaProperty.class));
		}
		
        // Go through the list of expected Galasa Properties and 
		// json properties (which have been converted into Galasa Properties)
		// and check if any of the elements contain a matching key-value entry.
        for (GalasaProperty property : expectedProperties) {
            boolean fieldMatches = false;

            for (GalasaProperty returned : jsonProperties) {
                if ((property.getName().equals(returned.getName()))&&(property.getValue().equals(returned.getValue()))) {
                    fieldMatches = true;
                }
            }
            assertThat(fieldMatches).isTrue();
        }
    }

	protected boolean checkNewPropertyInNamespace(String namespace, String propertyName, String propertyValue) throws Exception{
		boolean found = false;
		Map<String,String> properties = this.servlet.getFramework().getConfigurationPropertyService(namespace).getAllProperties();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey().toString();
			String value =entry.getValue().toString();
            if (key.equals(propertyName) && value.equals(propertyValue)){
				found = true;
            }
        }
		return found;
	}

	protected String generatePropertyJSON(String namespace, String propertyName, String propertyValue, String apiVersion){
		return gson.toJson(new GalasaProperty(namespace, propertyName, propertyValue, apiVersion));
	}

	protected GalasaProperty generateProperty(String namespace, String propertyName, String propertyValue, String apiVersion){
		return new GalasaProperty(namespace, propertyName, propertyValue, apiVersion);
	}
	protected String generateExpectedJson(String namespace, String propertyName, String propertyValue, String apiVersion){
		List<GalasaProperty> results = new ArrayList<GalasaProperty>();
		results.add(generateProperty(namespace, propertyName, propertyValue, apiVersion));
        return gson.toJson(results);
    }

	protected String generateExpectedJson(Map<String, String> properties){
		List<GalasaProperty> results = new ArrayList<GalasaProperty>();
		for (Map.Entry<String,String> entry : properties.entrySet()){
			// Key Value namesapce.propertyname value value
			String[] splitName = entry.getKey().split("[.]", 2);
			results.add(generateProperty(splitName[0], splitName[1],entry.getValue(),"galasa-dev/v1alpha1"));
		} 
        return gson.toJson(results);
    }
}