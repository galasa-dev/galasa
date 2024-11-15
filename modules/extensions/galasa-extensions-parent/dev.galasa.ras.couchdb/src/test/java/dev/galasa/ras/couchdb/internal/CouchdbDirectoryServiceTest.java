/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import dev.galasa.extensions.common.couchdb.pojos.IdRev;
import dev.galasa.extensions.common.impl.HttpRequestFactoryImpl;
import dev.galasa.extensions.common.mocks.BaseHttpInteraction;
import dev.galasa.extensions.common.mocks.HttpInteraction;
import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.ras.IRasSearchCriteria;
import dev.galasa.framework.spi.ras.RasRunResultPage;
import dev.galasa.framework.spi.ras.RasSearchCriteriaBundle;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedFrom;
import dev.galasa.framework.spi.ras.RasSearchCriteriaQueuedTo;
import dev.galasa.framework.spi.ras.RasSearchCriteriaRequestor;
import dev.galasa.framework.spi.ras.RasSearchCriteriaResult;
import dev.galasa.framework.spi.ras.RasSearchCriteriaRunName;
import dev.galasa.framework.spi.ras.RasSearchCriteriaStatus;
import dev.galasa.framework.spi.ras.RasSearchCriteriaTestName;
import dev.galasa.framework.spi.ras.RasSortField;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.ras.couchdb.internal.mocks.CouchdbTestFixtures;
import dev.galasa.ras.couchdb.internal.mocks.MockLogFactory;
import dev.galasa.ras.couchdb.internal.pojos.FoundRuns;
import dev.galasa.ras.couchdb.internal.pojos.TestStructureCouchdb;

public class CouchdbDirectoryServiceTest {

    private CouchdbTestFixtures fixtures = new CouchdbTestFixtures();

    class PostCouchdbFindRunsInteraction extends BaseHttpInteraction {

        private String[] expectedRequestBodyParts;

        public PostCouchdbFindRunsInteraction(String expectedUri, FoundRuns foundRuns, String... expectedRequestBodyParts) {
            this(expectedUri, HttpStatus.SC_OK, foundRuns, expectedRequestBodyParts);
        }

        public PostCouchdbFindRunsInteraction(String expectedUri, int statusCode, FoundRuns foundRuns, String... expectedRequestBodyParts) {
            super(expectedUri, statusCode);
            setResponsePayload(foundRuns);
            this.expectedRequestBodyParts = expectedRequestBodyParts;
        }

        @Override
        public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
            super.validateRequest(host,request);
            assertThat(request.getRequestLine().getMethod()).isEqualTo("POST");
            if (expectedRequestBodyParts.length > 0) {
                validatePostRequestBody((HttpPost) request);
            }
        }

        private void validatePostRequestBody(HttpPost postRequest) {
            try {
                String requestBody = EntityUtils.toString(postRequest.getEntity());
                assertThat(requestBody).contains(expectedRequestBodyParts);

            } catch (IOException ex) {
                fail("Failed to parse POST request body");
            }
        }
    }

    class GetRunByIdFromCouchdbInteraction extends BaseHttpInteraction {

        public GetRunByIdFromCouchdbInteraction(String expectedUri, int statusCode, TestStructureCouchdb runTestStructure) {
            super(expectedUri, statusCode);
            setResponsePayload(runTestStructure);
        }

        @Override
        public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
            super.validateRequest(host,request);
            assertThat(request.getRequestLine().getMethod()).isEqualTo("GET");
        }
    }

    class GetDocumentByIdFromCouchdbInteraction extends BaseHttpInteraction {

        public GetDocumentByIdFromCouchdbInteraction(String expectedUri, int statusCode, IdRev idRev) {
            super(expectedUri, statusCode);
            setResponsePayload(idRev);
        }

        @Override
        public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
            super.validateRequest(host,request);
            assertThat(request.getRequestLine().getMethod()).isEqualTo("GET");
        }
    }

    class DeleteDocumentFromCouchdbInteraction extends BaseHttpInteraction {

        public DeleteDocumentFromCouchdbInteraction(String expectedUri, int statusCode) {
            super(expectedUri, statusCode);
        }

        @Override
        public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
            super.validateRequest(host,request);
            assertThat(request.getRequestLine().getMethod()).isEqualTo("DELETE");
        }
    }

    private TestStructureCouchdb createRunTestStructure(String runName) {
        TestStructureCouchdb mockTestStructure = new TestStructureCouchdb();
        mockTestStructure._id = runName;
        mockTestStructure._rev = "this-is-a-revision";
        mockTestStructure.setRunName(runName);
        mockTestStructure.setArtifactRecordIds(new ArrayList<>());
        mockTestStructure.setLogRecordIds(new ArrayList<>());
        return mockTestStructure;
    }

    //------------------------------------------
    //
    // Tests for getting runs by criteria
    //
    //------------------------------------------

    @Test
    public void testGetRunsByQueuedFromOnePageReturnsRunsOk() throws Exception {
        // Given...
        TestStructureCouchdb mockRun1 = createRunTestStructure("run1");
        TestStructureCouchdb mockRun2 = createRunTestStructure("run2");

        Instant queuedFromTime = Instant.EPOCH;
        RasSearchCriteriaQueuedFrom queuedFrom = new RasSearchCriteriaQueuedFrom(queuedFromTime);

        FoundRuns findRunsResponse = new FoundRuns();
        findRunsResponse.docs = List.of(mockRun1, mockRun2);
        findRunsResponse.bookmark = "bookmark!";

        FoundRuns emptyRunsResponse = new FoundRuns();
        emptyRunsResponse.docs = new ArrayList<>();

        String expectedUri = "http://my.uri/galasa_run/_find";
        List<HttpInteraction> interactions = List.of(
            new PostCouchdbFindRunsInteraction(expectedUri, findRunsResponse, "queued", "$gte", queuedFromTime.toString()),
            new PostCouchdbFindRunsInteraction(expectedUri, emptyRunsResponse, "queued", "$gte", queuedFromTime.toString(), findRunsResponse.bookmark)
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        // When...
        List<IRunResult> runs = directoryService.getRuns(queuedFrom);

        // Then...
        assertThat(runs).hasSize(2);
        assertThat(runs.get(0).getTestStructure().getRunName()).isEqualTo(mockRun1.getRunName());
        assertThat(runs.get(1).getTestStructure().getRunName()).isEqualTo(mockRun2.getRunName());
    }

    @Test
    public void testGetRunsMultiplePagesReturnsRunsOk() throws Exception {
        // Given...
        TestStructureCouchdb mockRun1 = createRunTestStructure("run1");
        TestStructureCouchdb mockRun2 = createRunTestStructure("run2");
        TestStructureCouchdb mockRun3 = createRunTestStructure("run3");

        Instant queuedFromTime = Instant.EPOCH;
        RasSearchCriteriaQueuedFrom queuedFrom = new RasSearchCriteriaQueuedFrom(queuedFromTime);

        FoundRuns findRunsResponsePage1 = new FoundRuns();
        findRunsResponsePage1.docs = List.of(mockRun1, mockRun2);
        findRunsResponsePage1.bookmark = "bookmark1";

        FoundRuns findRunsResponsePage2 = new FoundRuns();
        findRunsResponsePage2.docs = List.of(mockRun3);
        findRunsResponsePage2.bookmark = "bookmark2";

        FoundRuns emptyRunsResponse = new FoundRuns();
        emptyRunsResponse.docs = new ArrayList<>();

        String expectedUri = "http://my.uri/galasa_run/_find";
        List<HttpInteraction> interactions = List.of(
            new PostCouchdbFindRunsInteraction(expectedUri, findRunsResponsePage1, "queued", "$gte", queuedFromTime.toString()),
            new PostCouchdbFindRunsInteraction(expectedUri, findRunsResponsePage2, "queued", "$gte", queuedFromTime.toString(), findRunsResponsePage1.bookmark),
            new PostCouchdbFindRunsInteraction(expectedUri, emptyRunsResponse, "queued", "$gte", queuedFromTime.toString(), findRunsResponsePage2.bookmark)
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        // When...
        List<IRunResult> runs = directoryService.getRuns(queuedFrom);

        // Then...
        assertThat(runs).hasSize(3);
        assertThat(runs.get(0).getTestStructure().getRunName()).isEqualTo(mockRun1.getRunName());
        assertThat(runs.get(1).getTestStructure().getRunName()).isEqualTo(mockRun2.getRunName());
        assertThat(runs.get(2).getTestStructure().getRunName()).isEqualTo(mockRun3.getRunName());
    }

    @Test
    public void testGetRunsWithInvalidRunIgnoresRunOk() throws Exception {
        // Given...
        String runName1 = "run1";
        TestStructureCouchdb mockRun1 = createRunTestStructure(runName1);

        // No run name is set, so this is not a valid run
        TestStructureCouchdb invalidRun = createRunTestStructure(null);

        FoundRuns findRunsResponse = new FoundRuns();
        findRunsResponse.docs = List.of(mockRun1, invalidRun);
        findRunsResponse.warning = "this response contains an invalid run!";
        findRunsResponse.bookmark = "bookmark!";

        FoundRuns emptyRunsResponse = new FoundRuns();
        emptyRunsResponse.docs = new ArrayList<>();

        String expectedUri = "http://my.uri/galasa_run/_find";
        List<HttpInteraction> interactions = List.of(
            new PostCouchdbFindRunsInteraction(expectedUri, findRunsResponse, "runName", runName1),
            new PostCouchdbFindRunsInteraction(expectedUri, emptyRunsResponse, "runName", runName1, findRunsResponse.bookmark)
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());
        RasSearchCriteriaRunName runNameCriteria = new RasSearchCriteriaRunName(runName1);

        // When...
        List<IRunResult> runs = directoryService.getRuns(runNameCriteria);

        // Then...
        assertThat(runs).hasSize(1);
        assertThat(runs.get(0).getTestStructure().getRunName()).isEqualTo(mockRun1.getRunName());
    }

    @Test
    public void testGetRunsWithErrorResponseCodeThrowsError() throws Exception {
        // Given...
        String expectedUri = "http://my.uri/galasa_run/_find";
        List<HttpInteraction> interactions = List.of(
            new PostCouchdbFindRunsInteraction(expectedUri, HttpStatus.SC_INTERNAL_SERVER_ERROR, null)
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());
        RasSearchCriteriaQueuedFrom queuedFrom = new RasSearchCriteriaQueuedFrom(Instant.EPOCH);

        // When...
        CouchdbRasException thrown = catchThrowableOfType(() -> directoryService.getRuns(queuedFrom), CouchdbRasException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Unable to find runs");
    }

    @Test
    public void testGetRunsWithInvalidResponseThrowsError() throws Exception {
        // Given...
        FoundRuns findRunsResponse = new FoundRuns();
        findRunsResponse.docs = null;
        findRunsResponse.bookmark = "bookmark!";

        FoundRuns emptyRunsResponse = new FoundRuns();
        emptyRunsResponse.docs = new ArrayList<>();

        String expectedUri = "http://my.uri/galasa_run/_find";
        List<HttpInteraction> interactions = List.of(
            new PostCouchdbFindRunsInteraction(expectedUri, findRunsResponse),
            new PostCouchdbFindRunsInteraction(expectedUri, emptyRunsResponse)
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());
        RasSearchCriteriaQueuedFrom queuedFrom = new RasSearchCriteriaQueuedFrom(Instant.EPOCH);

        // When...
        CouchdbRasException thrown = catchThrowableOfType(() -> directoryService.getRuns(queuedFrom), CouchdbRasException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Unable to find runs", "Invalid JSON response");
    }

    @Test
    public void testGetRunsMultipleCriteriaReturnsRunsOk() throws Exception {
        // Given...
        TestStructureCouchdb mockRun1 = createRunTestStructure("run1");
        TestStructureCouchdb mockRun2 = createRunTestStructure("run2");

        Instant queuedFromTime = Instant.MAX;
        Instant queuedToTime = Instant.MAX;
        String resultStr = "Passed";
        String requestorName = "me";
        String testNameStr = "mytest";
        String bundleName = "my.bundle";
        RasSearchCriteriaQueuedFrom queuedFrom = new RasSearchCriteriaQueuedFrom(queuedFromTime);
        RasSearchCriteriaQueuedTo queuedTo = new RasSearchCriteriaQueuedTo(queuedToTime);
        RasSearchCriteriaResult result = new RasSearchCriteriaResult(resultStr);
        RasSearchCriteriaRequestor requestor = new RasSearchCriteriaRequestor(requestorName);
        RasSearchCriteriaTestName testName = new RasSearchCriteriaTestName(testNameStr);
        RasSearchCriteriaBundle bundle = new RasSearchCriteriaBundle(bundleName);
        RasSearchCriteriaStatus status = new RasSearchCriteriaStatus(List.of(TestRunLifecycleStatus.FINISHED));

        FoundRuns findRunsResponse = new FoundRuns();
        findRunsResponse.docs = List.of(mockRun1, mockRun2);
        findRunsResponse.bookmark = "bookmark!";

        FoundRuns emptyRunsResponse = new FoundRuns();
        emptyRunsResponse.docs = new ArrayList<>();

        String expectedUri = "http://my.uri/galasa_run/_find";
        String[] expectedRequestBodyParts = new String[] {
            "queued", "$gte", queuedFromTime.toString(), "$lt", queuedToTime.toString(), "result", resultStr,
            "testName", testNameStr, "bundle", bundleName, "status", TestRunLifecycleStatus.FINISHED.toString()
        };

        List<HttpInteraction> interactions = List.of(
            new PostCouchdbFindRunsInteraction(expectedUri, findRunsResponse, expectedRequestBodyParts),
            new PostCouchdbFindRunsInteraction(expectedUri, emptyRunsResponse, expectedRequestBodyParts)
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        // When...
        List<IRunResult> runs = directoryService.getRuns(queuedFrom, queuedTo, result, requestor, testName, bundle, status);

        // Then...
        assertThat(runs).hasSize(2);
        assertThat(runs.get(0).getTestStructure().getRunName()).isEqualTo(mockRun1.getRunName());
        assertThat(runs.get(1).getTestStructure().getRunName()).isEqualTo(mockRun2.getRunName());
    }


    @Test
    public void testGetRunsWithInvalidCriteriaThrowsError() throws Exception {
        // Given...
        IRasSearchCriteria unknownCriteria = new IRasSearchCriteria() {
            @Override
            public boolean criteriaMatched(@NotNull TestStructure testStructure) {
                return false;
            }
        };

        List<HttpInteraction> interactions = new ArrayList<>();

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        // When...
        ResultArchiveStoreException thrown = catchThrowableOfType(() -> directoryService.getRuns(unknownCriteria), ResultArchiveStoreException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Unrecognised search criteria");
    }

    @Test
    public void testGetRunsPageByQueuedFromReturnsRunsOk() throws Exception {
        // Given...
        TestStructureCouchdb mockRun1 = createRunTestStructure("run1");
        TestStructureCouchdb mockRun2 = createRunTestStructure("run2");

        Instant queuedFromTime = Instant.EPOCH;
        RasSearchCriteriaQueuedFrom queuedFrom = new RasSearchCriteriaQueuedFrom(queuedFromTime);

        FoundRuns findRunsResponse = new FoundRuns();
        findRunsResponse.docs = List.of(mockRun1, mockRun2);
        findRunsResponse.bookmark = "bookmark!";

        FoundRuns emptyRunsResponse = new FoundRuns();
        emptyRunsResponse.docs = new ArrayList<>();

        String expectedUri = "http://my.uri/galasa_run/_find";
        List<HttpInteraction> interactions = List.of(
            new PostCouchdbFindRunsInteraction(expectedUri, findRunsResponse, "queued", "$gte", queuedFromTime.toString())
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        int maxResults = 100;

        // When...
        RasRunResultPage runsPage = directoryService.getRunsPage(maxResults, null, null, queuedFrom);

        // Then...
        assertThat(runsPage.getNextCursor()).isEqualTo(findRunsResponse.bookmark);

        List<IRunResult> runs = runsPage.getRuns();
        assertThat(runs).hasSize(2);
        assertThat(runs.get(0).getTestStructure().getRunName()).isEqualTo(mockRun1.getRunName());
        assertThat(runs.get(1).getTestStructure().getRunName()).isEqualTo(mockRun2.getRunName());
    }

    @Test
    public void testGetRunsPageByQueuedFromWithSortReturnsRunsOk() throws Exception {
        // Given...
        TestStructureCouchdb mockRun1 = createRunTestStructure("run1");
        TestStructureCouchdb mockRun2 = createRunTestStructure("run2");

        Instant queuedFromTime = Instant.EPOCH;
        RasSearchCriteriaQueuedFrom queuedFrom = new RasSearchCriteriaQueuedFrom(queuedFromTime);

        RasSortField runNameSort = new RasSortField("runName", "desc");

        FoundRuns findRunsResponse = new FoundRuns();
        findRunsResponse.docs = List.of(mockRun1, mockRun2);
        findRunsResponse.bookmark = "bookmark!";

        FoundRuns emptyRunsResponse = new FoundRuns();
        emptyRunsResponse.docs = new ArrayList<>();

        String expectedUri = "http://my.uri/galasa_run/_find";
        List<HttpInteraction> interactions = List.of(
            new PostCouchdbFindRunsInteraction(
                expectedUri,
                findRunsResponse,
                "queued",
                "$gte",
                queuedFromTime.toString(),
                "sort",
                runNameSort.getFieldName(),
                runNameSort.getSortDirection()
            )
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        int maxResults = 100;

        // When...
        RasRunResultPage runsPage = directoryService.getRunsPage(maxResults, runNameSort, null, queuedFrom);

        // Then...
        assertThat(runsPage.getNextCursor()).isEqualTo(findRunsResponse.bookmark);

        List<IRunResult> runs = runsPage.getRuns();
        assertThat(runs).hasSize(2);
        assertThat(runs.get(0).getTestStructure().getRunName()).isEqualTo(mockRun1.getRunName());
        assertThat(runs.get(1).getTestStructure().getRunName()).isEqualTo(mockRun2.getRunName());
    }

    @Test
    public void testGetRunsPageByQueuedFromWithSortAndPageTokenReturnsRunsOk() throws Exception {
        // Given...
        TestStructureCouchdb mockRun1 = createRunTestStructure("run1");
        TestStructureCouchdb mockRun2 = createRunTestStructure("run2");

        Instant queuedFromTime = Instant.EPOCH;
        RasSearchCriteriaQueuedFrom queuedFrom = new RasSearchCriteriaQueuedFrom(queuedFromTime);

        RasSortField runNameSort = new RasSortField("runName", "desc");

        FoundRuns findRunsResponse = new FoundRuns();
        findRunsResponse.docs = List.of(mockRun1, mockRun2);
        findRunsResponse.bookmark = "bookmark!";

        FoundRuns emptyRunsResponse = new FoundRuns();
        emptyRunsResponse.docs = new ArrayList<>();

        String bookmarkToRequest = "iwantthispage";

        String expectedUri = "http://my.uri/galasa_run/_find";
        List<HttpInteraction> interactions = List.of(
            new PostCouchdbFindRunsInteraction(
                expectedUri,
                findRunsResponse,
                "queued",
                "$gte",
                queuedFromTime.toString(),
                "sort",
                runNameSort.getFieldName(),
                runNameSort.getSortDirection(),
                "bookmark",
                bookmarkToRequest
            )
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        int maxResults = 100;

        // When...
        RasRunResultPage runsPage = directoryService.getRunsPage(maxResults, runNameSort, bookmarkToRequest, queuedFrom);

        // Then...
        assertThat(runsPage.getNextCursor()).isEqualTo(findRunsResponse.bookmark);

        List<IRunResult> runs = runsPage.getRuns();
        assertThat(runs).hasSize(2);
        assertThat(runs.get(0).getTestStructure().getRunName()).isEqualTo(mockRun1.getRunName());
        assertThat(runs.get(1).getTestStructure().getRunName()).isEqualTo(mockRun2.getRunName());
    }

    @Test
    public void testGetRunsPageWithNilBookmarkReturnsPageWithNoNextCursor() throws Exception {
        // Given...
        TestStructureCouchdb mockRun1 = createRunTestStructure("run1");

        Instant queuedFromTime = Instant.EPOCH;
        RasSearchCriteriaQueuedFrom queuedFrom = new RasSearchCriteriaQueuedFrom(queuedFromTime);

        FoundRuns findRunsResponse = new FoundRuns();
        findRunsResponse.docs = List.of(mockRun1);
        findRunsResponse.bookmark = "nil";

        String expectedUri = "http://my.uri/galasa_run/_find";
        List<HttpInteraction> interactions = List.of(
            new PostCouchdbFindRunsInteraction(expectedUri, findRunsResponse, "queued", "$gte", queuedFromTime.toString())
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        int maxResults = 100;

        // When...
        RasRunResultPage runsPage = directoryService.getRunsPage(maxResults, null, null, queuedFrom);

        // Then...
        assertThat(runsPage.getNextCursor()).isNull();
    }

    //------------------------------------------
    //
    // Tests for deleting runs
    //
    //------------------------------------------

    @Test
    public void testDiscardRunDeletesRunOk() throws Exception {
        // Given...
        String runId = "ABC123";
        TestStructureCouchdb mockRun1 = createRunTestStructure(runId);

        IdRev mockIdRev = new IdRev();
        String revision = "this-is-a-revision";
        mockIdRev._id = "this-is-an-id";
        mockIdRev._rev = revision;

        String artifactId1 = "artifact1";
        String artifactId2 = "artifact2";
        List<String> mockArtifactIds = List.of(artifactId1, artifactId2);

        String logId1 = "log1"; 
        String logId2 = "log2"; 
        List<String> mockLogRecordIds = List.of(logId1, logId2);

        mockRun1.setArtifactRecordIds(mockArtifactIds);
        mockRun1.setLogRecordIds(mockLogRecordIds);

        String baseUri = "http://my.uri";
        String runDbUri = baseUri + "/" + CouchdbRasStore.RUNS_DB + "/" + runId;
        String artifactsDbUri = baseUri + "/" + CouchdbRasStore.ARTIFACTS_DB;
        String logsDbUri = baseUri + "/" + CouchdbRasStore.LOG_DB;
        List<HttpInteraction> interactions = List.of(            
            // Start discarding the run's log records
            new GetDocumentByIdFromCouchdbInteraction(logsDbUri + "/" + logId1, HttpStatus.SC_OK, mockIdRev),
            new DeleteDocumentFromCouchdbInteraction(logsDbUri + "/" + logId1 + "?rev=" + revision, HttpStatus.SC_OK),
            new GetDocumentByIdFromCouchdbInteraction(logsDbUri + "/" + logId2, HttpStatus.SC_OK, mockIdRev),
            new DeleteDocumentFromCouchdbInteraction(logsDbUri + "/" + logId2 + "?rev=" + revision, HttpStatus.SC_OK),
            
            // Start discarding the run's artifact records
            new GetDocumentByIdFromCouchdbInteraction(artifactsDbUri + "/" + artifactId1, HttpStatus.SC_OK, mockIdRev),
            new DeleteDocumentFromCouchdbInteraction(artifactsDbUri + "/" + artifactId1 + "?rev=" + revision, HttpStatus.SC_OK),
            new GetDocumentByIdFromCouchdbInteraction(artifactsDbUri + "/" + artifactId2, HttpStatus.SC_OK, mockIdRev),
            new DeleteDocumentFromCouchdbInteraction(artifactsDbUri + "/" + artifactId2 + "?rev=" + revision, HttpStatus.SC_OK),

            // Delete the record of the run
            new DeleteDocumentFromCouchdbInteraction(runDbUri + "?rev=" + mockRun1._rev, HttpStatus.SC_OK)
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        // When...
        directoryService.discardRun(mockRun1);

        // Then...
        // The assertions in the interactions should not have failed
    }

    @Test
    public void testDiscardRunWithNoArtifactsDeletesRunOk() throws Exception {
        // Given...
        String runId = "ABC123";
        TestStructureCouchdb mockRun1 = createRunTestStructure(runId);

        IdRev mockIdRev = new IdRev();
        String revision = "this-is-a-revision";
        mockIdRev._id = "this-is-an-id";
        mockIdRev._rev = revision;

        String logId1 = "log1"; 
        String logId2 = "log2"; 
        List<String> mockLogRecordIds = List.of(logId1, logId2);

        mockRun1.setLogRecordIds(mockLogRecordIds);

        String baseUri = "http://my.uri";
        String runDbUri = baseUri + "/" + CouchdbRasStore.RUNS_DB + "/" + runId;
        String logsDbUri = baseUri + "/" + CouchdbRasStore.LOG_DB;
        List<HttpInteraction> interactions = List.of(
            // Start discarding the run's log records
            new GetDocumentByIdFromCouchdbInteraction(logsDbUri + "/" + logId1, HttpStatus.SC_OK, mockIdRev),
            new DeleteDocumentFromCouchdbInteraction(logsDbUri + "/" + logId1 + "?rev=" + revision, HttpStatus.SC_OK),
            new GetDocumentByIdFromCouchdbInteraction(logsDbUri + "/" + logId2, HttpStatus.SC_OK, mockIdRev),
            new DeleteDocumentFromCouchdbInteraction(logsDbUri + "/" + logId2 + "?rev=" + revision, HttpStatus.SC_OK),

            // Delete the record of the run
            new DeleteDocumentFromCouchdbInteraction(runDbUri + "?rev=" + mockRun1._rev, HttpStatus.SC_OK)
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        // When...
        directoryService.discardRun(mockRun1);

        // Then...
        // The assertions in the interactions should not have failed
    }

    @Test
    public void testDiscardRunWithNoArtifactsAndLogsDeletesRunOk() throws Exception {
        // Given...
        String runId = "ABC123";
        TestStructureCouchdb mockRun1 = createRunTestStructure(runId);

        String baseUri = "http://my.uri";
        String runDbUri = baseUri + "/" + CouchdbRasStore.RUNS_DB + "/" + runId;
        List<HttpInteraction> interactions = List.of(
            // Delete the record of the run
            new DeleteDocumentFromCouchdbInteraction(runDbUri + "?rev=" + mockRun1._rev, HttpStatus.SC_OK)
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        // When...
        directoryService.discardRun(mockRun1);

        // Then...
        // The assertions in the interactions should not have failed
    }

    @Test
    public void testDiscardRunWithCouchdbServerErrorThrowsCorrectError() throws Exception {
        // Given...
        String runId = "ABC123";
        TestStructureCouchdb mockRun1 = createRunTestStructure(runId);

        String baseUri = "http://my.uri";
        String runDbUri = baseUri + "/" + CouchdbRasStore.RUNS_DB + "/" + runId;
        List<HttpInteraction> interactions = List.of(
            // Delete the record of the run
            new DeleteDocumentFromCouchdbInteraction(runDbUri + "?rev=" + mockRun1._rev, HttpStatus.SC_INTERNAL_SERVER_ERROR)
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        // When...
        ResultArchiveStoreException thrown = catchThrowableOfType(() -> {
            directoryService.discardRun(mockRun1);
        }, ResultArchiveStoreException.class);

        // Then...
        // The assertions in the interactions should not have failed
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Unable to delete run", runId);
    }
}
