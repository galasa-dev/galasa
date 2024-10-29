/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common.impl.auth;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.net.URL;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;
import com.google.gson.Gson;
import org.apache.http.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import dev.galasa.plugin.common.AuthenticationException;
import dev.galasa.plugin.common.impl.GsonFactory;
import dev.galasa.plugin.common.impl.MockHttpClient;
import dev.galasa.plugin.common.impl.auth.beans.*;


public class AuthenticationServiceImplTest { 

    @Test
    public void testAuthServiceComplainsIfNullGalasaAuthTokenSupplied() throws Exception {
        Exception ex = catchException( ()-> new AuthenticationServiceImpl(new URL("http://not-null.com"), null, new MockHttpClient() ));
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
            public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
                // Note that the code under test has asked for execution of an Http request...
                super.incrementRequestsProcessedCount();

                assertThat(request.getURI().toString()).isEqualTo("https://mock-service.com/auth");

                StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_NOT_FOUND, "");
                BasicHttpResponse response = new BasicHttpResponse(statusLine);
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
            public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
                // Note that the code under test has asked for execution of an Http request...
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
            public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
                // Note that the code under test has asked for execution of an Http request...
                super.incrementRequestsProcessedCount();

                assertThat(request.getURI().toString()).isEqualTo("https://mock-service.com/auth");

                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                String requestBodyString = EntityUtils.toString(entity);

                Gson gson = new GsonFactory().getGson();
                AuthRequestPayload payload = gson.fromJson(requestBodyString, AuthRequestPayload.class);

                assertThat(payload.client_id).as("Client id field in request to auth endpoint is bad.").isEqualTo(expectedClientId);
                assertThat(payload.code).isNull();
                assertThat(payload.refresh_token).as("refresh token field in request to auth endpoint is bad.").isEqualTo(expectedRefreshToken);
                assertThat(payload.secret).isNull();

                // The request looks OK.

                // Formulate a mock response...
                StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "");
                BasicHttpResponse response = new BasicHttpResponse(statusLine);

                AuthResponsePayload responsePayload = new AuthResponsePayload();
                responsePayload.jwt = expectedJwt;
                responsePayload.refresh_token = null;

                String responseBodyString = gson.toJson(responsePayload);
                StringEntity responsePayloadEntity = new StringEntity(responseBodyString, ContentType.APPLICATION_JSON);
                response.setEntity(responsePayloadEntity);

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
            public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
                // Note that the code under test has asked for execution of an Http request...
                super.incrementRequestsProcessedCount();

                assertThat(request.getURI().toString()).isEqualTo("https://mock-service.com/auth");

                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                String requestBodyString = EntityUtils.toString(entity);

                Gson gson = new GsonFactory().getGson();
                AuthRequestPayload payload = gson.fromJson(requestBodyString, AuthRequestPayload.class);

                assertThat(payload.client_id).as("Client id field in request to auth endpoint is bad.").isEqualTo(expectedClientId);
                assertThat(payload.code).isNull();
                assertThat(payload.refresh_token).as("refresh token field in request to auth endpoint is bad.").isEqualTo(expectedRefreshToken);
                assertThat(payload.secret).isNull();

                // The request looks OK.

                // Formulate a mock response...
                StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_BAD_REQUEST, "");
                BasicHttpResponse response = new BasicHttpResponse(statusLine);

                AuthError authError = new AuthError();
                authError.error_code = 99;
                authError.error_message = "The galasa token was bad";

                String responseBodyString = gson.toJson(authError);
                StringEntity responsePayloadEntity = new StringEntity(responseBodyString, ContentType.APPLICATION_JSON);
                response.setEntity(responsePayloadEntity);

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
