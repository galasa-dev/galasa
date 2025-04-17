/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import dev.galasa.extensions.common.couchdb.pojos.IdRev;
import dev.galasa.extensions.common.couchdb.pojos.PutPostResponse;
import dev.galasa.extensions.common.mocks.BaseHttpInteraction;
import dev.galasa.extensions.common.mocks.HttpInteraction;
import dev.galasa.framework.spi.teststructure.TestStructure;
import dev.galasa.ras.couchdb.internal.mocks.CouchdbTestFixtures;
import dev.galasa.ras.couchdb.internal.mocks.MockLogFactory;

public class CouchdbRasStoreTest {

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

    class UpdateCouchdbDocumentInteraction extends BaseHttpInteraction {

        private String[] expectedRequestBodyParts;

        public UpdateCouchdbDocumentInteraction(String expectedUri, int statusCode, PutPostResponse response, String... expectedRequestBodyParts) {
            super(expectedUri, statusCode);
            setResponsePayload(response);
            this.expectedRequestBodyParts = expectedRequestBodyParts;
        }

        @Override
        public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
            super.validateRequest(host,request);
            assertThat(request.getRequestLine().getMethod()).isEqualTo("PUT");
            if (expectedRequestBodyParts.length > 0) {
                validatePutRequestBody((HttpPut) request);
            }
        }

        private void validatePutRequestBody(HttpPut putRequest) {
            try {
                String requestBody = EntityUtils.toString(putRequest.getEntity());
                assertThat(requestBody).contains(expectedRequestBodyParts);

            } catch (IOException ex) {
                fail("Failed to parse PUT request body");
            }
        }
    }

    CouchdbTestFixtures fixtures = new CouchdbTestFixtures();    

    private TestStructure createTestStructure(String runName, String status) {
        TestStructure testStructure = new TestStructure();

        testStructure.setRunName(runName);
        testStructure.setStatus(status);

        return testStructure;
    }


    // Creating the Ras store causes the test structure in the couchdb 
    @Test
    public void testCanCreateCouchdbRasStoreOK() throws Exception {

        // See if we can create a store...
        fixtures.createCouchdbRasStore(null);
    }

    @Test
    public void testCanUpdateTestStructureOK() throws Exception {
        // Given...
        String runId = "cdb-run1";
        String docId = "run1";
        String runName = "BOB1";
        String status = "finished";
        TestStructure newTestStructure = createTestStructure(runName, status);

        IdRev mockIdRev = new IdRev();
        mockIdRev._id = "id1";
        mockIdRev._rev = "my-revision";

        PutPostResponse mockPutResponse = new PutPostResponse();
        mockPutResponse.id = docId;
        mockPutResponse.rev = mockIdRev._rev;
        mockPutResponse.ok = true;

        String baseUri = "http://my.uri";
        MockLogFactory mockLogFactory = new MockLogFactory();
        List<HttpInteraction> interactions = List.of(
            new GetDocumentByIdFromCouchdbInteraction(baseUri + "/" + CouchdbRasStore.RUNS_DB + "/" + docId, HttpStatus.SC_OK, mockIdRev),
            new UpdateCouchdbDocumentInteraction(baseUri + "/" + CouchdbRasStore.RUNS_DB + "/" + docId, HttpStatus.SC_CREATED, mockPutResponse, runName, status)
        );

        CouchdbRasStore rasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);

        // When...
        rasStore.updateTestStructure(runId, newTestStructure);

        // Then...
        // None of the interaction assertions should have failed.
    }

    @Test
    public void testUpdateTestStructureRetriesOnConflict() throws Exception {
        // Given...
        String runId = "cdb-run1";
        String docId = "run1";
        String runName = "BOB1";
        String status = "finished";
        TestStructure newTestStructure = createTestStructure(runName, status);

        IdRev mockIdRev = new IdRev();
        mockIdRev._id = "id1";
        mockIdRev._rev = "my-revision";

        PutPostResponse mockPutResponse = new PutPostResponse();
        mockPutResponse.id = docId;
        mockPutResponse.rev = mockIdRev._rev;
        mockPutResponse.ok = true;

        String baseUri = "http://my.uri";
        MockLogFactory mockLogFactory = new MockLogFactory();
        List<HttpInteraction> interactions = List.of(
            new GetDocumentByIdFromCouchdbInteraction(baseUri + "/" + CouchdbRasStore.RUNS_DB + "/" + docId, HttpStatus.SC_OK, mockIdRev),
            new UpdateCouchdbDocumentInteraction(baseUri + "/" + CouchdbRasStore.RUNS_DB + "/" + docId, HttpStatus.SC_CONFLICT, null, runName, status),
            new UpdateCouchdbDocumentInteraction(baseUri + "/" + CouchdbRasStore.RUNS_DB + "/" + docId, HttpStatus.SC_CONFLICT, null, runName, status),
            new UpdateCouchdbDocumentInteraction(baseUri + "/" + CouchdbRasStore.RUNS_DB + "/" + docId, HttpStatus.SC_CREATED, mockPutResponse, runName, status)
        );

        CouchdbRasStore rasStore = fixtures.createCouchdbRasStore(interactions, mockLogFactory);

        // When...
        rasStore.updateTestStructure(runId, newTestStructure);

        // Then...
        // None of the interaction assertions should have failed.
    }

}