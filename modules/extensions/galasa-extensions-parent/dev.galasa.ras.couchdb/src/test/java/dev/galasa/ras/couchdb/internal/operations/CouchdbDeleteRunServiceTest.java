/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal.operations;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.junit.Test;

import dev.galasa.extensions.common.couchdb.pojos.IdRev;
import dev.galasa.extensions.common.mocks.BaseHttpInteraction;
import dev.galasa.extensions.common.mocks.HttpInteraction;
import dev.galasa.extensions.common.mocks.MockAsyncCloseableHttpClient;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.ras.couchdb.internal.BaseCouchdbOperationTest;
import dev.galasa.ras.couchdb.internal.CouchdbRasStore;
import dev.galasa.ras.couchdb.internal.mocks.CouchdbTestFixtures;
import dev.galasa.ras.couchdb.internal.mocks.MockLogFactory;
import dev.galasa.ras.couchdb.internal.pojos.TestStructureCouchdb;

public class CouchdbDeleteRunServiceTest extends BaseCouchdbOperationTest {

    private CouchdbTestFixtures fixtures = new CouchdbTestFixtures();

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

    @Test
    public void testDiscardRunDeletesRunOk() throws Exception {
        // Given...
        String runId = "ABC123";
        TestStructureCouchdb mockRun1 = createRunTestStructure(runId, "run1");

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
        MockAsyncCloseableHttpClient httpClient = new MockAsyncCloseableHttpClient(interactions);
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(mockLogFactory, httpClient);
        CouchdbDeleteRunService deleteRunOperation = new CouchdbDeleteRunService(mockRasStore);

        // When...
        deleteRunOperation.discardRun(mockRun1);

        // Then...
        // The assertions in the interactions should not have failed
    }

    @Test
    public void testDiscardRunWithNoArtifactsDeletesRunOk() throws Exception {
        // Given...
        String runId = "ABC123";
        TestStructureCouchdb mockRun1 = createRunTestStructure(runId, "run1");

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
        MockAsyncCloseableHttpClient httpClient = new MockAsyncCloseableHttpClient(interactions);
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(mockLogFactory, httpClient);
        CouchdbDeleteRunService deleteRunOperation = new CouchdbDeleteRunService(mockRasStore);

        // When...
        deleteRunOperation.discardRun(mockRun1);

        // Then...
        // The assertions in the interactions should not have failed
    }

    @Test
    public void testDiscardRunWithNoArtifactsAndLogsDeletesRunOk() throws Exception {
        // Given...
        String runId = "ABC123";
        TestStructureCouchdb mockRun1 = createRunTestStructure(runId, "run1");

        String baseUri = "http://my.uri";
        String runDbUri = baseUri + "/" + CouchdbRasStore.RUNS_DB + "/" + runId;
        List<HttpInteraction> interactions = List.of(
            // Delete the record of the run
            new DeleteDocumentFromCouchdbInteraction(runDbUri + "?rev=" + mockRun1._rev, HttpStatus.SC_OK)
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbDeleteRunService deleteRunOperation = new CouchdbDeleteRunService(mockRasStore);

        // When...
        deleteRunOperation.discardRun(mockRun1);

        // Then...
        // The assertions in the interactions should not have failed
    }

    @Test
    public void testDiscardRunWithCouchdbServerErrorThrowsCorrectError() throws Exception {
        // Given...
        String runId = "ABC123";
        TestStructureCouchdb mockRun1 = createRunTestStructure(runId, "run1");

        String baseUri = "http://my.uri";
        String runDbUri = baseUri + "/" + CouchdbRasStore.RUNS_DB + "/" + runId;
        List<HttpInteraction> interactions = List.of(
            // Delete the record of the run
            new DeleteDocumentFromCouchdbInteraction(runDbUri + "?rev=" + mockRun1._rev, HttpStatus.SC_INTERNAL_SERVER_ERROR)
        );

        MockLogFactory mockLogFactory = new MockLogFactory();
        MockAsyncCloseableHttpClient httpClient = new MockAsyncCloseableHttpClient(interactions);
        CouchdbRasStore mockRasStore = fixtures.createCouchdbRasStore(mockLogFactory, httpClient);
        CouchdbDeleteRunService deleteRunOperation = new CouchdbDeleteRunService(mockRasStore);

        // When...
        ResultArchiveStoreException thrown = catchThrowableOfType(() -> {
            deleteRunOperation.discardRun(mockRun1);
        }, ResultArchiveStoreException.class);

        // Then...
        // The assertions in the interactions should not have failed
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Failed to discard run", runId);
        assertThat(thrown.getCause().getMessage()).contains(
            "GAL6007E",
            "Internal server error",
            "Unexpected response received from CouchDB server",
            runId
        );
    }
}
