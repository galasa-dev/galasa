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

import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.Test;

import dev.galasa.extensions.common.mocks.BaseHttpInteraction;
import dev.galasa.extensions.common.mocks.HttpInteraction;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.ras.couchdb.internal.mocks.CouchdbTestFixtures;
import dev.galasa.ras.couchdb.internal.mocks.MockLogFactory;
import dev.galasa.ras.couchdb.internal.pojos.LogLines;
import dev.galasa.ras.couchdb.internal.pojos.TestStructureCouchdb;

public class CouchdbRasStoreStreamLogTest {

    CouchdbTestFixtures fixtures = new CouchdbTestFixtures();

    class GetLogDocumentInteraction extends BaseHttpInteraction {

        public GetLogDocumentInteraction(String expectedUri, int statusCode, LogLines logLines) {
            super(expectedUri, statusCode);
            setResponsePayload(logLines);
        }

        @Override
        public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
            super.validateRequest(host, request);
            assertThat(request.getMethod()).isEqualTo("GET");
        }
    }

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
    public void testStreamLogWithEmptyLogRecordIdsReturnsEmptyStream() throws Exception {
        // Given...
        TestStructureCouchdb testStructure = createTestStructureWithLogIds(new ArrayList<>());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MockLogFactory mockLogFactory = new MockLogFactory();

        // Empty list, no HTTP calls expected
        List<HttpInteraction> interactions = new ArrayList<>();

        CouchdbRasStore rasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);

        // When...
        rasStore.streamLog(testStructure, outputStream);

        // Then...
        String result = outputStream.toString(StandardCharsets.UTF_8);
        assertThat(result).isEmpty();
    }

    @Test
    public void testStreamLogWithSingleLogDocumentWritesCorrectly() throws Exception {
        // Given...
        String logId1 = "log-doc-1";
        List<String> logIds = List.of(logId1);
        TestStructureCouchdb testStructure = createTestStructureWithLogIds(logIds);

        LogLines logLines1 = createLogLines("test-run-id", "TestRun", 1, 
            "Line 1 of log",
            "Line 2 of log",
            "Line 3 of log"
        );

        String baseUri = "http://my.uri";
        MockLogFactory mockLogFactory = new MockLogFactory();
        List<HttpInteraction> interactions = List.of(
            new GetLogDocumentInteraction(baseUri + "/" + CouchdbRasStore.LOG_DB + "/" + logId1, HttpStatus.SC_OK, logLines1)
        );

        CouchdbRasStore rasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When...
        rasStore.streamLog(testStructure, outputStream);

        // Then...
        String result = outputStream.toString(StandardCharsets.UTF_8);
        assertThat(result).isEqualTo("Line 1 of log\nLine 2 of log\nLine 3 of log");
    }

    @Test
    public void testStreamLogWithMultipleLogDocumentsWritesInOrder() throws Exception {
        // Given...
        String logId1 = "log-doc-1";
        String logId2 = "log-doc-2";
        String logId3 = "log-doc-3";
        List<String> logIds = List.of(logId1, logId2, logId3);
        TestStructureCouchdb testStructure = createTestStructureWithLogIds(logIds);

        LogLines logLines1 = createLogLines("test-run-id", "TestRun", 1, "First document line 1", "First document line 2");
        LogLines logLines2 = createLogLines("test-run-id", "TestRun", 2, "Second document line 1");
        LogLines logLines3 = createLogLines("test-run-id", "TestRun", 3, "Third document line 1", "Third document line 2", "Third document line 3");

        String baseUri = "http://my.uri";
        MockLogFactory mockLogFactory = new MockLogFactory();
        List<HttpInteraction> interactions = List.of(
            new GetLogDocumentInteraction(baseUri + "/" + CouchdbRasStore.LOG_DB + "/" + logId1, HttpStatus.SC_OK, logLines1),
            new GetLogDocumentInteraction(baseUri + "/" + CouchdbRasStore.LOG_DB + "/" + logId2, HttpStatus.SC_OK, logLines2),
            new GetLogDocumentInteraction(baseUri + "/" + CouchdbRasStore.LOG_DB + "/" + logId3, HttpStatus.SC_OK, logLines3)
        );

        CouchdbRasStore rasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When...
        rasStore.streamLog(testStructure, outputStream);

        // Then...
        String result = outputStream.toString(StandardCharsets.UTF_8);
        String expected = "First document line 1\n" +
                         "First document line 2\n" +
                         "Second document line 1\n" +
                         "Third document line 1\n" +
                         "Third document line 2\n" +
                         "Third document line 3";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testStreamLogWithNullLinesInDocumentSkipsDocument() throws Exception {
        // Given...
        String logId1 = "log-doc-1";
        String logId2 = "log-doc-2";
        List<String> logIds = List.of(logId1, logId2);
        TestStructureCouchdb testStructure = createTestStructureWithLogIds(logIds);

        LogLines logLines1 = createLogLines("test-run-id", "TestRun", 1, "Line 1");
        LogLines logLines2 = new LogLines();
        logLines2.runId = "test-run-id";
        logLines2.runName = "TestRun";
        logLines2.order = 2;
        logLines2.lines = null; // Null lines

        String baseUri = "http://my.uri";
        MockLogFactory mockLogFactory = new MockLogFactory();
        List<HttpInteraction> interactions = List.of(
            new GetLogDocumentInteraction(baseUri + "/" + CouchdbRasStore.LOG_DB + "/" + logId1, HttpStatus.SC_OK, logLines1),
            new GetLogDocumentInteraction(baseUri + "/" + CouchdbRasStore.LOG_DB + "/" + logId2, HttpStatus.SC_OK, logLines2)
        );

        CouchdbRasStore rasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When...
        rasStore.streamLog(testStructure, outputStream);

        // Then...
        String result = outputStream.toString(StandardCharsets.UTF_8);
        assertThat(result).isEqualTo("Line 1");
    }

    @Test
    public void testStreamLogWithEmptyLinesInDocumentSkipsDocument() throws Exception {
        // Given...
        String logId1 = "log-doc-1";
        String logId2 = "log-doc-2";
        List<String> logIds = List.of(logId1, logId2);
        TestStructureCouchdb testStructure = createTestStructureWithLogIds(logIds);

        LogLines logLines1 = createLogLines("test-run-id", "TestRun", 1, "Line 1");
        LogLines logLines2 = new LogLines();
        logLines2.runId = "test-run-id";
        logLines2.runName = "TestRun";
        logLines2.order = 2;
        logLines2.lines = new ArrayList<>();

        String baseUri = "http://my.uri";
        MockLogFactory mockLogFactory = new MockLogFactory();
        List<HttpInteraction> interactions = List.of(
            new GetLogDocumentInteraction(baseUri + "/" + CouchdbRasStore.LOG_DB + "/" + logId1, HttpStatus.SC_OK, logLines1),
            new GetLogDocumentInteraction(baseUri + "/" + CouchdbRasStore.LOG_DB + "/" + logId2, HttpStatus.SC_OK, logLines2)
        );

        CouchdbRasStore rasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When...
        rasStore.streamLog(testStructure, outputStream);

        // Then...
        String result = outputStream.toString(StandardCharsets.UTF_8);
        assertThat(result).isEqualTo("Line 1");
    }

    @Test
    public void testStreamLogThrowsExceptionWhenHttpRequestFails() throws Exception {
        // Given...
        String logId1 = "log-doc-1";
        List<String> logIds = List.of(logId1);
        TestStructureCouchdb testStructure = createTestStructureWithLogIds(logIds);

        String baseUri = "http://my.uri";
        MockLogFactory mockLogFactory = new MockLogFactory();
        List<HttpInteraction> interactions = List.of(
            new GetLogDocumentInteraction(baseUri + "/" + CouchdbRasStore.LOG_DB + "/" + logId1, HttpStatus.SC_INTERNAL_SERVER_ERROR, null)
        );

        CouchdbRasStore rasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When...
        Throwable thrown = catchThrowable(() -> {
            rasStore.streamLog(testStructure, outputStream);
        });

        // Then...
        assertThat(thrown)
            .isInstanceOf(ResultArchiveStoreException.class)
            .hasMessageContaining("Internal server error");
    }

    @Test
    public void testStreamLogHandlesSpecialCharactersCorrectly() throws Exception {
        // Given...
        String logId1 = "log-doc-1";
        List<String> logIds = List.of(logId1);
        TestStructureCouchdb testStructure = createTestStructureWithLogIds(logIds);

        // Test with ASCII special characters and common symbols
        LogLines logLines1 = createLogLines("test-run-id", "TestRun", 1,
            "Line with special chars: !@#$%^&*()",
            "Line with quotes: \"double\" and 'single'",
            "Line with brackets: [square] {curly} <angle>"
        );

        String baseUri = "http://my.uri";
        MockLogFactory mockLogFactory = new MockLogFactory();
        List<HttpInteraction> interactions = List.of(
            new GetLogDocumentInteraction(baseUri + "/" + CouchdbRasStore.LOG_DB + "/" + logId1, HttpStatus.SC_OK, logLines1)
        );

        CouchdbRasStore rasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When...
        rasStore.streamLog(testStructure, outputStream);

        // Then...
        String result = outputStream.toString(StandardCharsets.UTF_8);
        assertThat(result).contains("!@#$%^&*()");
        assertThat(result).contains("\"double\" and 'single'");
        assertThat(result).contains("[square] {curly} <angle>");
    }
}
