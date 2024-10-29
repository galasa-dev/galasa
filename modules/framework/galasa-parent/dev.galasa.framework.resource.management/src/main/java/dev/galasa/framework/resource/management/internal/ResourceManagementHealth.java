/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Instant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.FrameworkException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class ResourceManagementHealth implements HttpHandler {
    
    private Log                      logger = LogFactory.getLog(this.getClass());

    private final ResourceManagement resourceManagement;

    private final HttpServer         httpServer;

    public ResourceManagementHealth(ResourceManagement resourceManagement, int port) throws FrameworkException {
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
        
        logger.trace("Health check request received");

        Instant lastSuccessfulRun = this.resourceManagement.getLastSuccessfulRun();
        
        Instant deadline = Instant.now().minusSeconds(60); // Must have run in the last minute

        String response = "lastsuccessfulrun=" + lastSuccessfulRun.toString();
        int responseCode = 200;
        if (lastSuccessfulRun.isBefore(deadline)) {
            logger.error("Health check failed, there were no successful runs since last health check");
            responseCode = 500;
        }

        exchange.sendResponseHeaders(responseCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

}