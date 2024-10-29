/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.framework.api.resources;

import static org.assertj.core.api.Assertions.*;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.api.common.mocks.MockServletOutputStream;

import dev.galasa.framework.api.resources.mocks.MockResourcesServlet;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.GalasaGson;

public class ResourcesServletTest extends BaseServletTest {
    
	static final GalasaGson gson = new GalasaGson();

	MockResourcesServlet servlet;
	HttpServletRequest req;
	HttpServletResponse resp;

	private class MockICPSServiceWithError extends MockIConfigurationPropertyStoreService {
        protected MockICPSServiceWithError(String namespace){
            super.namespaceInput= namespace;
        }
        
        @Override
        public void deleteProperty(@NotNull String name) throws ConfigurationPropertyStoreException {
            throw new ConfigurationPropertyStoreException("Could not Delete Key");
        }
    }

	protected void setServlet(String namespace){
		this.servlet = new MockResourcesServlet();
        servlet.setResponseBuilder(new ResponseBuilder(new MockEnvironment()));

        ServletOutputStream outStream = new MockServletOutputStream();
		PrintWriter writer = new PrintWriter(outStream);
        this.resp = new MockHttpServletResponse(writer, outStream);

		IConfigurationPropertyStoreService cpsstore;
		if (namespace != null){
			cpsstore = new MockIConfigurationPropertyStoreService(namespace);
		} else{
			cpsstore = new MockICPSServiceWithError("framework");
		}
		IFramework framework = new MockFramework(cpsstore);
		this.servlet.setFramework(framework);
	}
	
	protected void setServlet(String path,String namespace, Map<String, String[]> parameterMap){
		setServlet(namespace);
		this.req = new MockHttpServletRequest(parameterMap,path);
	}

	protected void setServlet( String path,String namespace, JsonObject requestBody, String method){
		setServlet(namespace);
		this.req = new MockHttpServletRequest(path, gson.toJson(requestBody), method);
	}

	protected void setServlet( String path,String namespace, JsonObject requestBody, String method, Map<String,String> headerMap) {
		setServlet(namespace);
		this.req = new MockHttpServletRequest(path, gson.toJson(requestBody), method, headerMap);
	}

	protected MockResourcesServlet getServlet(){
		return this.servlet;
	}

	protected HttpServletRequest getRequest(){
		return this.req;
	}

	protected HttpServletResponse getResponse(){
	return this.resp;
	}

	protected void checkPropertyInNamespace(String namespace, String propertyName, String propertyValue) throws Exception{
		assertThat(checkProperty(namespace, propertyName, propertyValue)).isTrue();
	}
	
	protected void checkPropertyNotInNamespace(String namespace, String propertyName, String propertyValue) throws Exception{
		assertThat(checkProperty(namespace, propertyName, propertyValue)).isFalse();
	}

	protected boolean checkProperty(String namespace, String propertyName, String propertyValue) throws Exception{
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

	protected void checkErrorListContainsError(List<String> errors, String errorMessage){
		boolean match = false;
		for (String error: errors){
			if (error.contains(errorMessage)){
				match = true;
			}
		}
		assertThat(match).isTrue();
	}

	protected JsonObject generatePropertyJson(String namespace, String propertyName, String propertyValue, String apiVersion) {
        JsonObject propertyJson = new JsonObject();
        propertyJson.addProperty("apiVersion", apiVersion);
        propertyJson.addProperty("kind", "GalasaProperty");

        JsonObject propertyMetadata = new JsonObject();
        propertyMetadata.addProperty("namespace", namespace);
        propertyMetadata.addProperty("name", propertyName);

        JsonObject propertyData = new JsonObject();
        propertyData.addProperty("value", propertyValue);

        propertyJson.add("metadata", propertyMetadata);
        propertyJson.add("data", propertyData);

        // Expecting a JSON structure in the form:
        // {
        //     "apiVersion": "galasa-dev/v1alpha1",
        //     "kind": "GalasaProperty",
        //     "metadata": {
        //         "namespace": "mynamespace",
        //         "name": "my.property.name"
        //     },
        //     "data": {
        //         "value": "my-property-value"
        //     }
        // }
		return propertyJson;
	}

	protected JsonArray generatePropertyArrayJson(String namespace, String propertyName, String propertyValue, String apiVersion){
        JsonArray propertyArray = new JsonArray();
        propertyArray.add(generatePropertyJson(namespace, propertyName, propertyValue, apiVersion));
        return propertyArray;
    }

    protected JsonObject generateRequestJson(String action, String namespace, String propertyName, String propertyValue, String apiVersion) {
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("action", action);
        requestJson.add("data", generatePropertyArrayJson(namespace, propertyName, propertyValue, apiVersion));
        return requestJson;
    }

    protected JsonObject generateRequestJson(String action, List<JsonObject> properties) {
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("action", action);

        JsonArray dataArray = new JsonArray();
        for (JsonObject property : properties) {
            dataArray.add(property);
        }

        requestJson.add("data", dataArray);
        return requestJson;
    }

	protected String generateExpectedJson(Map<String, String> properties){
		JsonArray results = new JsonArray();
		for (Map.Entry<String,String> entry : properties.entrySet()){
			// Key Value namesapce.propertyname value value
			String[] splitName = entry.getKey().split("[.]", 2);
			results.add(generatePropertyJson(splitName[0], splitName[1],entry.getValue(),"galasa-dev/v1alpha1"));
		} 
        return gson.toJson(results);
    }
}
