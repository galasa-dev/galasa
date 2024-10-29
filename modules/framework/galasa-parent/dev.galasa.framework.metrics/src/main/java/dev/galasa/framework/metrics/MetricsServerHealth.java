/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.metrics;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import dev.galasa.framework.spi.FrameworkException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class MetricsServerHealth implements HttpHandler {

    private final MetricsServer resourceManagement;

    private final HttpServer    httpServer;

    public MetricsServerHealth(MetricsServer resourceManagement, int port) throws FrameworkException {
        this.resourceManagement = resourceManagement;

        try {
            this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            this.httpServer.createContext("/", this);
            this.httpServer.start();
        } catch (IOException e) {
            throw new FrameworkException("Unable to initialise the health http server", e);
        }

    }

    public void shutdown() {
        this.httpServer.stop(0);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, 0);
            exchange.getResponseBody().close();
            return;
        }

        long successfulRuns = this.resourceManagement.getSuccessfulPollsSinceLastHealthCheck();

        String response = "successfulpolls=" + Long.toString(successfulRuns);
        int responseCode = 200;
        if (successfulRuns == 0) {
            responseCode = 500;
        }

        exchange.sendResponseHeaders(responseCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

}
