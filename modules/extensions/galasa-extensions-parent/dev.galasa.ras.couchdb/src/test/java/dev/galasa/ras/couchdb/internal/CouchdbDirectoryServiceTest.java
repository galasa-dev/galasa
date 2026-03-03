/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.google.gson.JsonObject;

import dev.galasa.extensions.common.couchdb.pojos.ViewResponse;
import dev.galasa.extensions.common.couchdb.pojos.ViewRow;
import dev.galasa.extensions.common.impl.HttpRequestFactoryImpl;
import dev.galasa.extensions.common.mocks.BaseHttpInteraction;
import dev.galasa.extensions.common.mocks.HttpInteraction;
import dev.galasa.extensions.common.mocks.MockCloseableHttpClient;
import dev.galasa.extensions.common.mocks.MockCloseableHttpResponse;
import dev.galasa.extensions.common.mocks.MockHttpEntity;
import dev.galasa.extensions.common.mocks.MockStatusLine;
import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
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
import dev.galasa.framework.spi.utils.GalasaGson;
import dev.galasa.ras.couchdb.internal.mocks.CouchdbTestFixtures;
import dev.galasa.ras.couchdb.internal.mocks.MockLogFactory;
import dev.galasa.ras.couchdb.internal.pojos.FoundRuns;
import dev.galasa.ras.couchdb.internal.pojos.TestStructureCouchdb;

public class CouchdbDirectoryServiceTest extends BaseCouchdbOperationTest {

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

    class GetRunsFromCouchdbViewInteraction extends BaseHttpInteraction {

        public GetRunsFromCouchdbViewInteraction(String expectedUri, int statusCode, ViewResponse viewResponse) {
            super(expectedUri, statusCode);
            setResponsePayload(viewResponse);
        }

        @Override
        public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
            super.validateRequest(host,request);
            assertThat(request.getRequestLine().getMethod()).isEqualTo("GET");
        }
    }

    //------------------------------------------
    //
    // Tests for getting runs by criteria
    //
    //------------------------------------------

    @Test
    public void testGetRunsByQueuedFromOnePageReturnsRunsOk() throws Exception {
        // Given...
        TestStructureCouchdb mockRun1 = createRunTestStructure("run1-id", "run1", "none");
        TestStructureCouchdb mockRun2 = createRunTestStructure("run2-id", "run2", "none");

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
        List<TestStructureCouchdb> firstPageRuns = new ArrayList<>();
        List<TestStructureCouchdb> secondPageRuns = new ArrayList<>();
        for (int i = 0; i < CouchdbDirectoryService.COUCHDB_RESULTS_LIMIT_PER_QUERY; i++) {
            firstPageRuns.add(createRunTestStructure("run" + i + "-id", "run" + i, "none"));

            int secondPageMockRunIndex = i + 1000;
            secondPageRuns.add(createRunTestStructure("run" + secondPageMockRunIndex + "-id", "run" + secondPageMockRunIndex, "none"));
        }

        Instant queuedFromTime = Instant.EPOCH;
        RasSearchCriteriaQueuedFrom queuedFrom = new RasSearchCriteriaQueuedFrom(queuedFromTime);

        FoundRuns findRunsResponsePage1 = new FoundRuns();
        findRunsResponsePage1.docs = firstPageRuns;
        findRunsResponsePage1.bookmark = "bookmark1";

        FoundRuns findRunsResponsePage2 = new FoundRuns();
        findRunsResponsePage2.docs = secondPageRuns;
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
        assertThat(runs).hasSize(200);
        assertThat(runs.get(0).getTestStructure().getRunName()).isEqualTo("run0");
        assertThat(runs.get(1).getTestStructure().getRunName()).isEqualTo("run1");
        assertThat(runs.get(2).getTestStructure().getRunName()).isEqualTo("run2");
    }

    @Test
    public void testGetRunsWithInvalidRunIgnoresRunOk() throws Exception {
        // Given...
        String runName1 = "run1";
        TestStructureCouchdb mockRun1 = createRunTestStructure("run1-id", runName1, "none");

        // No run name is set, so this is not a valid run
        TestStructureCouchdb invalidRun = createRunTestStructure("invalid-run", null, "none");

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
        assertThat(runs).hasSize(2);
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
        CouchdbRasException thrown = catchThrowableOfType(CouchdbRasException.class, () -> directoryService.getRuns(queuedFrom));

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
        CouchdbRasException thrown = catchThrowableOfType(CouchdbRasException.class, () -> directoryService.getRuns(queuedFrom));

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Unable to find runs", "Invalid JSON response");
    }

    @Test
    public void testGetRunsMultipleCriteriaReturnsRunsOk() throws Exception {
        // Given...
        TestStructureCouchdb mockRun1 = createRunTestStructure("run1-id", "run1", "none");
        TestStructureCouchdb mockRun2 = createRunTestStructure("run2-id", "run2", "none");

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
    public void testGetRunsPageByQueuedFromReturnsRunsOk() throws Exception {
        // Given...
        TestStructureCouchdb mockRun1 = createRunTestStructure("run1-id", "run1", "none");
        TestStructureCouchdb mockRun2 = createRunTestStructure("run2-id", "run2", "none");

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
        TestStructureCouchdb mockRun1 = createRunTestStructure("run1-id", "run1", "none");
        TestStructureCouchdb mockRun2 = createRunTestStructure("run2-id", "run2", "none");

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
        TestStructureCouchdb mockRun1 = createRunTestStructure("run1-id", "run1", "none");
        TestStructureCouchdb mockRun2 = createRunTestStructure("run2-id", "run2", "none");

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
        TestStructureCouchdb mockRun1 = createRunTestStructure("run1-id", "run1", "none");

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

    @Test
    public void testGetRunsPageWithMaxResultsZeroSetsLimitToIntMaxValue() throws Exception {
        // Given...
        TestStructureCouchdb mockRun1 = createRunTestStructure("run1-id", "run1", "none");

        Instant queuedFromTime = Instant.EPOCH;
        RasSearchCriteriaQueuedFrom queuedFrom = new RasSearchCriteriaQueuedFrom(queuedFromTime);

        FoundRuns findRunsResponse = new FoundRuns();
        findRunsResponse.docs = List.of(mockRun1);
        findRunsResponse.bookmark = "bookmark!";

        String expectedUri = "http://my.uri/galasa_run/_find";
        
        // The key assertion here is that the request body should contain Integer.MAX_VALUE as the limit
        String expectedLimitValue = String.valueOf(Integer.MAX_VALUE);
        List<HttpInteraction> interactions = List.of(
            new PostCouchdbFindRunsInteraction(expectedUri, findRunsResponse, "queued", "$gte", queuedFromTime.toString(), "limit", expectedLimitValue)
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        int maxResults = 0;

        // When...
        RasRunResultPage runsPage = directoryService.getRunsPage(maxResults, null, null, queuedFrom);

        // Then...
        // The assertions in the PostCouchdbFindRunsInteraction should verify that the limit
        // parameter in the request body is set to Integer.MAX_VALUE
        assertThat(runsPage.getNextCursor()).isEqualTo(findRunsResponse.bookmark);

        List<IRunResult> runs = runsPage.getRuns();
        assertThat(runs).hasSize(1);
        assertThat(runs.get(0).getTestStructure().getRunName()).isEqualTo(mockRun1.getRunName());
    }

    //------------------------------------------
    //
    // Tests for getting runs by run name
    //
    //------------------------------------------

    @Test
    public void testGetRunsByRunNameReturnsRunOk() throws Exception {
        // Given...
        String run1Id = "run1-id";
        String run1Name = "ABC123";
        String urlEncodedRun1Name = URLEncoder.encode('"' + run1Name + '"', StandardCharsets.UTF_8);
        TestStructureCouchdb mockRun1 = createRunTestStructure(run1Id, run1Name, "none");

        ViewResponse mockViewResponse = new ViewResponse();
        ViewRow run1Row = new ViewRow();
        run1Row.id = run1Id;
        run1Row.key = run1Name;
        run1Row.doc = mockRun1;

        List<ViewRow> mockRows = new ArrayList<>();
        mockRows.add(run1Row);
        mockViewResponse.rows = mockRows;

        String baseUri = "http://my.uri";
        String runNameViewUri = baseUri + "/" + CouchdbRasStore.RUNS_DB + "/_design/docs/_view/" + CouchdbRasStore.RUN_NAMES_VIEW_NAME;

        List<HttpInteraction> interactions = List.of(
            // Get the run document
            new GetRunsFromCouchdbViewInteraction(runNameViewUri + "?key=" + urlEncodedRun1Name + "&include_docs=true", HttpStatus.SC_OK, mockViewResponse)
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        MockCloseableHttpClient httpClient = new MockCloseableHttpClient(interactions);
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(mockLogFactory, httpClient);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        // When...
        List<IRunResult> runs = directoryService.getRunsByRunName(run1Name);

        // Then...
        // The assertions in the interactions should not have failed
        assertThat(runs).hasSize(1);

        IRunResult run = runs.get(0);
        assertThat(run.getRunId()).isEqualTo("cdb-" + run1Id);
        assertThat((TestStructureCouchdb)run.getTestStructure()).usingRecursiveComparison().isEqualTo(mockRun1);
    }

    @Test
    public void testGetRunsByRunNameWithNoDocumentIncludedReturnsNoRunsOk() throws Exception {
        // Given...
        String run1Id = "run1-id";
        String run1Name = "ABC123";
        String urlEncodedRun1Name = URLEncoder.encode('"' + run1Name + '"', StandardCharsets.UTF_8);

        ViewResponse mockViewResponse = new ViewResponse();
        ViewRow run1Row = new ViewRow();
        run1Row.id = run1Id;
        run1Row.key = run1Name;
        
        // Simulate a scenario where no document is included in the response
        run1Row.doc = null;

        List<ViewRow> mockRows = new ArrayList<>();
        mockRows.add(run1Row);
        mockViewResponse.rows = mockRows;

        String baseUri = "http://my.uri";
        String runNameViewUri = baseUri + "/" + CouchdbRasStore.RUNS_DB + "/_design/docs/_view/" + CouchdbRasStore.RUN_NAMES_VIEW_NAME;

        List<HttpInteraction> interactions = List.of(
            // Get the run document
            new GetRunsFromCouchdbViewInteraction(runNameViewUri + "?key=" + urlEncodedRun1Name + "&include_docs=true", HttpStatus.SC_OK, mockViewResponse)
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        MockCloseableHttpClient httpClient = new MockCloseableHttpClient(interactions);
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(mockLogFactory, httpClient);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        // When...
        List<IRunResult> runs = directoryService.getRunsByRunName(run1Name);

        // Then...
        // The assertions in the interactions should not have failed
        assertThat(runs).isEmpty();
    }

    @Test
    public void testGetRunsByRunNameWithBadlyFormattedJsonResponseThrowsCorrectError() throws Exception {
        // Given...
        String run1Name = "ABC123";
        String urlEncodedRun1Name = URLEncoder.encode('"' + run1Name + '"', StandardCharsets.UTF_8);

        ViewResponse mockViewResponse = new ViewResponse();
        mockViewResponse.rows = null;

        String baseUri = "http://my.uri";
        String runNameViewUri = baseUri + "/" + CouchdbRasStore.RUNS_DB + "/_design/docs/_view/" + CouchdbRasStore.RUN_NAMES_VIEW_NAME;

        List<HttpInteraction> interactions = List.of(
            // Get the run document
            new GetRunsFromCouchdbViewInteraction(runNameViewUri + "?key=" + urlEncodedRun1Name + "&include_docs=true", HttpStatus.SC_OK, mockViewResponse)
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        MockCloseableHttpClient httpClient = new MockCloseableHttpClient(interactions);
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(mockLogFactory, httpClient);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        // When...
        ResultArchiveStoreException thrown = catchThrowableOfType(ResultArchiveStoreException.class, () -> {
            directoryService.getRunsByRunName(run1Name);
        });

        // Then...
        // The assertions in the interactions should not have failed
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains(
            "GAL6013E",
            CouchdbRasStore.RUN_NAMES_VIEW_NAME,
            CouchdbRasStore.RUNS_DB,
            "Invalid JSON response returned from CouchDB"
        );
    }

    //------------------------------------------
    //
    // Tests for getting runs by group
    //
    //------------------------------------------

    @Test
    public void testGetRunsByGroupReturnsRunOk() throws Exception {
        // Given...
        String run1Id = "run1-id";
        String run1Name = "ABC123";
        String groupName = "testGroup";
        String urlEncodedGroupName = URLEncoder.encode('"' + groupName + '"', StandardCharsets.UTF_8);
        TestStructureCouchdb mockRun1 = createRunTestStructure(run1Id, run1Name, groupName);

        ViewResponse mockViewResponse = new ViewResponse();
        ViewRow run1Row = new ViewRow();
        run1Row.id = run1Id;
        run1Row.key = run1Name;
        run1Row.doc = mockRun1;

        List<ViewRow> mockRows = new ArrayList<>();
        mockRows.add(run1Row);
        mockViewResponse.rows = mockRows;

        String baseUri = "http://my.uri";
        String groupNameViewUri = baseUri + "/" + CouchdbRasStore.RUNS_DB + "/_design/docs/_view/" + CouchdbRasStore.RUN_GROUP_VIEW_NAME;

        List<HttpInteraction> interactions = List.of(
            // Get the run document
            new GetRunsFromCouchdbViewInteraction(groupNameViewUri + "?key=" + urlEncodedGroupName + "&include_docs=true", HttpStatus.SC_OK, mockViewResponse)
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        MockCloseableHttpClient httpClient = new MockCloseableHttpClient(interactions);
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(mockLogFactory, httpClient);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        // When...
        List<IRunResult> runs = directoryService.getRunsByGroupName(groupName);

        // Then...
        // The assertions in the interactions should not have failed
        assertThat(runs).hasSize(1);

        IRunResult run = runs.get(0);
        assertThat(run.getRunId()).isEqualTo("cdb-" + run1Id);
        assertThat((TestStructureCouchdb)run.getTestStructure()).usingRecursiveComparison().isEqualTo(mockRun1);
        assertThat((String)run.getTestStructure().getGroup()).isEqualTo(groupName);
    }

    //------------------------------------------
    //
    // Tests for getRunArtifactPath methods
    //
    //------------------------------------------

    @Test
    public void testGetRunArtifactPathWithNoArtifactRecordIdsReturnsEmptyRoot() throws Exception {
        // Given...
        TestStructureCouchdb mockRun = createRunTestStructure("run1-id", "run1", "none");
        mockRun.setArtifactRecordIds(null);

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(List.of(), mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        // When...
        java.nio.file.Path artifactPath = directoryService.getRunArtifactPath(mockRun);

        // Then...
        assertThat(artifactPath).isNotNull();
        assertThat(artifactPath.toString()).isEqualTo("/");
    }

    @Test
    public void testGetRunArtifactPathWithEmptyArtifactRecordIdsReturnsEmptyRoot() throws Exception {
        // Given...
        TestStructureCouchdb mockRun = createRunTestStructure("run1-id", "run1", "none");
        mockRun.setArtifactRecordIds(new ArrayList<>());

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(List.of(), mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        // When...
        java.nio.file.Path artifactPath = directoryService.getRunArtifactPath(mockRun);

        // Then...
        assertThat(artifactPath).isNotNull();
        assertThat(artifactPath.toString()).isEqualTo("/");
    }

    @Test
    public void testGetRunArtifactPathWithNotFoundArtifactRecordReturnsEmptyRoot() throws Exception {
        // Given...
        TestStructureCouchdb mockRun = createRunTestStructure("run1-id", "run1", "none");
        mockRun.setArtifactRecordIds(List.of("artifact-id-1"));

        String expectedUri = "http://my.uri/galasa_artifacts/artifact-id-1";
        List<HttpInteraction> interactions = List.of(
            new BaseHttpInteraction(expectedUri, HttpStatus.SC_NOT_FOUND) {
                @Override
                public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
                    super.validateRequest(host, request);
                    assertThat(request.getRequestLine().getMethod()).isEqualTo("GET");
                }
            }
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        // When...
        java.nio.file.Path artifactPath = directoryService.getRunArtifactPath(mockRun);

        // Then...
        assertThat(artifactPath).isNotNull();
        assertThat(artifactPath.toString()).isEqualTo("/");
    }

    @Test
    public void testGetRunArtifactPathWithSpecificArtifactNotFoundReturnsEmptyRoot() throws Exception {
        // Given...
        TestStructureCouchdb mockRun = createRunTestStructure("run1-id", "run1", "none");
        mockRun.setArtifactRecordIds(List.of("artifact-id-1"));

        String expectedUri = "http://my.uri/galasa_artifacts/artifact-id-1";
        List<HttpInteraction> interactions = List.of(
            new BaseHttpInteraction(expectedUri, HttpStatus.SC_NOT_FOUND) {
                @Override
                public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
                    super.validateRequest(host, request);
                    assertThat(request.getRequestLine().getMethod()).isEqualTo("GET");
                }
            }
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        // When...
        java.nio.file.Path artifactPath = directoryService.getRunArtifactPath(mockRun, "specific-artifact.txt");

        // Then...
        assertThat(artifactPath).isNotNull();
        assertThat(artifactPath.toString()).isEqualTo("/");
    }

    @Test
    public void testGetRunArtifactPathWithErrorResponseThrowsException() throws Exception {
        // Given...
        TestStructureCouchdb mockRun = createRunTestStructure("run1-id", "run1", "none");
        mockRun.setArtifactRecordIds(List.of("artifact-id-1"));

        String expectedUri = "http://my.uri/galasa_artifacts/artifact-id-1";
        List<HttpInteraction> interactions = List.of(
            new BaseHttpInteraction(expectedUri, HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                @Override
                public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
                    super.validateRequest(host, request);
                    assertThat(request.getRequestLine().getMethod()).isEqualTo("GET");
                }
            }
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        // When...
        CouchdbRasException thrown = catchThrowableOfType(
            CouchdbRasException.class,
            () -> directoryService.getRunArtifactPath(mockRun)
        );

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Unable to find artifacts");
    }

    @Test
    public void testGetRunArtifactPathWithSpecificArtifactStopsSearchingAfterFound() throws Exception {
        // Given...
        TestStructureCouchdb mockRun = createRunTestStructure("run1-id", "run1", "none");
        mockRun.setArtifactRecordIds(List.of("artifact-id-1", "artifact-id-2", "artifact-id-3"));

        JsonObject artifactRecord1 = new JsonObject();
        artifactRecord1.addProperty("_id", "artifact-id-1");
        artifactRecord1.addProperty("_rev", "1-abc");
        
        JsonObject attachments = new JsonObject();
        JsonObject file1 = new JsonObject();
        file1.addProperty("content_type", "text/plain");
        file1.addProperty("length", 100);
        attachments.add("file1.txt", file1);
        
        JsonObject targetFile = new JsonObject();
        targetFile.addProperty("content_type", "text/plain");
        targetFile.addProperty("length", 200);
        attachments.add("target.txt", targetFile);
        
        artifactRecord1.add("_attachments", attachments);
        
        GalasaGson gson = new GalasaGson();
        String artifactRecord1Json = gson.toJson(artifactRecord1);

        // Only the first artifact record should be requested since we find the target artifact there
        List<HttpInteraction> interactions = List.of(
            new BaseHttpInteraction("http://my.uri/galasa_artifacts/artifact-id-1", HttpStatus.SC_OK) {
                @Override
                public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
                    super.validateRequest(host, request);
                    assertThat(request.getRequestLine().getMethod()).isEqualTo("GET");
                }

                @Override
                public MockCloseableHttpResponse getResponse() {
                    MockCloseableHttpResponse response = new MockCloseableHttpResponse();
                    MockStatusLine statusLine = new MockStatusLine();
                    statusLine.setStatusCode(HttpStatus.SC_OK);
                    response.setStatusLine(statusLine);
                    response.setEntity(new MockHttpEntity(artifactRecord1Json));
                    return response;
                }
            }
            // No interactions for artifact-id-2 or artifact-id-3 should occur
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        // When...
        java.nio.file.Path artifactPath = directoryService.getRunArtifactPath(mockRun, "target.txt");

        // Then...
        assertThat(artifactPath).isNotNull();
        // The mock interactions will fail if artifact-id-2 or artifact-id-3 are requested
    }

    @Test
    public void testGetRunArtifactPathLoadsAllArtifactsFromMultipleRecords() throws Exception {
        // Given...
        TestStructureCouchdb mockRun = createRunTestStructure("run1-id", "run1", "none");
        mockRun.setArtifactRecordIds(List.of("artifact-id-1", "artifact-id-2"));

        GalasaGson gson = new GalasaGson();

        JsonObject artifactRecord1 = new JsonObject();
        artifactRecord1.addProperty("_id", "artifact-id-1");
        artifactRecord1.addProperty("_rev", "1-abc");

        JsonObject attachments1 = new JsonObject();
        JsonObject file1 = new JsonObject();
        file1.addProperty("content_type", "text/plain");
        file1.addProperty("length", 100);
        attachments1.add("file1.txt", file1);

        artifactRecord1.add("_attachments", attachments1);
        String artifactRecord1Json = gson.toJson(artifactRecord1);

        JsonObject artifactRecord2 = new JsonObject();
        artifactRecord2.addProperty("_id", "artifact-id-2");
        artifactRecord2.addProperty("_rev", "1-def");

        JsonObject attachments2 = new JsonObject();
        JsonObject file2 = new JsonObject();
        file2.addProperty("content_type", "text/plain");
        file2.addProperty("length", 200);
        attachments2.add("file2.txt", file2);

        artifactRecord2.add("_attachments", attachments2);
        String artifactRecord2Json = gson.toJson(artifactRecord2);

        List<HttpInteraction> interactions = List.of(
            new BaseHttpInteraction("http://my.uri/galasa_artifacts/artifact-id-1", HttpStatus.SC_OK) {
                @Override
                public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
                    super.validateRequest(host, request);
                    assertThat(request.getRequestLine().getMethod()).isEqualTo("GET");
                }

                @Override
                public MockCloseableHttpResponse getResponse() {
                    MockCloseableHttpResponse response = new MockCloseableHttpResponse();
                    MockStatusLine statusLine = new MockStatusLine();
                    statusLine.setStatusCode(HttpStatus.SC_OK);
                    response.setStatusLine(statusLine);
                    response.setEntity(new MockHttpEntity(artifactRecord1Json));
                    return response;
                }
            },
            new BaseHttpInteraction("http://my.uri/galasa_artifacts/artifact-id-2", HttpStatus.SC_OK) {
                @Override
                public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
                    super.validateRequest(host, request);
                    assertThat(request.getRequestLine().getMethod()).isEqualTo("GET");
                }

                @Override
                public MockCloseableHttpResponse getResponse() {
                    MockCloseableHttpResponse response = new MockCloseableHttpResponse();
                    MockStatusLine statusLine = new MockStatusLine();
                    statusLine.setStatusCode(HttpStatus.SC_OK);
                    response.setStatusLine(statusLine);
                    response.setEntity(new MockHttpEntity(artifactRecord2Json));
                    return response;
                }
            }
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        // When...
        java.nio.file.Path artifactPath = directoryService.getRunArtifactPath(mockRun);

        // Then...
        assertThat(artifactPath).isNotNull();
        // Both artifact records should have been requested (verified by the interactions)
    }

    @Test
    public void testGetRunArtifactPathWithSpecificArtifactInSecondRecord() throws Exception {
        // Given...
        TestStructureCouchdb mockRun = createRunTestStructure("run1-id", "run1", "none");
        mockRun.setArtifactRecordIds(List.of("artifact-id-1", "artifact-id-2"));

        GalasaGson gson = new GalasaGson();

        JsonObject artifactRecord1 = new JsonObject();
        artifactRecord1.addProperty("_id", "artifact-id-1");
        artifactRecord1.addProperty("_rev", "1-abc");

        JsonObject attachments1 = new JsonObject();
        JsonObject file1 = new JsonObject();
        file1.addProperty("content_type", "text/plain");
        file1.addProperty("length", 100);
        attachments1.add("file1.txt", file1);

        artifactRecord1.add("_attachments", attachments1);
        String artifactRecord1Json = gson.toJson(artifactRecord1);

        JsonObject artifactRecord2 = new JsonObject();
        artifactRecord2.addProperty("_id", "artifact-id-2");
        artifactRecord2.addProperty("_rev", "1-def");

        JsonObject attachments2 = new JsonObject();
        JsonObject targetFile = new JsonObject();
        targetFile.addProperty("content_type", "text/plain");
        targetFile.addProperty("length", 200);
        attachments2.add("target.txt", targetFile);

        artifactRecord2.add("_attachments", attachments2);
        String artifactRecord2Json = gson.toJson(artifactRecord2);

        List<HttpInteraction> interactions = List.of(
            new BaseHttpInteraction("http://my.uri/galasa_artifacts/artifact-id-1", HttpStatus.SC_OK) {
                @Override
                public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
                    super.validateRequest(host, request);
                    assertThat(request.getRequestLine().getMethod()).isEqualTo("GET");
                }

                @Override
                public MockCloseableHttpResponse getResponse() {
                    MockCloseableHttpResponse response = new MockCloseableHttpResponse();
                    MockStatusLine statusLine = new MockStatusLine();
                    statusLine.setStatusCode(HttpStatus.SC_OK);
                    response.setStatusLine(statusLine);
                    response.setEntity(new MockHttpEntity(artifactRecord1Json));
                    return response;
                }
            },
            new BaseHttpInteraction("http://my.uri/galasa_artifacts/artifact-id-2", HttpStatus.SC_OK) {
                @Override
                public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
                    super.validateRequest(host, request);
                    assertThat(request.getRequestLine().getMethod()).isEqualTo("GET");
                }

                @Override
                public MockCloseableHttpResponse getResponse() {
                    MockCloseableHttpResponse response = new MockCloseableHttpResponse();
                    MockStatusLine statusLine = new MockStatusLine();
                    statusLine.setStatusCode(HttpStatus.SC_OK);
                    response.setStatusLine(statusLine);
                    response.setEntity(new MockHttpEntity(artifactRecord2Json));
                    return response;
                }
            }
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDirectoryService directoryService = new CouchdbDirectoryService(mockRasStore, mockLogFactory, new HttpRequestFactoryImpl());

        // When...
        java.nio.file.Path artifactPath = directoryService.getRunArtifactPath(mockRun, "target.txt");

        // Then...
        assertThat(artifactPath).isNotNull();
        // Both artifact records should have been requested since target.txt is in the second one
    }
}
