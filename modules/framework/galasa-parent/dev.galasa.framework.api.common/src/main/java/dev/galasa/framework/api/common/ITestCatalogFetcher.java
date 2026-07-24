/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import java.io.OutputStream;

import dev.galasa.framework.spi.streams.IStream;

/**
 * Fetches the raw JSON content of a test catalog for a given stream.
 *
 * <p>Two fetch modes are provided:</p>
 * <ul>
 *   <li>{@link #streamTestCatalog} — writes bytes directly to a caller-supplied
 *       {@link OutputStream} without buffering the full body in memory. Use this
 *       when proxying the catalog to an HTTP response.</li>
 *   <li>{@link #fetchTestCatalog} — reads the full body and returns it as a
 *       {@code String}. Use this when the caller needs to parse the catalog
 *       (e.g. for test selection).</li>
 * </ul>
 */
public interface ITestCatalogFetcher {

    /**
     * Fetches the test catalog for the given stream and writes the raw JSON bytes
     * directly to {@code outputStream} without buffering the full body in memory.
     *
     * @param stream       the stream whose test catalog URL and optional Maven
     *                     credentials are used to retrieve the catalog.
     * @param outputStream the stream to write catalog bytes into.
     * @throws InternalServletException if the catalog cannot be fetched, the
     *         response status is not 200, the body exceeds the size limit, the
     *         content type is invalid, or credentials cannot be resolved.
     */
    void streamTestCatalog(IStream stream, OutputStream outputStream) throws InternalServletException;

    /**
     * Fetches the test catalog for the given stream and returns it as a JSON string.
     *
     * @param stream the stream whose test catalog URL and optional Maven credentials
     *               are used to retrieve the catalog.
     * @return the raw JSON body of the test catalog, or {@code null} if the stream
     *         has no test catalog URL configured.
     * @throws InternalServletException if the catalog cannot be fetched, the response
     *         status is not 200, the body exceeds the size limit, or the credentials
     *         cannot be resolved.
     */
    String fetchTestCatalog(IStream stream) throws InternalServletException;
}
