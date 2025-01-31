/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import dev.galasa.framework.spi.IRunResult;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.ras.internal.RasServlet;
import dev.galasa.framework.api.ras.internal.RasServletTest;
import dev.galasa.framework.api.ras.internal.mocks.*;
import dev.galasa.framework.mocks.MockFileSystem;
import dev.galasa.framework.mocks.MockPath;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.nio.file.Path;

public class TestRunArtifactsListServlet extends RasServletTest {

	class MockJsonObject {
		public Path path;
		public String contentType;
		public int size;

		public MockJsonObject(Path path, String contentType, int size) {
			this.path = path;
			this.contentType = contentType;
			this.size = size;
		}

        public Path getPath() {
            return path;
        }

        public String getContentType() {
            return contentType;
        }

        public int getSize() {
            return size;
        }
	}

	private String generateExpectedJsonArtifacts(List<MockJsonObject> artifacts) {
		String jsonResult = "[\n";
		int numOfArtifacts = artifacts.size();
		if (numOfArtifacts > 0) {

			for (int i = 0; i < numOfArtifacts; i++ ) {
				String runData = "";
				if (0 < i && i < numOfArtifacts) {
					runData = ",\n";
				}
                MockJsonObject artifact = artifacts.get(i);
                runData += getArtifactAsJsonString(artifact.path.toString(), artifact.contentType, artifact.size);
                if (i == numOfArtifacts - 1) {
                    runData += ",\n";
                }
				jsonResult += runData;
			}
		}
		return jsonResult;
	}

    private String getArtifactAsJsonString(String path, String contentType, int size) {
        String artifactJson = "  {\n"+
            "    \"path\": \""+path+"\",\n"+
            "    \"contentType\": \""+contentType+"\",\n"+
            "    \"size\": "+size+"\n"+
            "  }";
        return artifactJson;
    }

    private Map<String, String> getArtifactFields(String path, String contentType, String size) {
        Map<String, String> artifactFields = Map.of(
            "path", path,
            "contentType", contentType
        );

        if (size != null) {
            artifactFields.put("size", size);
        }
        return artifactFields;
    }

    private void checkRootArtifactsJson(String jsonString) throws Exception {
        checkJsonArrayStructure(jsonString, getArtifactFields("/run.log", "text/plain", null));
        checkJsonArrayStructure(jsonString, getArtifactFields("/structure.json", "application/json", null));
        checkJsonArrayStructure(jsonString, getArtifactFields("/artifacts.properties", "text/plain", null));
        checkJsonArrayStructure(jsonString, getArtifactFields("/artifacts.json", "application/json", null));
    }

	@Before
	public void setUp() {
		mockFileSystem = new MockFileSystem();
	}

	/*
     * Regex Path
     */

	@Test
	public void testPathRegexExpectedLocalPathReturnsTrue(){
		//Given...
		String expectedPath = RunArtifactsListRoute.path;
		String inputPath = "/runs/lcl-abcd-1234.run/artifacts";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void testPathRegexExpectedCouchDBPathReturnsTrue(){
		//Given...
		String expectedPath = RunArtifactsListRoute.path;
		String inputPath = "/runs/cdb-efgh-5678.run/artifacts";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void testPathRegexLowerCasePathReturnsTrue(){
		//Given...
		String expectedPath = RunArtifactsListRoute.path;
		String inputPath = "/runs/cdbstoredrun/artifacts";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void testPathRegexExpectedPathWithCapitalLeadingLetterReturnsTrue(){
		//Given...
		String expectedPath = RunArtifactsListRoute.path;
		String inputPath = "/runs/ABC-DEFG-5678.run/artifacts";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void testPathRegexUpperCasePathReturnsFalse(){
		//Given...
		String expectedPath = RunArtifactsListRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/ARTIFACTS";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void testPathRegexExpectedPathWithLeadingNumberReturnsFalse(){
		//Given...
		String expectedPath = RunArtifactsListRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/1artifacts";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void testPathRegexExpectedPathWithTrailingForwardSlashReturnsTrue(){
		//Given...
		String expectedPath = RunArtifactsListRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/artifacts/";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void testPathRegexNumberPathReturnsFalse(){
		//Given...
		String expectedPath = RunArtifactsListRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/artifacts1";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void testPathRegexUnexpectedPathReturnsFalse(){
		//Given...
		String expectedPath = RunArtifactsListRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/artifact";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void testPathRegexEmptyPathReturnsFalse(){
		//Given...
		String expectedPath = RunArtifactsListRoute.path;
		String inputPath = "";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void testPathRegexSpecialCharacterPathReturnsFalse(){
		//Given...
		String expectedPath = RunArtifactsListRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/artifacts/?";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void testPathRegexMultipleForwardSlashPathReturnsFalse(){
		//Given...
		String expectedPath = RunArtifactsListRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/artifacts//////";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	/*
	 * GET Requests
	 */

    @Test
	public void testMultipleArtifactsToListReturnsOKWithArtifacts() throws Exception {
		//Given..
		String runName = "testA";
		MockPath mockArtifactsPath = new MockPath("/" + runName, mockFileSystem);
		List<Path> dummyArtifactPaths = Arrays.asList(
			new MockPath(mockArtifactsPath + "/dummyB.gz",mockFileSystem),
			new MockPath(mockArtifactsPath + "/dummyC.txt",mockFileSystem),
			new MockPath(mockArtifactsPath + "/dummyA.json",mockFileSystem)
        );

		List<MockJsonObject> mockArtifacts = Arrays.asList(
			new MockJsonObject(dummyArtifactPaths.get(0), "application/x-gzip", 0),
			new MockJsonObject(dummyArtifactPaths.get(1), "text/plain", 0),
			new MockJsonObject(dummyArtifactPaths.get(2), "application/json", 0)
		);

		mockFileSystem.createDirectories(mockArtifactsPath);
		for (Path artifactPath : dummyArtifactPaths) {
			mockFileSystem.createFile(artifactPath);

		}

		String runId = "xxxxx678xxxxx";
        String runLog = "log";
		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, runLog);

		//Build Http query parameters
		Map<String, String[]> parameterMap = new HashMap<String,String[]>();

		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/artifacts");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
        // [
        //     {
        //       "path": "/artifacts/testA/dummyB.gz",
        //       "contentType": "application/x-gzip",
        //       "size": 0
        //     },
        //     {
        //       "path": "/artifacts/testA/dummyC.txt",
        //       "contentType": "text/plain",
        //       "size": 0
        //     },
        //     {
        //       "path": "/artifacts/testA/dummyA.json",
        //       "contentType": "application/json",
        //       "size": 0
        //     },
        //     {
        //       "path": "/run.log",
        //       "contentType": "text/plain",
        //       "size": 3
        //     },
        //     {
        //       "path": "/structure.json",
        //       "contentType": "application/json",
        //       "size": 71
        //     },
        //     {
        //       "path": "/artifacts.properties",
        //       "contentType": "text/plain",
        //       "size": 240
        //     }
        //     {
        //       "path": "/artifacts.json",
        //       "contentType": "application/json",
        //       "size": 313
        //     }
        // ]
		assertThat(resp.getStatus()).isEqualTo(200);

		String jsonString = outStream.toString();
		JsonElement jsonElement = JsonParser.parseString(jsonString);
		assertThat(jsonElement).isNotNull().as("Failed to parse the body to a json object.");

		JsonArray jsonArray = jsonElement.getAsJsonArray();
		assertThat(jsonArray.size()).isEqualTo(7);

        List<Path> expectedArtifactPaths = dummyArtifactPaths.stream()
            .map(path -> new MockPath("/artifacts" + path.toString(), mockFileSystem))
            .collect(Collectors.toList());

        List<MockJsonObject> expectedArtifacts = new ArrayList<>();
        for (int i = 0; i < mockArtifacts.size(); i++) {
            expectedArtifacts.add(
                new MockJsonObject(expectedArtifactPaths.get(i), mockArtifacts.get(i).getContentType(), mockArtifacts.get(i).getSize()
            ));
        }

        checkRootArtifactsJson(jsonString);
		String expectedJson = generateExpectedJsonArtifacts(expectedArtifacts);
		assertThat(jsonString).contains(expectedJson);

		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testOneArtifactToListReturnsOKWithArtifact() throws Exception {
		//Given..
		String runName = "testA";
        String runLog = "log";
		MockPath mockArtifactPath = new MockPath("/" + runName,mockFileSystem);
		MockPath dummyArtifactPath = new MockPath(mockArtifactPath + "/dummy.gz",mockFileSystem);
		MockJsonObject mockArtifact = new MockJsonObject(dummyArtifactPath, "application/x-gzip", 0);
		mockFileSystem.createDirectories(mockArtifactPath);
		mockFileSystem.createFile(dummyArtifactPath);

		String runId = "xxxxx678xxxxx";
		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, runLog);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/artifacts");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// [
		//	 {
		//     "path": "/artifacts/testA/dummy.gz",
		//     "contentType": "application/x-gzip",
		//	   "size": 0
		//   },
		//	 {
		//     "path": "/run.log",
		//     "contentType": "text/plain",
		//	   "size": 3
		//   },
		//	 {
		//     "path": "/structure.json",
		//     "contentType": "application/json",
		//     "size": "82",
		//   },
		//	 {
		//     "path": "/artifacts.properties",
		//     "contentType": "text/plain",
		//     "size": "240",
		//   }
		//	 {
		//     "path": "/artifacts.json",
		//     "contentType": "application/json",
		//     "size": "240",
		//   }
		// ]
		assertThat(resp.getStatus()).isEqualTo(200);

		String jsonString = outStream.toString();
		JsonElement jsonElement = JsonParser.parseString(jsonString);
		assertThat(jsonElement).isNotNull().as("Failed to parse the body to a json object.");

		JsonArray jsonArray = jsonElement.getAsJsonArray();
		assertThat(jsonArray.size()).isEqualTo(5);

        MockPath expectedArtifactPath = new MockPath("/artifacts" + dummyArtifactPath.toString(), mockFileSystem);
        MockJsonObject expectedArtifact = new MockJsonObject(expectedArtifactPath, mockArtifact.getContentType(), mockArtifact.getSize());

		String expectedJson = generateExpectedJsonArtifacts(Arrays.asList(expectedArtifact));
		assertThat(jsonString).contains(expectedJson);
        checkRootArtifactsJson(jsonString);

		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

    @Test
	public void testNoArtifactsToListGivesRootArtifacts() throws Exception {
		//Given..
		String runName = "testA";
		MockFileSystem mockFileSystem = new MockFileSystem();
		MockPath mockArtifactsPath = new MockPath("/" + runName + "/artifacts",mockFileSystem);
		mockFileSystem.createDirectories(mockArtifactsPath);

		String runId = "xxxxx678xxxxx";
        String runLog = "log";
		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, runLog);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/artifacts");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// [
		//	 {
		//     "path": "/run.log",
		//     "contentType": "text/plain",
		//     "size": 3,
		//   },
        //	 {
		//     "path": "/structure.json",
		//     "contentType": "application/json",
		//     "size": 82,
		//   },
		//	 {
		//     "path": "/artifacts.properties",
		//     "contentType": "text/plain",
		//     "size": 240,
		//   }
		//	 {
		//     "path": "/artifacts.json",
		//     "contentType": "application/json",
		//     "size": 240,
		//   }
		// ]
		assertThat(resp.getStatus()).isEqualTo(200);

		String jsonString = outStream.toString();
		JsonElement jsonElement = JsonParser.parseString(jsonString);
		assertThat(jsonElement).isNotNull().as("Failed to parse the body to a json object.");

		JsonArray jsonArray = jsonElement.getAsJsonArray();
		assertThat(jsonArray.size()).isEqualTo(4);

        checkRootArtifactsJson(jsonString);
        String expectedJson = generateExpectedJsonArtifacts(new ArrayList<>());
		assertThat(jsonString).contains(expectedJson);

		assertThat(resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testBadRunIdGivesNotFoundError() throws Exception {
		//Given..
		String runId = "badRunId";
		List<IRunResult> mockInputRunResults = new ArrayList<>();

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/artifacts");
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// {
		//   "error_code" : 5002,
		//   "error_message" : "GAL5002E: Error retrieving ras run from identifier 'badRunId'.""
		// }
		assertThat(resp.getStatus()).isEqualTo(404);
		checkErrorStructure(outStream.toString() , 5091 , "GAL5091E", "badRunId" );

		assertThat( resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testNoArtifactsToListWithAcceptHeaderGivesRootArtifacts() throws Exception {
		//Given..
		String runName = "testA";
		MockFileSystem mockFileSystem = new MockFileSystem();
		MockPath mockArtifactsPath = new MockPath("/" + runName + "/artifacts",mockFileSystem);
		mockFileSystem.createDirectories(mockArtifactsPath);

		String runId = "xxxxx678xxxxx";
        String runLog = "log";
		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, runLog);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		Map<String, String> headerMap = new HashMap<String,String>();
        headerMap.put("Accept", "application/json");
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/artifacts", headerMap);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		assertThat(resp.getStatus()).isEqualTo(200);

		String jsonString = outStream.toString();
		JsonElement jsonElement = JsonParser.parseString(jsonString);
		assertThat(jsonElement).isNotNull().as("Failed to parse the body to a json object.");

		JsonArray jsonArray = jsonElement.getAsJsonArray();
		assertThat(jsonArray.size()).isEqualTo(4);

        checkRootArtifactsJson(jsonString);
        String expectedJson = generateExpectedJsonArtifacts(new ArrayList<>());
		assertThat(jsonString).contains(expectedJson);

		assertThat(resp.getContentType()).isEqualTo("application/json");
	}
}