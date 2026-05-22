/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.maven.repository.internal.mocks;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Mock URLConnection that stores request properties for verification
 */
public class MockURLConnection extends URLConnection {
    private Map<String, String> requestProperties = new HashMap<>();

    public MockURLConnection(URL url) {
        super(url);
    }

    @Override
    public void connect() throws IOException {
    }

    @Override
    public void setRequestProperty(String key, String value) {
        requestProperties.put(key, value);
    }

    @Override
    public String getRequestProperty(String key) {
        return requestProperties.get(key);
    }
}
