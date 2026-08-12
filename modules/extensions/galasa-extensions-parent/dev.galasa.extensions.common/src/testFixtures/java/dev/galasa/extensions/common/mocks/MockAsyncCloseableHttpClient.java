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

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;

public class MockAsyncCloseableHttpClient extends CloseableHttpClient {

    private List<HttpInteraction> interactions;

    public MockAsyncCloseableHttpClient(List<HttpInteraction> interactions) {
        this.interactions = new ArrayList<>(interactions);
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

        ClassicHttpResponse response = null;
        System.out.printf("Http request:\n  target: %s \n  request: %s\n", target.toString(), request.toString());

        for (HttpInteraction interaction : interactions) {
            try {
                interaction.validateRequest(target, request);
                System.out.printf("Http request: interaction %s received from the code under test as expected.\n", target.toString());

                response = interaction.getResponse();
                break;
            } catch (AssertionError e) {
                // Try the next interaction...
            }
        }

        if (response == null) {
            String msg = "Mock http client was sent an HTTP request which wasn't expected or ran out of expected http interactions.\n" +
                "request: " + request.toString();
            fail(msg);
        }

        return CloseableHttpResponse.adapt(response);
    }

}
