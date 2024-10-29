/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockIFrameworkRuns;
import dev.galasa.framework.api.common.mocks.MockIRun;
import dev.galasa.framework.api.common.mocks.MockServletOutputStream;
import dev.galasa.framework.api.runs.mocks.MockRunsServlet;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.utils.GalasaGson;

public class RunsServletTest extends BaseServletTest {

	static final GalasaGson gson = new GalasaGson();

	MockRunsServlet servlet;
	HttpServletRequest req;
	HttpServletResponse resp;
    protected List<IRun> runs = new ArrayList<IRun>();


	protected void setServlet(String path, String groupName, List<IRun> runs){
        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username,name,sub");

        this.servlet = new MockRunsServlet(mockEnv);
        servlet.setResponseBuilder(new ResponseBuilder(mockEnv));

        ServletOutputStream outStream = new MockServletOutputStream();
        PrintWriter writer = new PrintWriter(outStream);
        this.resp = new MockHttpServletResponse(writer, outStream);
        this.req = new MockHttpServletRequest(path);
		if (groupName != null){
            IFrameworkRuns frameworkRuns = new MockIFrameworkRuns(groupName, runs);
			IFramework framework = new MockFramework(frameworkRuns);
			this.servlet.setFramework(framework);
		}
	}
	
	protected void setServlet(String path, String groupName, String value, String method){
		setServlet(path, groupName, null);
		this.req = new MockHttpServletRequest(path, value, method);
	}

    protected void setServlet(String path, String groupName, String value, String method, Map<String, String> headerMap){
		setServlet(path, groupName, null);
		this.req = new MockHttpServletRequest(path, value, method, headerMap);
	}

    protected void setServlet(String path, String groupName, List<IRun> runs, String value, String method, Map<String, String> headerMap){
		setServlet(path, groupName, runs);
		this.req = new MockHttpServletRequest(path, value, method, headerMap);
	}

	protected MockRunsServlet getServlet(){
		return this.servlet;
	}

	protected HttpServletRequest getRequest(){
		return this.req;
	}

	protected HttpServletResponse getResponse(){
	return this.resp;
	}

    protected void addRun(String runName, String runType, String requestor, String test, String runStatus, String bundle, String testClass, String groupName){
		this.runs.add(new MockIRun( runName, runType, requestor, test, runStatus, bundle, testClass, groupName));
    }

	protected String generateExpectedJson(List<IRun> runs, String complete) {
        String expectedJson = "{\n  \"complete\": "+complete+",\n  \"runs\": [\n    ";
		for (int r= 0; r< runs.size(); r++ ) {
            String runString ="";
			if (r > 0) {
					runString += ",\n    ";
			}
			runString += "{\n      \"name\": \""+runs.get(r).getName()+"\",\n"+
                "      \"heartbeat\": \"2023-10-12T12:16:49.832925Z\",\n"+
                "      \"type\": \""+runs.get(r).getType()+"\",\n"+
                "      \"group\": \""+runs.get(r).getGroup()+"\",\n"+
                "      \"test\": \""+runs.get(r).getTestClassName()+"\",\n"+
                "      \"bundleName\": \""+runs.get(r).getTestBundleName()+"\",\n"+
                "      \"testName\": \""+runs.get(r).getTest()+"\",\n";
            if (!runs.get(r).getStatus().equals("submitted")){
                runString +="      \"status\": \""+runs.get(r).getStatus()+"\",\n";
            }
            runString +="      \"result\": \"Passed\",\n"+
                "      \"queued\": \"2023-10-12T12:16:49.832925Z\",\n"+
                "      \"finished\": \"2023-10-12T12:16:49.832925Z\",\n"+
                "      \"waitUntil\": \"2023-10-12T12:16:49.832925Z\",\n"+
                "      \"requestor\": \""+runs.get(r).getRequestor()+"\",\n"+
                "      \"isLocal\": false,\n"+
                "      \"isTraceEnabled\": false,\n"+
                "      \"rasRunId\": \"cdb-"+runs.get(r).getName()+"\"\n    }";
				
				expectedJson += runString;
        }
        expectedJson += "\n  ]\n}";
        return expectedJson;
    }

    protected String generatePayload(String[] classNames, String requestorType, String requestor, String testStream, String groupName, String overrideExpectedRequestor){
        String classes ="";
        if (overrideExpectedRequestor !=null){
            requestor = overrideExpectedRequestor;
        }
        for (String className : classNames){
            addRun( "runnamename", requestorType, requestor, "name", "submitted", className.split("/")[0], "java", groupName);
            classes += "\""+className+"\",";
        }
        classes = classes.substring(0, classes.length()-1);
        String payload = "{\"classNames\": ["+classes+"]," +
            "\"requestorType\": \""+requestorType+"\"," +
            "\"requestor\": \""+requestor+"\"," +
            "\"testStream\": \""+testStream+"\"," +
            "\"obr\": \"this.obr\","+
            "\"mavenRepository\": \"this.maven.repo\"," +
            "\"sharedEnvironmentRunTime\": \"envRunTime\"," +
            "\"overrides\": {}," +
            "\"trace\": true }";
            
        return payload;
    }
    
}

