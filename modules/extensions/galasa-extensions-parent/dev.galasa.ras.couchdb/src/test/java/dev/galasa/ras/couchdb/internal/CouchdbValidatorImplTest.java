/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal;

import java.util.*;
import static org.assertj.core.api.Assertions.*;

import org.apache.http.*;
import org.apache.http.client.methods.HttpPost;
import org.junit.*;
import org.junit.rules.TestName;

import java.time.Instant;
import dev.galasa.framework.spi.utils.GalasaGson;
import dev.galasa.extensions.common.couchdb.pojos.Welcome;
import dev.galasa.extensions.common.impl.HttpRequestFactoryImpl;
import dev.galasa.extensions.common.couchdb.CouchdbException;
import dev.galasa.extensions.common.couchdb.CouchdbValidator;
import dev.galasa.extensions.common.api.HttpRequestFactory;
import dev.galasa.extensions.common.mocks.*;
import dev.galasa.ras.couchdb.internal.mocks.CouchdbTestFixtures;
import dev.galasa.ras.couchdb.internal.mocks.CouchdbTestFixtures.BaseHttpInteraction;

public class CouchdbValidatorImplTest {
    
    @Rule
    public TestName testName = new TestName();


  
    public static class WelcomeInteractionOK extends BaseHttpInteraction {

        public WelcomeInteractionOK() {
            super(CouchdbTestFixtures.rasUriStr, CouchdbTestFixtures.documentId1, "124");
        }

        @Override
        public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
            super.validateRequest(host,request);
            assertThat(request.getRequestLine().getMethod()).isEqualTo("GET");
        }

        public void validateRequest(HttpHost host, HttpRequest request, String method) throws RuntimeException {
            super.validateRequest(host,request);
            assertThat(request.getRequestLine().getMethod()).isEqualTo(method);
        }


        @Override
        public void validateRequestContentType(HttpRequest request) {
            // We don't expect a Content-type header as there is no payload sent to the server
        }
       

        @Override
        public MockCloseableHttpResponse getResponse() {

            // We expect a request to the couchdb system.
            // We will reply with a PutPostResponse

            Welcome welcomeBean = new Welcome();
            welcomeBean.couchdb = "Welcome";
            welcomeBean.version = "3.3.3";

            GalasaGson gson = new GalasaGson();
            String updateMessagePayload = gson.toJson(welcomeBean);

            HttpEntity entity = new MockHttpEntity(updateMessagePayload); 

            MockCloseableHttpResponse response = new MockCloseableHttpResponse();

            MockStatusLine statusLine = new MockStatusLine();
            statusLine.setStatusCode(HttpStatus.SC_OK);
            response.setStatusLine(statusLine);
            response.setEntity(entity);

            return response;
        }
    }

    public static class WelcomeInteractionBad extends WelcomeInteractionOK{

        public WelcomeInteractionBad (){
            super();
        }

        @Override
        public MockCloseableHttpResponse getResponse() {

            // We expect a request to the couchdb system.
            // We will reply with a PutPostResponse

            Welcome welcomeBean = new Welcome();
            welcomeBean.couchdb = "dummy-edition";
            welcomeBean.version = "3.3.3";

            GalasaGson gson = new GalasaGson();
            String updateMessagePayload = gson.toJson(welcomeBean);

            HttpEntity entity = new MockHttpEntity(updateMessagePayload); 

            MockCloseableHttpResponse response = new MockCloseableHttpResponse();

            MockStatusLine statusLine = new MockStatusLine();
            statusLine.setStatusCode(HttpStatus.SC_CREATED);
            response.setStatusLine(statusLine);
            response.setEntity(entity);

            return response;
        }
    }

    public static class CheckDatabasePresentInteraction extends WelcomeInteractionOK {

        public CheckDatabasePresentInteraction() {
            super();
        }

        @Override
        public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
            super.validateRequest(host,request,"HEAD");
        }

        @Override
        public MockCloseableHttpResponse getResponse() {
            MockCloseableHttpResponse response = new MockCloseableHttpResponse();
            MockStatusLine statusLine = new MockStatusLine();
            statusLine.setStatusCode(HttpStatus.SC_OK);
            response.setStatusLine(statusLine);

            return response;
        }
    }

    public static class CheckDatabasePresentNotFoundInteraction extends WelcomeInteractionOK {

        public CheckDatabasePresentNotFoundInteraction() {
            super();
        }

        @Override
        public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
            super.validateRequest(host,request,"HEAD");
        }

        @Override
            public MockCloseableHttpResponse getResponse() {
            MockCloseableHttpResponse response = new MockCloseableHttpResponse();
            MockStatusLine statusLine = new MockStatusLine();
            statusLine.setStatusCode(HttpStatus.SC_NOT_FOUND);
            response.setStatusLine(statusLine);
            return response;
        }
    }

    public static class CheckDatabasePresentConflictInteraction extends WelcomeInteractionOK {

        public CheckDatabasePresentConflictInteraction() {
            super();
        }

        @Override
        public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
            super.validateRequest(host,request,"PUT");
        }

        @Override
            public MockCloseableHttpResponse getResponse() {
            MockCloseableHttpResponse response = new MockCloseableHttpResponse();
            MockStatusLine statusLine = new MockStatusLine();
            statusLine.setStatusCode(HttpStatus.SC_CONFLICT);
            response.setStatusLine(statusLine);
            return response;
        }
    }

    public static class CheckDatabaseHasDocumentInteraction extends WelcomeInteractionOK {

        public CheckDatabaseHasDocumentInteraction() {
            super();
        }
        
        @Override
        public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
            super.validateRequest(host,request,"GET");
        }

        @Override
        public MockCloseableHttpResponse getResponse() {

            // We expect a request to the couchdb system.
            // We will reply with a PutPostResponse

            Welcome welcomeBean = new Welcome();
            welcomeBean.couchdb = "dummy-edition";
            welcomeBean.version = "3.3.3";

            GalasaGson gson = new GalasaGson();
            String updateMessagePayload = gson.toJson(welcomeBean);

            HttpEntity entity = new MockHttpEntity(updateMessagePayload); 

            MockCloseableHttpResponse response = new MockCloseableHttpResponse();

            MockStatusLine statusLine = new MockStatusLine();
            statusLine.setStatusCode(HttpStatus.SC_OK);
            response.setStatusLine(statusLine);
            response.setEntity(entity);

            return response;
        }

    }
    
    public static class SubmitDesignDocumentInteraction extends WelcomeInteractionOK {

        public SubmitDesignDocumentInteraction() {
            super();
        }
        
        @Override
        public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
            super.validateRequest(host,request,"PUT");
        }

        @Override
        public MockCloseableHttpResponse getResponse() {
            MockCloseableHttpResponse response = new MockCloseableHttpResponse();
            MockStatusLine statusLine = new MockStatusLine();
            statusLine.setStatusCode(HttpStatus.SC_CREATED);
            response.setStatusLine(statusLine);
            return response;
        }
    }

    public static class CheckIndexPOSTInteraction extends WelcomeInteractionOK {

        private String[] expectedIndexFields;

        public CheckIndexPOSTInteraction(String... expectedIndexFields) {
            super();
            this.expectedIndexFields = expectedIndexFields;
        }
        
        @Override
        public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
            super.validateRequest(host,request,"POST");
            validatePostRequestBody((HttpPost) request, expectedIndexFields);
        }

        @Override
        public MockCloseableHttpResponse getResponse() {
            MockCloseableHttpResponse response = new MockCloseableHttpResponse();
            MockStatusLine statusLine = new MockStatusLine();
            statusLine.setStatusCode(HttpStatus.SC_OK);
            response.setStatusLine(statusLine);
            return response;
        }

    }

    @Test
    public void testRasStoreCreateBlowsUpIfCouchDBDoesntReturnWelcomeString() throws Exception {

        List <HttpInteraction> interactions = new ArrayList<HttpInteraction>();

        interactions.add( new WelcomeInteractionBad() );

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        CouchdbValidator validatorUnderTest = new CouchdbValidatorImpl();
        HttpRequestFactory requestFactory = new HttpRequestFactoryImpl("Basic", "checkisvalid");
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        // When..
        Throwable thrown = catchThrowable(()-> validatorUnderTest.checkCouchdbDatabaseIsValid( CouchdbTestFixtures.rasUri , mockHttpClient, requestFactory, mockTimeService));

        // Then..
        assertThat(thrown).isNotNull();
        assertThat(thrown).as("exception caught is of type "+thrown.getClass().toString()).isInstanceOf(CouchdbException.class);
    }

    @Test
    public void testRasStoreCreatesDBIfCouchDBReturnsWelcomeString() throws Exception {
        List <HttpInteraction> interactions = new ArrayList<HttpInteraction>();

        //Check Welcome Screen
        interactions.add( new WelcomeInteractionOK(){
            
        } );

        //Add Interactions for checking the databases are present
        interactions.add( new CheckDatabasePresentInteraction());
        interactions.add( new CheckDatabasePresentInteraction());
        interactions.add( new CheckDatabasePresentInteraction());

        // Check Desing Docs Interactions
        interactions.add( new CheckDatabaseHasDocumentInteraction());
        interactions.add( new SubmitDesignDocumentInteraction());

        //Check Indexes Interactions
        interactions.add( new CheckDatabaseHasDocumentInteraction());
        interactions.add( new CheckIndexPOSTInteraction("runName"));
        interactions.add( new CheckDatabaseHasDocumentInteraction());
        interactions.add( new CheckIndexPOSTInteraction("requestor"));
        interactions.add( new CheckDatabaseHasDocumentInteraction());
        interactions.add( new CheckIndexPOSTInteraction("queued"));
        interactions.add( new CheckDatabaseHasDocumentInteraction());
        interactions.add( new CheckIndexPOSTInteraction("startTime"));
        interactions.add( new CheckDatabaseHasDocumentInteraction());
        interactions.add( new CheckIndexPOSTInteraction("endTime"));
        interactions.add( new CheckDatabaseHasDocumentInteraction());
        interactions.add( new CheckIndexPOSTInteraction("testName"));
        interactions.add( new CheckDatabaseHasDocumentInteraction());
        interactions.add( new CheckIndexPOSTInteraction("bundle"));
        interactions.add( new CheckDatabaseHasDocumentInteraction());
        interactions.add( new CheckIndexPOSTInteraction("result"));
        interactions.add( new CheckDatabaseHasDocumentInteraction());
        interactions.add( new CheckIndexPOSTInteraction("group"));
        interactions.add( new CheckDatabaseHasDocumentInteraction());
        interactions.add( new CheckIndexPOSTInteraction("submissionId"));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        CouchdbValidator validatorUnderTest = new CouchdbValidatorImpl();
        HttpRequestFactory requestFactory = new HttpRequestFactoryImpl("Basic", "checkisvalid");
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        // When..
        Throwable thrown = catchThrowable(()->validatorUnderTest.checkCouchdbDatabaseIsValid( CouchdbTestFixtures.rasUri , mockHttpClient, requestFactory, mockTimeService));

        assertThat(thrown).isNull();
    }

    @Test
    public void testRasStoreCreatesDBIfDBNotPresentThrowsException() throws Exception {
        List <HttpInteraction> interactions = new ArrayList<HttpInteraction>();

        //Check Welcome Screen
        interactions.add( new WelcomeInteractionOK(){
            @Override
            public MockCloseableHttpResponse getResponse() {
    
                // We expect a request to the couchdb system.
                // We will reply with a PutPostResponse
    
                Welcome welcomeBean = new Welcome();
                welcomeBean.couchdb = "Welcome";
                welcomeBean.version = "3.3.3";
    
                GalasaGson gson = new GalasaGson();
                String updateMessagePayload = gson.toJson(welcomeBean);
    
                HttpEntity entity = new MockHttpEntity(updateMessagePayload); 
    
                MockCloseableHttpResponse response = new MockCloseableHttpResponse();
    
                MockStatusLine statusLine = new MockStatusLine();
                statusLine.setStatusCode(HttpStatus.SC_OK);
                response.setStatusLine(statusLine);
                response.setEntity(entity);

                return response;
            }
        } );

        //Add bad Interaction for checking the databases are present
        interactions.add( new CheckDatabasePresentInteraction()
            {
                @Override
                public MockCloseableHttpResponse getResponse() {

                MockCloseableHttpResponse response = new MockCloseableHttpResponse();
    
                MockStatusLine statusLine = new MockStatusLine();
                statusLine.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                response.setStatusLine(statusLine);

                return response;
            }
            });

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        CouchdbValidator validatorUnderTest = new CouchdbValidatorImpl();
        HttpRequestFactory requestFactory = new HttpRequestFactoryImpl("Basic", "checkisvalid");
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        // When..
        Throwable thrown = catchThrowable(()->validatorUnderTest.checkCouchdbDatabaseIsValid( CouchdbTestFixtures.rasUri , mockHttpClient, requestFactory, mockTimeService));

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Validation failed of database galasa_run");
    }

    @Test
    public void testRasStoreCreatesDBIfDBNotPresentConflictResultsInExceptionAfterRetries() throws Exception {
        List <HttpInteraction> interactions = new ArrayList<HttpInteraction>();

        //Check Welcome Screen
        interactions.add( new WelcomeInteractionOK(){
            @Override
            public MockCloseableHttpResponse getResponse() {
    
                // We expect a request to the couchdb system.
                // We will reply with a PutPostResponse
    
                Welcome welcomeBean = new Welcome();
                welcomeBean.couchdb = "Welcome";
                welcomeBean.version = "3.3.3";
    
                GalasaGson gson = new GalasaGson();
                String updateMessagePayload = gson.toJson(welcomeBean);
    
                HttpEntity entity = new MockHttpEntity(updateMessagePayload); 
    
                MockCloseableHttpResponse response = new MockCloseableHttpResponse();
    
                MockStatusLine statusLine = new MockStatusLine();
                statusLine.setStatusCode(HttpStatus.SC_OK);
                response.setStatusLine(statusLine);
                response.setEntity(entity);

                return response;
            }
        } );

        // Add interaction for database check which results in not found (i.e. need to create database)
        
        //Add Interactions for checking the databases are present which result inc onflict to max out retry attempts
        interactions.add( new CheckDatabasePresentNotFoundInteraction());
        interactions.add( new CheckDatabasePresentConflictInteraction());
        interactions.add( new CheckDatabasePresentNotFoundInteraction());
        interactions.add( new CheckDatabasePresentConflictInteraction());
        interactions.add( new CheckDatabasePresentNotFoundInteraction());
        interactions.add( new CheckDatabasePresentConflictInteraction());
        interactions.add( new CheckDatabasePresentNotFoundInteraction());
        interactions.add( new CheckDatabasePresentConflictInteraction());
        interactions.add( new CheckDatabasePresentNotFoundInteraction());
        interactions.add( new CheckDatabasePresentConflictInteraction());
        interactions.add( new CheckDatabasePresentNotFoundInteraction());
        interactions.add( new CheckDatabasePresentConflictInteraction());
        interactions.add( new CheckDatabasePresentNotFoundInteraction());
        interactions.add( new CheckDatabasePresentConflictInteraction());
        interactions.add( new CheckDatabasePresentNotFoundInteraction());
        interactions.add( new CheckDatabasePresentConflictInteraction());
        interactions.add( new CheckDatabasePresentNotFoundInteraction());
        interactions.add( new CheckDatabasePresentConflictInteraction());
        interactions.add( new CheckDatabasePresentNotFoundInteraction());
        interactions.add( new CheckDatabasePresentConflictInteraction());

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        CouchdbValidator validatorUnderTest = new CouchdbValidatorImpl();
        HttpRequestFactory requestFactory = new HttpRequestFactoryImpl("Basic", "checkisvalid");
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        // When..
        Throwable thrown = catchThrowable(()->validatorUnderTest.checkCouchdbDatabaseIsValid( CouchdbTestFixtures.rasUri , mockHttpClient, requestFactory,mockTimeService));

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Create Database galasa_run failed on CouchDB server due to conflicts, attempted 10 times");
    }
}