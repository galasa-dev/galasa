/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.mocks;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.servlet.http.HttpSession;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.authentication.IOidcProvider;

public class MockOidcProvider implements IOidcProvider {

    private String redirectUrl;

    private boolean isJwtValid = false;

    private boolean throwException = false;

    private HttpResponse<String> mockResponse;

    public MockOidcProvider() {}

    public MockOidcProvider(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public MockOidcProvider(HttpResponse<String> mockResponse) {
        this.mockResponse = mockResponse;
    }

    public void setJwtValid(boolean isJwtValid) {
        this.isJwtValid = isJwtValid;
    }

    public void setThrowException(boolean throwException) {
        this.throwException = throwException;
    }

    @Override
    public JsonObject getOpenIdConfiguration() throws IOException, InterruptedException {
        return null;
    }

    @Override
    public boolean isJwtValid(String jwt)
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InterruptedException {
        if (throwException) {
            throwIOException();
        }
        return isJwtValid;
    }

    @Override
    public String getConnectorRedirectUrl(String clientId, String callbackUrl, HttpSession session)
            throws IOException, InterruptedException {
        if (throwException) {
            throwIOException();
        }
        return redirectUrl;
    }

    @Override
    public HttpResponse<String> sendTokenPost(String clientId, String clientSecret, String refreshToken)
            throws IOException, InterruptedException {
        if (throwException) {
            throwIOException();
        }
        return mockResponse;
    }

    @Override
    public HttpResponse<String> sendTokenPost(String clientId, String clientSecret, String authCode, String redirectUri)
            throws IOException, InterruptedException {
        if (throwException) {
            throwIOException();
        }
        return mockResponse;
    }

    @Override
    public HttpResponse<String> sendAuthorizationGet(String clientId, String callbackUrl, HttpSession session)
            throws IOException, InterruptedException {
        if (throwException) {
            throwIOException();
        }
        return mockResponse;
    }

    private void throwIOException() throws IOException {
        throw new IOException("simulating an unexpected failure!");
    }
}
