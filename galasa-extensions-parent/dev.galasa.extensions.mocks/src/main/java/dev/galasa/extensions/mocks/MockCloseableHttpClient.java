/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.mocks;

import static org.assertj.core.api.Fail.fail;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.*;
import org.apache.http.protocol.HttpContext;

public class MockCloseableHttpClient extends CloseableHttpClient {

    private Iterator<HttpInteraction> interactionWalker;
    private HttpInteraction currentInteraction ;

    public MockCloseableHttpClient( List<HttpInteraction> interactions) {
        this.interactionWalker = interactions.iterator();
        nextInteraction();
    }

    // Bump over to the next interaction.
    private void nextInteraction() {
        if (interactionWalker.hasNext()) {
            this.currentInteraction = interactionWalker.next();
        } else {
            this.currentInteraction = null ;
        }
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
 
        System.out.printf("Http request:\n  target: %s \n  request: %s\n",target.toString(),request.toString());

        if (this.currentInteraction == null ) {
            String msg = "Mock http client was sent an HTTP request which wasn't expected.\nMock run out of expected http interactions.\n"+
                "request: "+request.toString();
            ;
            fail(msg);
            throw new ClientProtocolException(msg);
        }

        // Validate that the request is as expected.
        this.currentInteraction.validateRequest(target, request);

        System.out.printf("Http request: interaction %s received from the code under test as expected.\n",target.toString());

        // Prepare the response to return.
        CloseableHttpResponse response = this.currentInteraction.getResponse();

        // We've used up this http interaction.
        nextInteraction();

        return response;
    }

}
