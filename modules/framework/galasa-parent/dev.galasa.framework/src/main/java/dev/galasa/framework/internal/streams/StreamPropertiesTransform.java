/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.streams;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.galasa.framework.spi.streams.IOBR;
import dev.galasa.framework.spi.streams.IStream;
import dev.galasa.framework.spi.streams.StreamsException;

public class StreamPropertiesTransform {

    private static final String TEST_STREAM_PROPERTY_PREFIX = "test.stream.";
    private static final String TEST_STREAM_DESCRIPTION_SUFFIX = ".description";
    private static final String TEST_STREAM_TESTCATALOG_SUFFIX = ".location";
    private static final String TEST_STREAM_OBR_SUFFIX = ".obr";
    private static final String TEST_STREAM_MAVEN_REPO_SUFFIX = ".repo";

    public Map<String, String> getStreamAsProperties(IStream stream) throws StreamsException {
        Map<String, String> streamProperties = new HashMap<>();

        String streamName = stream.getName();
        String description = stream.getDescription();
        if (description != null) {
            streamProperties.put(getStreamPropertyKey(streamName, TEST_STREAM_DESCRIPTION_SUFFIX), description);
        }

        URL testCatalogUrl = stream.getTestCatalogUrl();
        URL mavenRepoUrl = stream.getMavenRepositoryUrl();
        if (testCatalogUrl == null) {
            throw new StreamsException("Unable to get stream properties, testcatalog URL is not set");

        } else if (mavenRepoUrl == null) {
            throw new StreamsException("Unable to get stream properties, maven repository URL is not set");
        }

        streamProperties.put(getStreamPropertyKey(streamName, TEST_STREAM_TESTCATALOG_SUFFIX), testCatalogUrl.toString());
        streamProperties.put(getStreamPropertyKey(streamName, TEST_STREAM_MAVEN_REPO_SUFFIX), mavenRepoUrl.toString());

        List<IOBR> streamObrs = stream.getObrs();
        if (streamObrs == null || streamObrs.isEmpty()) {
            throw new StreamsException("Unable to get stream properties, no OBRs have been set");
        }

        List<String> obrStrings = new ArrayList<>();
        for (IOBR obr : streamObrs) {
            obrStrings.add(obr.toString());
        }

        streamProperties.put(getStreamPropertyKey(streamName, TEST_STREAM_OBR_SUFFIX), String.join(",", obrStrings));
        return streamProperties;
    }

    private String getStreamPropertyKey(String streamName, String suffix) {
        return TEST_STREAM_PROPERTY_PREFIX + streamName + suffix;
    }
}
