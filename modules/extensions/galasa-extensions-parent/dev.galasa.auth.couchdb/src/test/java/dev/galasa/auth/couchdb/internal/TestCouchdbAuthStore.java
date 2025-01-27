/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.auth.couchdb.internal;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.junit.Test;

import dev.galasa.auth.couchdb.internal.beans.FrontEndClient;
import dev.galasa.auth.couchdb.internal.beans.UserDoc;
import dev.galasa.extensions.common.couchdb.pojos.IdRev;
import dev.galasa.extensions.common.couchdb.pojos.PutPostResponse;
import dev.galasa.extensions.common.couchdb.pojos.ViewResponse;
import dev.galasa.extensions.common.couchdb.pojos.ViewRow;
import dev.galasa.extensions.common.impl.HttpRequestFactoryImpl;
import dev.galasa.extensions.common.mocks.BaseHttpInteraction;
import dev.galasa.extensions.common.mocks.HttpInteraction;
import dev.galasa.extensions.common.mocks.MockCloseableHttpClient;
import dev.galasa.extensions.common.mocks.MockHttpClientFactory;
import dev.galasa.extensions.common.mocks.MockLogFactory;
import dev.galasa.extensions.common.mocks.MockTimeService;
import dev.galasa.extensions.common.mocks.couchdb.MockCouchdbValidator;
import dev.galasa.framework.spi.auth.AuthStoreException;
import dev.galasa.framework.spi.auth.IFrontEndClient;
import dev.galasa.framework.spi.auth.IInternalAuthToken;
import dev.galasa.framework.spi.auth.IUser;

public class TestCouchdbAuthStore {

    class GetAllDocumentsInteraction extends BaseHttpInteraction {

        public GetAllDocumentsInteraction(String expectedUri, int responseStatusCode, ViewResponse tokenDocsToReturn) {
            super(expectedUri, responseStatusCode);
            setResponsePayload(tokenDocsToReturn);
        }

        @Override
        public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
            super.validateRequest(host, request);
            assertThat(request.getRequestLine().getMethod()).isEqualTo("GET");
        }
    }

    class GetDocumentInteraction<T> extends BaseHttpInteraction {

        public GetDocumentInteraction(String expectedUri, int responseStatusCode, T responseObjToReturn) {
            super(expectedUri, responseStatusCode);
            setResponsePayload(responseObjToReturn);
        }

        @Override
        public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
            super.validateRequest(host, request);
            assertThat(request.getRequestLine().getMethod()).isEqualTo("GET");
        }
    }

    class DeleteDocumentInteraction extends BaseHttpInteraction {

        public DeleteDocumentInteraction(String expectedUri, int responseStatusCode) {
            super(expectedUri, responseStatusCode);
            setResponsePayload("");
        }

        @Override
        public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
            super.validateRequest(host, request);
            assertThat(request.getRequestLine().getMethod()).isEqualTo("DELETE");
        }
    }

    class CreateDocumentInteraction extends BaseHttpInteraction {

        public CreateDocumentInteraction(String expectedUri, int responseStatusCode) {
            super(expectedUri, responseStatusCode);

            PutPostResponse responseTransportBean = new PutPostResponse();
            responseTransportBean.id = "id";
            responseTransportBean.ok = true;
            responseTransportBean.rev = "rev";
            setResponsePayload(responseTransportBean);
        }

        @Override
        public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
            super.validateRequest(host, request);
            assertThat(request.getRequestLine().getMethod()).isEqualTo("POST");
        }
    }

    class UpdateDocumentInteraction extends BaseHttpInteraction {

        public UpdateDocumentInteraction(String expectedUri, int responseStatusCode, String id, String rev) {
            super(expectedUri, responseStatusCode);

            PutPostResponse responseTransportBean = new PutPostResponse();
            responseTransportBean.id = id;
            responseTransportBean.ok = true;
            responseTransportBean.rev = rev;
            setResponsePayload(responseTransportBean);
        }

        @Override
        public void validateRequest(HttpHost host, HttpRequest request) throws RuntimeException {
            super.validateRequest(host, request);
            assertThat(request.getRequestLine().getMethod()).isEqualTo("PUT");
        }
    }

    @Test
    public void testGetTokensReturnsTokensWithFailingRequestReturnsErrorDup() throws Exception {
        // Given...
        URI authStoreUri = URI.create("couchdb:https://my-auth-store");
        MockLogFactory logFactory = new MockLogFactory();

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(new GetAllDocumentsInteraction("https://my-auth-store/galasa_tokens/_all_docs",
                HttpStatus.SC_INTERNAL_SERVER_ERROR, null));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri, httpClientFactory, new HttpRequestFactoryImpl(),
                logFactory, new MockCouchdbValidator(), mockTimeService);

        // When...
        AuthStoreException thrown = catchThrowableOfType(() -> authStore.getTokens(), AuthStoreException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL6101E", "Failed to get auth tokens from the CouchDB auth store");
    }

    @Test
    public void testGetTokensReturnsTokensFromCouchdbOK() throws Exception {
        // Given...
        URI authStoreUri = URI.create("couchdb:https://my-auth-store");
        MockLogFactory logFactory = new MockLogFactory();

        ViewRow tokenDoc = new ViewRow();
        tokenDoc.key = "token1";
        List<ViewRow> mockDocs = List.of(tokenDoc);

        ViewResponse mockAllDocsResponse = new ViewResponse();
        mockAllDocsResponse.rows = mockDocs;

        CouchdbAuthToken mockToken = new CouchdbAuthToken("token1", "dex-client", "my test token", Instant.now(),
                new CouchdbUser("johndoe", "dex-user-id"));
        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(new GetAllDocumentsInteraction("https://my-auth-store/galasa_tokens/_all_docs",
                HttpStatus.SC_OK, mockAllDocsResponse));
        interactions.add(new GetDocumentInteraction<CouchdbAuthToken>("https://my-auth-store/galasa_tokens/token1",
                HttpStatus.SC_OK, mockToken));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri, httpClientFactory, new HttpRequestFactoryImpl(),
                logFactory, new MockCouchdbValidator(), mockTimeService);

        // When...
        List<IInternalAuthToken> tokens = authStore.getTokens();

        // Then...
        assertThat(tokens).hasSize(1);

        IInternalAuthToken actualToken = tokens.get(0);
        assertThat(actualToken).usingRecursiveComparison().isEqualTo(mockToken);
    }

    @Test
    public void testGetTokensReturnsTokensByLoginIdFromCouchdbOK() throws Exception {
        // Given...
        URI authStoreUri = URI.create("couchdb:https://my-auth-store");
        MockLogFactory logFactory = new MockLogFactory();

        ViewRow tokenDoc = new ViewRow();
        tokenDoc.id = "token1";
        List<ViewRow> mockDocs = List.of(tokenDoc);

        ViewResponse mockAllDocsResponse = new ViewResponse();
        mockAllDocsResponse.rows = mockDocs;

        CouchdbAuthToken mockToken = new CouchdbAuthToken("token1", "dex-client", "my test token", Instant.now(),
                new CouchdbUser("johndoe", "dex-user-id"));
        CouchdbAuthToken mockToken2 = new CouchdbAuthToken("token2", "dex-client", "my test token", Instant.now(),
                new CouchdbUser("notJohnDoe", "dex-user-id"));
        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(new GetAllDocumentsInteraction(
                "https://my-auth-store/galasa_tokens/_design/docs/_view/loginId-view?key=%22johndoe%22",
                HttpStatus.SC_OK, mockAllDocsResponse));
        interactions.add(new GetDocumentInteraction<CouchdbAuthToken>("https://my-auth-store/galasa_tokens/token1",
                HttpStatus.SC_OK, mockToken));
        interactions.add(new GetDocumentInteraction<CouchdbAuthToken>("https://my-auth-store/galasa_tokens/token1",
                HttpStatus.SC_OK, mockToken2));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri, httpClientFactory, new HttpRequestFactoryImpl(),
                logFactory, new MockCouchdbValidator(), mockTimeService);
        // When...
        List<IInternalAuthToken> tokens = authStore.getTokensByLoginId("johndoe");

        // Then...
        assertThat(tokens).hasSize(1);

        IInternalAuthToken actualToken = tokens.get(0);
        assertThat(actualToken).usingRecursiveComparison().isEqualTo(mockToken);
    }

    @Test
    public void testGetTokensReturnsTokensByLoginIdWithFailingRequestReturnsError() throws Exception {
        // Given...
        URI authStoreUri = URI.create("couchdb:https://my-auth-store");
        MockLogFactory logFactory = new MockLogFactory();

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(new GetAllDocumentsInteraction(
                "https://my-auth-store/galasa_tokens/_design/docs/_view/loginId-view?key=%22johndoe%22",
                HttpStatus.SC_INTERNAL_SERVER_ERROR, null));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri, httpClientFactory, new HttpRequestFactoryImpl(),
                logFactory, new MockCouchdbValidator(), mockTimeService);

        // When...
        AuthStoreException thrown = catchThrowableOfType(() -> authStore.getTokensByLoginId("johndoe"),
                AuthStoreException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL6101E", "Failed to get auth tokens from the CouchDB auth store");
    }

    @Test
    public void testStoreTokenSendsRequestToCreateTokenDocumentOK() throws Exception {
        // Given...
        URI authStoreUri = URI.create("couchdb:https://my-auth-store");
        MockLogFactory logFactory = new MockLogFactory();

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(new CreateDocumentInteraction("https://my-auth-store/galasa_tokens", HttpStatus.SC_CREATED));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri, httpClientFactory, new HttpRequestFactoryImpl(),
                logFactory, new MockCouchdbValidator(), mockTimeService);

        // When...
        authStore.storeToken("this-is-a-dex-id", "my token", new CouchdbUser("user1", "user1-id"));

        // Then the assertions made in the create token document interaction shouldn't
        // have failed.
    }

    @Test
    public void testStoreTokenWithFailingRequestToCreateTokenDocumentReturnsError() throws Exception {
        // Given...
        URI authStoreUri = URI.create("couchdb:https://my-auth-store");
        MockLogFactory logFactory = new MockLogFactory();

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(new CreateDocumentInteraction("https://my-auth-store/galasa_tokens",
                HttpStatus.SC_INTERNAL_SERVER_ERROR));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri, httpClientFactory, new HttpRequestFactoryImpl(),
                logFactory, new MockCouchdbValidator(), mockTimeService);

        // When...
        AuthStoreException thrown = catchThrowableOfType(
                () -> authStore.storeToken("this-is-a-dex-id", "my token", new CouchdbUser("user1", "user1-id")),
                AuthStoreException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL6102E",
                "Failed to store auth token in the CouchDB tokens database");
    }

    @Test
    public void testDeleteTokenSendsRequestToDeleteTokenDocumentOK() throws Exception {
        // Given...
        URI authStoreUri = URI.create("couchdb:https://my-auth-store");
        MockLogFactory logFactory = new MockLogFactory();

        String tokenIdToDelete = "my-old-token";

        IdRev mockIdRev = new IdRev();
        mockIdRev._rev = "this-is-a-revision";

        String expectedGetRequestUrl = "https://my-auth-store/galasa_tokens/" + tokenIdToDelete;
        String expectedDeleteRequestUrl = "https://my-auth-store/galasa_tokens/" + tokenIdToDelete + "?rev="
                + mockIdRev._rev;

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(new GetDocumentInteraction<IdRev>(expectedGetRequestUrl, HttpStatus.SC_OK, mockIdRev));
        interactions.add(new DeleteDocumentInteraction(expectedDeleteRequestUrl, HttpStatus.SC_OK));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri, httpClientFactory, new HttpRequestFactoryImpl(),
                logFactory, new MockCouchdbValidator(), mockTimeService);

        // When...
        authStore.deleteToken(tokenIdToDelete);

        // Then the assertions made in the document interactions shouldn't have failed.
    }

    @Test
    public void testDeleteTokenWithAcceptedRequestToDeleteTokenDocumentDoesNotError() throws Exception {
        // Given...
        URI authStoreUri = URI.create("couchdb:https://my-auth-store");
        MockLogFactory logFactory = new MockLogFactory();

        String tokenIdToDelete = "my-old-token";

        IdRev mockIdRev = new IdRev();
        mockIdRev._rev = "this-is-a-revision";

        String expectedGetRequestUrl = "https://my-auth-store/galasa_tokens/" + tokenIdToDelete;
        String expectedDeleteRequestUrl = "https://my-auth-store/galasa_tokens/" + tokenIdToDelete + "?rev="
                + mockIdRev._rev;

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(new GetDocumentInteraction<IdRev>(expectedGetRequestUrl, HttpStatus.SC_OK, mockIdRev));

        // The DELETE request may return a 202 Accepted, which shouldn't be a problem
        // for us
        interactions.add(new DeleteDocumentInteraction(expectedDeleteRequestUrl, HttpStatus.SC_ACCEPTED));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri, httpClientFactory, new HttpRequestFactoryImpl(),
                logFactory, new MockCouchdbValidator(), mockTimeService);

        // When...
        authStore.deleteToken(tokenIdToDelete);

        // Then the assertions made in the document interactions shouldn't have failed.
    }

    @Test
    public void testDeleteTokenWithFailingRequestToDeleteTokenDocumentThrowsError() throws Exception {
        // Given...
        URI authStoreUri = URI.create("couchdb:https://my-auth-store");
        MockLogFactory logFactory = new MockLogFactory();

        String tokenIdToDelete = "my-old-token";

        IdRev mockIdRev = new IdRev();
        mockIdRev._rev = "this-is-a-revision";

        String expectedGetRequestUrl = "https://my-auth-store/galasa_tokens/" + tokenIdToDelete;
        String expectedDeleteRequestUrl = "https://my-auth-store/galasa_tokens/" + tokenIdToDelete + "?rev="
                + mockIdRev._rev;

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(new GetDocumentInteraction<IdRev>(expectedGetRequestUrl, HttpStatus.SC_OK, mockIdRev));

        // Simulate an internal server error
        interactions
                .add(new DeleteDocumentInteraction(expectedDeleteRequestUrl, HttpStatus.SC_INTERNAL_SERVER_ERROR));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri, httpClientFactory, new HttpRequestFactoryImpl(),
                logFactory, new MockCouchdbValidator(), mockTimeService);

        // When...
        AuthStoreException thrown = catchThrowableOfType(() -> {
            authStore.deleteToken(tokenIdToDelete);
        }, AuthStoreException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL6104E",
                "Failed to delete auth token from the CouchDB tokens database");
        assertThat(thrown.getMessage()).contains("GAL6007E", "Expected status code(s) [200, 202, 404] but received 500");
    }

    @Test
    public void testDeleteTokenWithFailingRequestToGetTokenDocumentThrowsError() throws Exception {
        // Given...
        URI authStoreUri = URI.create("couchdb:https://my-auth-store");
        MockLogFactory logFactory = new MockLogFactory();

        String tokenIdToDelete = "my-old-token";

        IdRev mockIdRev = new IdRev();
        mockIdRev._rev = "this-is-a-revision";

        String expectedGetRequestUrl = "https://my-auth-store/galasa_tokens/" + tokenIdToDelete;

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();

        // Simulate an internal server error
        interactions.add(new GetDocumentInteraction<IdRev>(expectedGetRequestUrl, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                mockIdRev));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri, httpClientFactory, new HttpRequestFactoryImpl(),
                logFactory, new MockCouchdbValidator(), mockTimeService);

        // When...
        AuthStoreException thrown = catchThrowableOfType(() -> {
            authStore.deleteToken(tokenIdToDelete);
        }, AuthStoreException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL6104E",
                "Failed to delete auth token from the CouchDB tokens database");
    }

    @Test
    public void testDeleteTokenWithBadGetTokenDocumentResponseBodyThrowsError() throws Exception {
        // Given...
        URI authStoreUri = URI.create("couchdb:https://my-auth-store");
        MockLogFactory logFactory = new MockLogFactory();

        String tokenIdToDelete = "my-old-token";

        String expectedGetRequestUrl = "https://my-auth-store/galasa_tokens/" + tokenIdToDelete;

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();

        // Simulate an internal server error
        interactions.add(new GetDocumentInteraction<IdRev>(expectedGetRequestUrl, HttpStatus.SC_OK, null));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri, httpClientFactory, new HttpRequestFactoryImpl(),
                logFactory, new MockCouchdbValidator(), mockTimeService);

        // When...
        AuthStoreException thrown = catchThrowableOfType(() -> {
            authStore.deleteToken(tokenIdToDelete);
        }, AuthStoreException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL6104E",
                "Failed to delete auth token from the CouchDB tokens database");
        assertThat(thrown.getMessage()).contains("GAL6011E",
                "Failed to get document with ID 'my-old-token' from the 'galasa_tokens' database");
    }

    @Test
    public void testGetAllUsersReturnsListOfUsersFromCouchdbOK() throws Exception {
        // Given...
        URI authStoreUri = URI.create("couchdb:https://my-users-store");
        MockLogFactory logFactory = new MockLogFactory();

        ViewRow userDoc = new ViewRow();
        ViewRow userDoc2 = new ViewRow();

        userDoc.id = "user1";
        userDoc2.id = "user2";

        List<ViewRow> mockDocs = List.of(userDoc, userDoc2);

        ViewResponse mockAllDocsResponse = new ViewResponse();
        mockAllDocsResponse.rows = mockDocs;

        UserDoc mockUser = new UserDoc("user1", List.of(), "2");
        UserDoc mockUser2 = new UserDoc("user2", List.of(), "2");
        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(new GetAllDocumentsInteraction("https://my-users-store/galasa_users/_all_docs",
                HttpStatus.SC_OK, mockAllDocsResponse));
        interactions.add(new GetDocumentInteraction<UserDoc>("https://my-users-store/galasa_users/user1",
                HttpStatus.SC_OK, mockUser));
        interactions.add(new GetDocumentInteraction<UserDoc>("https://my-users-store/galasa_users/user2",
                HttpStatus.SC_OK, mockUser2));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri,
                httpClientFactory, new HttpRequestFactoryImpl(), logFactory, new MockCouchdbValidator(),
                mockTimeService);
        // When...
        List<IUser> users = authStore.getAllUsers();

        // Then...
        assertThat(users).hasSize(2);
    }

    @Test
    public void testGetTokensReturnsTokensWithFailingRequestReturnsError() throws Exception {
        // Given...
        URI authStoreUri = URI.create("couchdb:https://my-users-store");
        MockLogFactory logFactory = new MockLogFactory();

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(new GetAllDocumentsInteraction("https://my-users-store/galasa_users/_all_docs",
                HttpStatus.SC_INTERNAL_SERVER_ERROR, null));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri, httpClientFactory, new HttpRequestFactoryImpl(),
                logFactory, new MockCouchdbValidator(), mockTimeService);

        // When...
        AuthStoreException thrown = catchThrowableOfType(() -> authStore.getAllUsers(), AuthStoreException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL6202E",
                "Failed to get user documents from the CouchDB users database.");
    }

    @Test
    public void testStoreUserSendsRequestToCreateUserDocumentOK() throws Exception {
        // Given...
        URI authStoreUri = URI.create("couchdb:https://my-users-store");
        MockLogFactory logFactory = new MockLogFactory();

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(new CreateDocumentInteraction("https://my-users-store/galasa_users", HttpStatus.SC_CREATED));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri, httpClientFactory, new HttpRequestFactoryImpl(),
                logFactory, new MockCouchdbValidator(), mockTimeService);

        // When...
        authStore.createUser("this-is-a-login-id", "rest-api", "2");

        // Then the assertions made in the create users document interaction shouldn't
        // have failed.
    }

    @Test
    public void testGetUserReturnsUsersByLoginIdFromCouchdbOK() throws Exception {
        // Given...
        URI authStoreUri = URI.create("couchdb:https://my-auth-store");
        MockLogFactory logFactory = new MockLogFactory();

        ViewRow userDoc1 = new ViewRow();

        userDoc1.id = "user1";
        List<ViewRow> mockDocs = List.of(userDoc1);

        ViewResponse mockAllDocsResponse = new ViewResponse();
        mockAllDocsResponse.rows = mockDocs;

        FrontEndClient client = new FrontEndClient();

        client.setClientName("web-ui");
        client.setLastLogin(Instant.now());

        UserDoc mockUser = new UserDoc("johndoe", List.of(client), "2");

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(new GetAllDocumentsInteraction(
                "https://my-auth-store/galasa_users/_design/docs/_view/loginId-view?key=%22user1%22",
                HttpStatus.SC_OK, mockAllDocsResponse));
        interactions.add(new GetDocumentInteraction<UserDoc>("https://my-auth-store/galasa_users/user1",
                HttpStatus.SC_OK, mockUser));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri, httpClientFactory, new HttpRequestFactoryImpl(),
                logFactory, new MockCouchdbValidator(), mockTimeService);
        // When...
        IUser user = authStore.getUserByLoginId("user1");

        assertThat(user).isInstanceOf(UserImpl.class);
        assertThat(user).isNotNull();
        assertThat(user.getLoginId()).isEqualTo("johndoe");
    }

    @Test
    public void testGetUserReturnsCorrectUserByLoginIdFromCouchdbOK() throws Exception {
        // Given...
        URI authStoreUri = URI.create("couchdb:https://my-auth-store");
        MockLogFactory logFactory = new MockLogFactory();

        ViewRow userDoc1 = new ViewRow();

        userDoc1.id = "user1";
        List<ViewRow> mockDocs = List.of(userDoc1);

        ViewResponse mockAllDocsResponse = new ViewResponse();
        mockAllDocsResponse.rows = mockDocs;

        UserDoc mockUser = new UserDoc("johndoe", List.of(new FrontEndClient("web-ui", Instant.now())), "2");

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(new GetAllDocumentsInteraction(
                "https://my-auth-store/galasa_users/_design/docs/_view/loginId-view?key=%22notJohndoe%22",
                HttpStatus.SC_OK, mockAllDocsResponse));
        interactions.add(new GetDocumentInteraction<UserDoc>("https://my-auth-store/galasa_users/user1",
                HttpStatus.SC_OK, mockUser));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri,
                httpClientFactory, new HttpRequestFactoryImpl(), logFactory, new MockCouchdbValidator(),
                mockTimeService);
        // When...
        IUser user = authStore.getUserByLoginId("notJohndoe");

        assertThat(user).usingRecursiveComparison().isNotEqualTo(mockUser);
    }

    @Test
    public void testUpdateUserUpdatesExisitingClientOK() throws Exception {
        // Given...
        UserImpl mockUser = new UserImpl(new UserDoc("johndoe", List.of(new FrontEndClient("web-ui", Instant.MIN)),"2"));
        mockUser.setVersion("1");
        mockUser.setUserNumber("user1");

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(new UpdateDocumentInteraction("https://my-auth-store/galasa_users/user1",
                HttpStatus.SC_CREATED, "user1", "2" ));

        CouchdbAuthStore authStore = createAuthStoreToTest(interactions);

        IUser userGotBack = authStore.updateUser(mockUser);

        assertThat(userGotBack.getVersion()).isEqualTo("2");
    }

    @Test
    public void testUpdateUserCouchDBPassesBackANullVersionField() throws Exception {
        // Given...
        UserImpl mockUser = new UserImpl(new UserDoc("johndoe", List.of(new FrontEndClient("rest-api", Instant.MIN)),"2"));
        mockUser.setVersion("1");
        mockUser.setUserNumber("user1");

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(
            new UpdateDocumentInteraction("https://my-auth-store/galasa_users/user1", HttpStatus.SC_CREATED, "user1" , null )
        );

        CouchdbAuthStore authStore = createAuthStoreToTest(interactions);

        AuthStoreException ex = catchThrowableOfType( ()-> authStore.updateUser(mockUser), AuthStoreException.class);

        assertThat(ex.getMessage()).contains("GAL6204E");
    }

    @Test
    public void testUpdateUserCouchDBPassesBackADocIdWithWrongUserNumberField() throws Exception {
        // Given...
        UserImpl mockUser = new UserImpl(new UserDoc("johndoe", List.of(new FrontEndClient("rest-api", Instant.MIN)),"2"));
        mockUser.setVersion("1");
        mockUser.setUserNumber("user1");

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(
            new UpdateDocumentInteraction("https://my-auth-store/galasa_users/user1", HttpStatus.SC_CREATED, "user2" , "2" )
        );

        CouchdbAuthStore authStore = createAuthStoreToTest(interactions);

        AuthStoreException ex = catchThrowableOfType( ()-> authStore.updateUser(mockUser), AuthStoreException.class);

        assertThat(ex.getMessage()).contains("GAL6205E");
    }

    @Test
    public void testUpdateUserCouchDBPassesBackADocIdWithMissingIdField() throws Exception {
        // Given...

        UserImpl mockUser = new UserImpl(new UserDoc("johndoe", List.of(new FrontEndClient("rest-api", Instant.MIN)),"2"));
        mockUser.setVersion("1");
        mockUser.setUserNumber("user1");

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(
            new UpdateDocumentInteraction("https://my-auth-store/galasa_users/user1", HttpStatus.SC_CREATED,null , "2" )
        );

        CouchdbAuthStore authStore = createAuthStoreToTest(interactions);

        AuthStoreException ex = catchThrowableOfType( ()-> authStore.updateUser(mockUser), AuthStoreException.class);

        assertThat(ex.getMessage()).contains("GAL6204E");
    }

    @Test
    public void testUpdateUserCouchDBPassesBackAnUnexpectedServerError() throws Exception {
        // Given...
        UserImpl mockUser = new UserImpl(new UserDoc("johndoe", List.of(new FrontEndClient("rest-api", Instant.MIN)),"2"));
        mockUser.setVersion("1");
        mockUser.setUserNumber("user1");

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(
            new UpdateDocumentInteraction("https://my-auth-store/galasa_users/user1", HttpStatus.SC_INTERNAL_SERVER_ERROR,null , "2" )
        );

        CouchdbAuthStore authStore = createAuthStoreToTest(interactions);

        AuthStoreException ex = catchThrowableOfType( ()-> authStore.updateUser(mockUser), AuthStoreException.class);

        assertThat(ex.getMessage()).contains("GAL6203E");
    }

    @Test
    public void testUpdateUserWithBadUserNullIdFieldGetsDetectedAsError() throws Exception {
        UserImpl mockUser = new UserImpl(new UserDoc("johndoe", List.of(new FrontEndClient("rest-api", Instant.MIN)),"2"));
        mockUser.setVersion("1");
        mockUser.setUserNumber(null);

        CouchdbAuthStore authStore = createAuthStoreToTest();

        AuthStoreException ex = catchThrowableOfType( ()-> authStore.updateUser(mockUser), AuthStoreException.class);

        assertThat(ex.getMessage()).contains("GAL6206E");
    }

    @Test
    public void testUpdateUserWithBadUserNullVersionFieldGetsDetectedAsError() throws Exception {
        UserImpl mockUser = new UserImpl(new UserDoc("johndoe", List.of(new FrontEndClient("rest-api", Instant.MIN)),"2"));
        mockUser.setVersion(null);
        mockUser.setUserNumber("user1");

        CouchdbAuthStore authStore = createAuthStoreToTest();

        AuthStoreException ex = catchThrowableOfType( ()-> authStore.updateUser(mockUser), AuthStoreException.class);

        assertThat(ex.getMessage()).contains("GAL6207E");
    }

    private CouchdbAuthStore createAuthStoreToTest() throws Exception {
        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        return createAuthStoreToTest(interactions);
    }

    private CouchdbAuthStore createAuthStoreToTest(List<HttpInteraction> interactions) throws Exception {
        URI authStoreUri = URI.create("couchdb:https://my-auth-store");
        MockLogFactory logFactory = new MockLogFactory();

        MockCloseableHttpClient mockHttpClient = new
        MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new
        MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.MIN);

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri,
        httpClientFactory, new HttpRequestFactoryImpl(), logFactory, new
        MockCouchdbValidator(), mockTimeService);

        return authStore;
    }

    @Test
    public void testCanCreateAClientDocument() throws Exception {
        CouchdbAuthStore authStore = createAuthStoreToTest();
        IFrontEndClient client = authStore.createClient("myClientName");
        assertThat(client.getClientName()).isEqualTo("myClientName");
        assertThat(client.getLastLogin()).isEqualTo(Instant.MIN);
    }

    @Test
    public void testCanGetUserByUserNumberReturnsOK() throws Exception{

        // Given...
        URI authStoreUri = URI.create("couchdb:https://my-auth-store");
        MockLogFactory logFactory = new MockLogFactory();

        FrontEndClient client = new FrontEndClient();

        client.setClientName("web-ui");
        client.setLastLogin(Instant.now());

        UserDoc mockUser = new UserDoc("johndoe", List.of(client),"2");

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(new GetDocumentInteraction<UserDoc>("https://my-auth-store/galasa_users/user2",
                HttpStatus.SC_OK, mockUser));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri, httpClientFactory, new HttpRequestFactoryImpl(),
                logFactory, new MockCouchdbValidator(), mockTimeService);
        // When...
        IUser user = authStore.getUser("user2");

        assertThat(user).isInstanceOf(UserImpl.class);
        assertThat(user).isNotNull();
        assertThat(user.getLoginId()).isEqualTo("johndoe");

    }

    @Test
    public void testReturnsNullWhenUserNotFoundByUserNumberReturnsOK() throws Exception{

        // Given...
        URI authStoreUri = URI.create("couchdb:https://my-auth-store");
        MockLogFactory logFactory = new MockLogFactory();

        FrontEndClient client = new FrontEndClient();

        client.setClientName("web-ui");
        client.setLastLogin(Instant.now());

        UserDoc mockUser = new UserDoc("johndoe", List.of(client),"2");
        mockUser.setUserNumber("user2");
        mockUser.setVersion("v-1");

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(new GetDocumentInteraction<UserDoc>("https://my-auth-store/galasa_users/user1",
                HttpStatus.SC_OK, null));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri, httpClientFactory, new HttpRequestFactoryImpl(),
                logFactory, new MockCouchdbValidator(), mockTimeService);
        // When...
        IUser user = authStore.getUser("user1");

        assertThat(user).isNull();

    }

    @Test
    public void testGetUserThrowsInternalServerError() throws Exception{

        // Given...
        URI authStoreUri = URI.create("couchdb:https://my-auth-store");
        MockLogFactory logFactory = new MockLogFactory();

        FrontEndClient client = new FrontEndClient();

        client.setClientName("web-ui");
        client.setLastLogin(Instant.now());

        UserDoc mockUser = new UserDoc("johndoe", List.of(client),"2");
        mockUser.setUserNumber("user1");
        mockUser.setVersion("v-1");

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(new GetDocumentInteraction<UserDoc>("https://my-auth-store/galasa_users/user1",
                HttpStatus.SC_INTERNAL_SERVER_ERROR, null));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri, httpClientFactory, new HttpRequestFactoryImpl(),
                logFactory, new MockCouchdbValidator(), mockTimeService);

        // When...
        AuthStoreException thrown = catchThrowableOfType(
                () -> authStore.getUser("user1"),
                AuthStoreException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL6202E",
                "Failed to get user documents from the CouchDB users database");
    }

    @Test
    public void testDeleteUserWithAcceptedRequestToDeleteUserDocumentReturnsOK() throws Exception {
        // Given...
        URI authStoreUri = URI.create("couchdb:https://my-auth-store");
        MockLogFactory logFactory = new MockLogFactory();

        String userIdToDelete = "my-old-user";

        IdRev mockIdRev = new IdRev();
        mockIdRev._rev = "this-is-a-revision";
        mockIdRev._id = userIdToDelete;

        UserDoc userToBeDeleted = new UserDoc();
        userToBeDeleted.setVersion(mockIdRev._rev);
        userToBeDeleted.setUserNumber(mockIdRev._id);

        IUser user = new UserImpl(userToBeDeleted);

        String expectedGetRequestUrl = "https://my-auth-store/galasa_users/" + userIdToDelete;
        String expectedDeleteRequestUrl = "https://my-auth-store/galasa_users/" + userIdToDelete + "?rev="
                + mockIdRev._rev;

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(new GetDocumentInteraction<IdRev>(expectedGetRequestUrl, HttpStatus.SC_OK, mockIdRev));

        // The DELETE request may return a 202 Accepted, which shouldn't be a problem
        // for us
        interactions.add(new DeleteDocumentInteraction(expectedDeleteRequestUrl, HttpStatus.SC_ACCEPTED));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri, httpClientFactory, new HttpRequestFactoryImpl(),
                logFactory, new MockCouchdbValidator(), mockTimeService);

        // When...
        authStore.deleteUser(user);

        // Then the assertions made in the document interactions shouldn't have failed.
    }

    @Test
    public void testDeleteUserWithAcceptedRequestToDeleteUserDocumentThrowsError() throws Exception {
        // Given...
        URI authStoreUri = URI.create("couchdb:https://my-auth-store");
        MockLogFactory logFactory = new MockLogFactory();

        String userIdToDelete = "my-old-user";

        IdRev mockIdRev = new IdRev();
        mockIdRev._rev = "this-is-a-revision";
        mockIdRev._id = userIdToDelete;

        UserDoc userToBeDeleted = new UserDoc();
        userToBeDeleted.setVersion(mockIdRev._rev);
        userToBeDeleted.setUserNumber(mockIdRev._id);

        IUser user = new UserImpl(userToBeDeleted);

        String expectedGetRequestUrl = "https://my-auth-store/galasa_users/" + userIdToDelete;
        String expectedDeleteRequestUrl = "https://my-auth-store/galasa_users/" + userIdToDelete + "?rev="
                + mockIdRev._rev;

        List<HttpInteraction> interactions = new ArrayList<HttpInteraction>();
        interactions.add(new GetDocumentInteraction<IdRev>(expectedGetRequestUrl, HttpStatus.SC_OK, mockIdRev));

        interactions.add(new DeleteDocumentInteraction(expectedDeleteRequestUrl, HttpStatus.SC_INTERNAL_SERVER_ERROR));

        MockCloseableHttpClient mockHttpClient = new MockCloseableHttpClient(interactions);

        MockHttpClientFactory httpClientFactory = new MockHttpClientFactory(mockHttpClient);
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        CouchdbAuthStore authStore = new CouchdbAuthStore(authStoreUri, httpClientFactory, new HttpRequestFactoryImpl(),
                logFactory, new MockCouchdbValidator(), mockTimeService);

        // When...
        AuthStoreException thrown = catchThrowableOfType(
                () -> authStore.deleteUser(user),
                AuthStoreException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL6208E",
                "Failed to delete user from the CouchDB users database.");
    }


}
