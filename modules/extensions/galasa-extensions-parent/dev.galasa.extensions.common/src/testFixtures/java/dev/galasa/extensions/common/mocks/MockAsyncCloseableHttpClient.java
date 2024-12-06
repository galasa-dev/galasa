/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.mocks;

import static org.assertj.core.api.Fail.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.*;
import org.apache.http.protocol.HttpContext;

public class MockAsyncCloseableHttpClient extends CloseableHttpClient {

    private List<HttpInteraction> interactions;

    public MockAsyncCloseableHttpClient(List<HttpInteraction> interactions) {
        this.interactions = new ArrayList<>(interactions);
    }

    @Override
    @SuppressWarnings("deprecation")
    public HttpParams getParams() {
        throw new UnsupportedOperationException("Unimplemented method 'getParams'");
    }

    @Override
    @SuppressWarnings("deprecation")
    public ClientConnectionManager getConnectionManager() {
        throw new UnsupportedOperationException("Unimplemented method 'getConnectionManager'");
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'close'");
    }

    @Override
    protected CloseableHttpResponse doExecute(
        HttpHost target, HttpRequest request, HttpContext context
    ) throws IOException, ClientProtocolException {
 
        CloseableHttpResponse response = null;
        System.out.printf("Http request:\n  target: %s \n  request: %s\n",target.toString(),request.toString());

        for (HttpInteraction interaction : interactions) {
            try {
                interaction.validateRequest(target, request);
                System.out.printf("Http request: interaction %s received from the code under test as expected.\n",target.toString());

                response = interaction.getResponse();
                break;
            } catch (AssertionError e) {
                // Try the next interaction...
            }
        }

        if (response == null) {
            String msg = "Mock http client was sent an HTTP request which wasn't expected or ran out of expected http interactions.\n"+
                "request: "+request.toString();
            fail(msg);
        }

        return response;
    }

}
