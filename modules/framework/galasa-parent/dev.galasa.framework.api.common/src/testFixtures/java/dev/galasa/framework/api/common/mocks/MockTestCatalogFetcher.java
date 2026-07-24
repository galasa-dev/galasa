/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import dev.galasa.framework.api.common.ITestCatalogFetcher;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.spi.streams.IStream;

/**
 * Test mock for {@link ITestCatalogFetcher}.
 *
 * <p>By default returns/writes the JSON string supplied at construction time for
 * every call. Set an exception via {@link #setExceptionToThrow} to make both
 * methods throw instead.</p>
 */
public class MockTestCatalogFetcher implements ITestCatalogFetcher {

    private final String catalogJson;
    private InternalServletException exceptionToThrow;

    public MockTestCatalogFetcher(String catalogJson) {
        this.catalogJson = catalogJson;
    }

    public void setExceptionToThrow(InternalServletException exception) {
        this.exceptionToThrow = exception;
    }

    @Override
    public void streamTestCatalog(IStream stream, OutputStream outputStream) throws InternalServletException {
        if (exceptionToThrow != null) {
            throw exceptionToThrow;
        }
        if (catalogJson != null) {
            try {
                outputStream.write(catalogJson.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            } catch (IOException e) {
                throw new RuntimeException("MockTestCatalogFetcher: failed to write to output stream", e);
            }
        }
    }

    @Override
    public String fetchTestCatalog(IStream stream) throws InternalServletException {
        if (exceptionToThrow != null) {
            throw exceptionToThrow;
        }
        return catalogJson;
    }
}
