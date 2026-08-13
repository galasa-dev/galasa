/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common.impl.auth;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.junit.Test;

import com.google.gson.Gson;

import dev.galasa.plugin.common.AuthenticationException;
import dev.galasa.plugin.common.impl.GsonFactory;
import dev.galasa.plugin.common.impl.MockHttpClient;
import dev.galasa.plugin.common.impl.auth.beans.*;


public class AuthenticationServiceImplTest { 

    @Test
    public void testAuthServiceComplainsIfNullGalasaAuthTokenSupplied() throws Exception {
        Exception ex = catchException( ()-> new AuthenticationServiceImpl(new URL("http://not-null.com"), null, new MockHttpClient()));
        assertThat(ex).isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void testAuthServiceComplainsIfNullApiServerUrlSupplied() throws Exception {
        Exception ex = catchException( ()-> new AuthenticationServiceImpl(null, "could-be-a:valid-token", new MockHttpClient()));
        assertThat(ex).isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void testAuthServiceGivenTokenWithNoSeparatorFails() throws Exception {
        Exception ex = catchException( ()-> new AuthenticationServiceImpl(new URL("http://not-null.com"), "no-separator-in-this-token", new MockHttpClient()));
        assertThat(ex).isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void testAuthServiceGivenTokenWithTwoSeparatorsFails() throws Exception {
        Exception ex = catchException( ()-> new AuthenticationServiceImpl(new URL("http://not-null.com"), "too-many:separators:here", new MockHttpClient()));
        assertThat(ex).isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void testAuthServiceGivenNullHttpClientFails() throws Exception {
        Exception ex = catchException( ()-> new AuthenticationServiceImpl(new URL("http://not-null.com"), "could-be-a:valid-token", null));
        assertThat(ex).isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void testAuthServiceGetsHttpNotFoundResponseThrowsDecentError() throws Exception {

        MockHttpClient mockHttpClient = new MockHttpClient() {
            @Override
            public ClassicHttpResponse performRequest(ClassicHttpRequest request) throws IOException, URISyntaxException {
                super.incrementRequestsProcessedCount();

                assertThat(request.getUri().toString()).isEqualTo("https://mock-service.com/auth");

                BasicClassicHttpResponse response = new BasicClassicHttpResponse(HttpStatus.SC_NOT_FOUND);
                return response;
            }
        };

        AuthenticationServiceImpl service = new AuthenticationServiceImpl(new URL("https://mock-service.com"), "could-be-a:valid-token", mockHttpClient);

        Exception gotBackException = catchException(()->service.getJWT());
        
        assertThat(mockHttpClient.getRequestsProcessed()).as("code under tests never requested a JWT from the server.").isEqualTo(1);
        assertThat(gotBackException).isInstanceOf(AuthenticationException.class);
        AuthenticationException gotBackAuthEx = (AuthenticationException)gotBackException;
        assertThat(gotBackAuthEx.getMessage()).contains("Response from server");
    }

    @Test
    public void testAuthServiceGetsExceptionFromHttpClientThrowsDecentError() throws Exception {

        MockHttpClient mockHttpClient = new MockHttpClient() {
            @Override
            public ClassicHttpResponse performRequest(ClassicHttpRequest request) throws IOException, URISyntaxException {
                super.incrementRequestsProcessedCount();
                throw new IOException("Simulated failure from a unit test");
            }
        };

        AuthenticationServiceImpl service = new AuthenticationServiceImpl(new URL("https://mock-service.com"), "could-be-a:valid-token", mockHttpClient);

        Exception gotBackException = catchException(()->service.getJWT());
        
        assertThat(mockHttpClient.getRequestsProcessed()).as("code under tests never requested a JWT from the server.").isEqualTo(1);
        assertThat(gotBackException).isInstanceOf(AuthenticationException.class);
        AuthenticationException gotBackAuthEx = (AuthenticationException)gotBackException;
        assertThat(gotBackAuthEx.getMessage()).contains("Simulated failure from a unit test");
    }

    @Test
    public void testAuthServiceCanGetAJWT() throws Exception {

        String expectedClientId = "asdasdasads";
        String expectedRefreshToken = "valid-token";
        String expectedJwt = "my-jwt";

        MockHttpClient mockHttpClient = new MockHttpClient() {
            @Override
            public ClassicHttpResponse performRequest(ClassicHttpRequest request) throws IOException, URISyntaxException, ParseException {
                super.incrementRequestsProcessedCount();

                assertThat(request.getUri().toString()).isEqualTo("https://mock-service.com/auth");

                HttpEntity entity = request.getEntity();
                String requestBodyString = EntityUtils.toString(entity);

                Gson gson = new GsonFactory().getGson();
                AuthRequestPayload payload = gson.fromJson(requestBodyString, AuthRequestPayload.class);

                assertThat(payload.client_id).as("Client id field in request to auth endpoint is bad.").isEqualTo(expectedClientId);
                assertThat(payload.code).isNull();
                assertThat(payload.refresh_token).as("refresh token field in request to auth endpoint is bad.").isEqualTo(expectedRefreshToken);
                assertThat(payload.secret).isNull();

                // Formulate a mock response...
                BasicClassicHttpResponse response = new BasicClassicHttpResponse(HttpStatus.SC_OK);

                AuthResponsePayload responsePayload = new AuthResponsePayload();
                responsePayload.jwt = expectedJwt;
                responsePayload.refresh_token = null;

                String responseBodyString = gson.toJson(responsePayload);
                response.setEntity(new StringEntity(responseBodyString, ContentType.APPLICATION_JSON));

                return response;
            }
        };

        AuthenticationServiceImpl service = new AuthenticationServiceImpl(new URL("https://mock-service.com"), expectedRefreshToken+":"+expectedClientId, mockHttpClient);

        String jwt = service.getJWT();

        assertThat(mockHttpClient.getRequestsProcessed()).as("code under tests never requested a JWT from the server.").isEqualTo(1);
        assertThat(jwt).isNotNull().isNotBlank().isEqualTo(expectedJwt);
    }

    @Test
    public void testRejectedTokenCausesErrorToBeReported() throws Exception {

        String expectedClientId = "asdasdasads";
        String expectedRefreshToken = "valid-token";

        MockHttpClient mockHttpClient = new MockHttpClient() {
            @Override
            public ClassicHttpResponse performRequest(ClassicHttpRequest request) throws IOException, URISyntaxException, ParseException {
                super.incrementRequestsProcessedCount();

                assertThat(request.getUri().toString()).isEqualTo("https://mock-service.com/auth");

                HttpEntity entity = request.getEntity();
                String requestBodyString = EntityUtils.toString(entity);

                Gson gson = new GsonFactory().getGson();
                AuthRequestPayload payload = gson.fromJson(requestBodyString, AuthRequestPayload.class);

                assertThat(payload.client_id).as("Client id field in request to auth endpoint is bad.").isEqualTo(expectedClientId);
                assertThat(payload.code).isNull();
                assertThat(payload.refresh_token).as("refresh token field in request to auth endpoint is bad.").isEqualTo(expectedRefreshToken);
                assertThat(payload.secret).isNull();

                // Formulate a mock response with BAD_REQUEST...
                BasicClassicHttpResponse response = new BasicClassicHttpResponse(HttpStatus.SC_BAD_REQUEST);

                AuthError authError = new AuthError();
                authError.error_code = 99;
                authError.error_message = "The galasa token was bad";

                String responseBodyString = gson.toJson(authError);
                response.setEntity(new StringEntity(responseBodyString, ContentType.APPLICATION_JSON));

                return response;
            }
        };

        AuthenticationServiceImpl service = new AuthenticationServiceImpl(new URL("https://mock-service.com"), expectedRefreshToken+":"+expectedClientId, mockHttpClient);

        Throwable t = catchThrowable( ()-> { service.getJWT(); });

        assertThat(t).isNotNull().isInstanceOf(AuthenticationException.class);
        AuthenticationException ex = (AuthenticationException)t;
        assertThat(ex).hasMessageContaining("The galasa token was bad");
    }


    // This test was helpful to show that the AuthenticationService code should work against a real server.
    // To use it, you need to plug-in your own token.
    // @Test
    // public void testCanTargetARealEcosystem() throws Exception {
    //     String refreshToken = "xxx";
    //     String clientId = "xxx";
    //     String apiServerUrlString = "https:/my.server/api";
    //     HttpClient httpClient = HttpClientBuilder.create().build();
    //     URL apiServerUrl = new URL(apiServerUrlString);
    //     AuthenticationService service = new AuthenticationService(apiServerUrl, refreshToken+":"+clientId, httpClient);
    //     String jwt = service.getJWT();
    //     assertThat(jwt).isNotBlank().contains("zzzzz");
    // }
}
