/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.mocks;

import static org.assertj.core.api.Fail.fail;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;

public class MockCloseableHttpClient extends CloseableHttpClient {

    private Iterator<HttpInteraction> interactionWalker;
    private HttpInteraction currentInteraction;

    public MockCloseableHttpClient(List<HttpInteraction> interactions) {
        this.interactionWalker = interactions.iterator();
        nextInteraction();
    }

    // Bump over to the next interaction.
    private void nextInteraction() {
        if (interactionWalker.hasNext()) {
            this.currentInteraction = interactionWalker.next();
        } else {
            this.currentInteraction = null;
        }
    }

    @Override
    public void close() throws IOException {
        // Do nothing...
    }

    @Override
    public void close(CloseMode closeMode) {
        // Do nothing...
    }

    @Override
    protected CloseableHttpResponse doExecute(
        HttpHost target, ClassicHttpRequest request, HttpContext context
    ) throws IOException {

        System.out.printf("Http request:\n  target: %s \n  request: %s\n", target.toString(), request.toString());

        if (this.currentInteraction == null) {
            String msg = "Mock http client was sent an HTTP request which wasn't expected.\nMock run out of expected http interactions.\n" +
                "request: " + request.toString();
            fail(msg);
            throw new IOException(msg);
        }

        // Validate that the request is as expected.
        this.currentInteraction.validateRequest(target, request);

        System.out.printf("Http request: interaction %s received from the code under test as expected.\n", target.toString());

        // Prepare the response to return.
        ClassicHttpResponse response = this.currentInteraction.getResponse();

        // We've used up this http interaction.
        nextInteraction();

        return CloseableHttpResponse.adapt(response);
    }

}
