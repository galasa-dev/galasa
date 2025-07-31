/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.spi.IRun;

public class RunsServletTest extends BaseServletTest {

    public static final Map<String, String> REQUIRED_HEADERS = new HashMap<>(Map.of("Authorization", "Bearer " + DUMMY_JWT));

    protected String generateStatusUpdateJson(String result) {
		return
		"{\n" +
		"  \"result\": \"" + result + "\"\n" +
		"}";
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

            JsonArray tagsArray = new JsonArray();
            for( String tag : run.getTags() ) {
                tagsArray.add(tag);
            }
            runJson.add("tags", tagsArray);

            runsJsonArray.add(runJson);
        }

        expectedJsonObj.add("runs", runsJsonArray);

        String expectedJson = gson.toJson(expectedJsonObj);
        return expectedJson;
    }

    protected String generatePayload(String[] classNames, String requestorType, String requestor, String testStream, String groupName, String overrideExpectedRequestor, String submissionId, Set<String>tags) {
        String classes ="";
        if (overrideExpectedRequestor !=null){
            requestor = overrideExpectedRequestor;
        }
        for (String className : classNames){
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

