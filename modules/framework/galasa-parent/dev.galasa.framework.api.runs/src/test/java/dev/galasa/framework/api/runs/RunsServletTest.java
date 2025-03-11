/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
    private static final Map<String, String> REQUIRED_HEADERS = new HashMap<>(Map.of("Authorization", "Bearer " + DUMMY_JWT));

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
        this.req = new MockHttpServletRequest(path, REQUIRED_HEADERS);
		if (groupName != null){
            IFrameworkRuns frameworkRuns = new MockIFrameworkRuns(groupName, runs);
			IFramework framework = new MockFramework(frameworkRuns);
			this.servlet.setFramework(framework);
		}
	}
	
	protected void setServlet(String path, String groupName, String value, String method){
		setServlet(path, groupName, null);
		this.req = new MockHttpServletRequest(path, value, method, REQUIRED_HEADERS);
	}

    protected void setServlet(String path, String groupName, String value, String method, Map<String, String> headerMap){
		setServlet(path, groupName, null);
        headerMap.putAll(REQUIRED_HEADERS);
		this.req = new MockHttpServletRequest(path, value, method, headerMap);
	}

    protected void setServlet(String path, String groupName, List<IRun> runs, String value, String method, Map<String, String> headerMap){
		setServlet(path, groupName, runs);
        headerMap.putAll(REQUIRED_HEADERS);
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

    protected void addRun(String runName, String runType, String requestor, String test, String runStatus, String bundle, String testClass, String groupName, String submissionId){
		this.runs.add(new MockIRun( runName, runType, requestor, test, runStatus, bundle, testClass, groupName, submissionId));
    }

	protected String generateExpectedJson(List<IRun> runs, boolean complete) {

        JsonObject expectedJsonObj = new JsonObject();

        expectedJsonObj.addProperty("complete", complete);

        JsonArray runsJsonArray = new JsonArray();

        for (IRun run : runs) {
            JsonObject runJson = new JsonObject();
            runJson.addProperty("name", run.getName());
            runJson.addProperty("heartbeat", "2023-10-12T12:16:49.832925Z");
            runJson.addProperty("type", run.getType());
            runJson.addProperty("group", run.getGroup());
            runJson.addProperty("submissionId", run.getSubmissionId());
            runJson.addProperty("test", run.getTestClassName());
            runJson.addProperty("bundleName", run.getTestBundleName());
            runJson.addProperty("testName", run.getTest());

            if (!run.getStatus().equals("submitted")) {
                runJson.addProperty("status", run.getStatus());
            }

            runJson.addProperty("result", "Passed");
            runJson.addProperty("queued", "2023-10-12T12:16:49.832925Z");
            runJson.addProperty("finished", "2023-10-12T12:16:49.832925Z");
            runJson.addProperty("waitUntil", "2023-10-12T12:16:49.832925Z");
            runJson.addProperty("requestor", run.getRequestor());
            runJson.addProperty("isLocal", false);
            runJson.addProperty("isTraceEnabled", false);
            runJson.addProperty("rasRunId", "cdb-" + run.getName());

            runsJsonArray.add(runJson);
        }

        expectedJsonObj.add("runs", runsJsonArray);

        String expectedJson = gson.toJson(expectedJsonObj);
        return expectedJson;
    }

    protected String generatePayload(String[] classNames, String requestorType, String requestor, String testStream, String groupName, String overrideExpectedRequestor, String submissionId) {
        String classes ="";
        if (overrideExpectedRequestor !=null){
            requestor = overrideExpectedRequestor;
        }
        for (String className : classNames){
            addRun( "runnamename", requestorType, requestor, "name", "submitted", className.split("/")[0], "java", groupName, submissionId);
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

