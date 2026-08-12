/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.Test;

import dev.galasa.extensions.common.mocks.HttpInteraction;
import dev.galasa.ras.couchdb.internal.mocks.CouchdbTestFixtures;
import dev.galasa.ras.couchdb.internal.mocks.MockLogFactory;
import dev.galasa.ras.couchdb.internal.pojos.LogLines;
import dev.galasa.ras.couchdb.internal.pojos.TestStructureCouchdb;

public class CouchdbRunResultTest {

    CouchdbTestFixtures fixtures = new CouchdbTestFixtures();

    private TestStructureCouchdb createTestStructureWithLogIds(List<String> logIds) {
        TestStructureCouchdb testStructure = new TestStructureCouchdb();
        testStructure._id = "test-run-id";
        testStructure._rev = "test-revision";
        testStructure.setRunName("TestRun");
        testStructure.setLogRecordIds(logIds);
        return testStructure;
    }

    private LogLines createLogLines(String runId, String runName, long order, String... lines) {
        LogLines logLines = new LogLines();
        logLines.runId = runId;
        logLines.runName = runName;
        logLines.order = order;
        logLines.lines = List.of(lines);
        return logLines;
    }

    @Test
    public void testStreamLogDelegatesToStore() throws Exception {
        // Given...
        String logId1 = "log-doc-1";
        List<String> logIds = List.of(logId1);
        TestStructureCouchdb testStructure = createTestStructureWithLogIds(logIds);

        LogLines logLines1 = createLogLines("test-run-id", "TestRun", 1, 
            "Line 1 from CouchdbRunResult",
            "Line 2 from CouchdbRunResult"
        );

        String baseUri = "http://my.uri";
        MockLogFactory mockLogFactory = new MockLogFactory();
        List<HttpInteraction> interactions = List.of(
            new CouchdbRasStoreStreamLogTest().new GetLogDocumentInteraction(
                baseUri + "/" + CouchdbRasStore.LOG_DB + "/" + logId1, 
                HttpStatus.SC_OK, 
                logLines1
            )
        );

        CouchdbRasStore rasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbRunResult runResult = new CouchdbRunResult(rasStore, testStructure, mockLogFactory);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When...
        runResult.streamLog(outputStream);

        // Then...
        String result = outputStream.toString(StandardCharsets.UTF_8);
        assertThat(result).isEqualTo("Line 1 from CouchdbRunResult\nLine 2 from CouchdbRunResult");
    }

    @Test
    public void testStreamLogWithEmptyLogIds() throws Exception {
        // Given...
        TestStructureCouchdb testStructure = createTestStructureWithLogIds(List.of());

        MockLogFactory mockLogFactory = new MockLogFactory();
        List<HttpInteraction> interactions = new ArrayList<>();
        CouchdbRasStore rasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbRunResult runResult = new CouchdbRunResult(rasStore, testStructure, mockLogFactory);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When...
        runResult.streamLog(outputStream);

        // Then...
        String result = outputStream.toString(StandardCharsets.UTF_8);
        assertThat(result).isEmpty();
    }

    @Test
    public void testGetRunIdReturnsCorrectFormat() throws Exception {
        // Given...
        TestStructureCouchdb testStructure = new TestStructureCouchdb();
        testStructure._id = "my-doc-id";
        testStructure._rev = "my-revision";

        MockLogFactory mockLogFactory = new MockLogFactory();
        List<HttpInteraction> interactions = new ArrayList<>();
        CouchdbRasStore rasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbRunResult runResult = new CouchdbRunResult(rasStore, testStructure, mockLogFactory);

        // When...
        String runId = runResult.getRunId();

        // Then...
        assertThat(runId).isEqualTo("cdb-my-doc-id");
    }

    @Test
    public void testGetLogSizeReturnsMetadataSize() throws Exception {
        // Given...
        TestStructureCouchdb testStructure = new TestStructureCouchdb();
        testStructure._id = "test-run-id";
        testStructure._rev = "test-revision";
        testStructure.setLogSize(Long.valueOf(12345L));

        MockLogFactory mockLogFactory = new MockLogFactory();
        List<HttpInteraction> interactions = new ArrayList<>();
        CouchdbRasStore rasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbRunResult runResult = new CouchdbRunResult(rasStore, testStructure, mockLogFactory);

        // When...
        long logSize = runResult.getLogSize();

        // Then...
        assertThat(logSize).isEqualTo(12345L);
    }

    @Test
    public void testGetLogSizeReturnsMinusOneWhenNoMetadata() throws Exception {
        // Given...
        TestStructureCouchdb testStructure = new TestStructureCouchdb();
        testStructure._id = "test-run-id";
        testStructure._rev = "test-revision";
        // No log size set (null)

        MockLogFactory mockLogFactory = new MockLogFactory();
        List<HttpInteraction> interactions = new ArrayList<>();
        CouchdbRasStore rasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbRunResult runResult = new CouchdbRunResult(rasStore, testStructure, mockLogFactory);

        // When...
        long logSize = runResult.getLogSize();

        // Then...
        assertThat(logSize).isEqualTo(-1L);
    }

    @Test
    public void testGetLogSizeReturnsZeroForEmptyLog() throws Exception {
        // Given...
        TestStructureCouchdb testStructure = new TestStructureCouchdb();
        testStructure._id = "test-run-id";
        testStructure._rev = "test-revision";
        testStructure.setLogSize(Long.valueOf(0L));

        MockLogFactory mockLogFactory = new MockLogFactory();
        List<HttpInteraction> interactions = new ArrayList<>();
        CouchdbRasStore rasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        CouchdbRunResult runResult = new CouchdbRunResult(rasStore, testStructure, mockLogFactory);

        // When...
        long logSize = runResult.getLogSize();

        // Then...
        assertThat(logSize).isEqualTo(0L);
    }
}
