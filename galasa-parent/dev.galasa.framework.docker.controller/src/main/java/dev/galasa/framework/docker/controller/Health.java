/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.docker.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import dev.galasa.framework.spi.FrameworkException;

@SuppressWarnings("restriction")
public class Health implements HttpHandler {

    private final HttpServer httpServer;

    public Health(int port) throws FrameworkException {

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

        String response = "Ok";

        int responseCode = 200;

        exchange.sendResponseHeaders(responseCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

}
