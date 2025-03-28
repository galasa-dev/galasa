/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.streams;

import static org.assertj.core.api.Assertions.*;

import java.net.URL;
import java.util.List;

import org.junit.Test;

import dev.galasa.framework.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.streams.IOBR;
import dev.galasa.framework.spi.streams.IStream;
import dev.galasa.framework.spi.streams.StreamsException;

public class TestStreamServiceImpl {
    
    private void addStreamToCps(
        MockIConfigurationPropertyStoreService mockCps,
        String streamName,
        String description,
        String mavenRepoUrl,
        String commaSeparatedObrUrls,
        String testcatalogUrl
    ) throws ConfigurationPropertyStoreException {
        mockCps.setProperty("test.stream." + streamName + ".obr", commaSeparatedObrUrls);
        mockCps.setProperty("test.stream." + streamName + ".description", description);
        mockCps.setProperty("test.stream." + streamName + ".repo", mavenRepoUrl);
        mockCps.setProperty("test.stream." + streamName + ".location", testcatalogUrl);
    }

    @Test
    public void testGetStreamsWithNoStreamsReturnsEmptyListOK() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();        

        StreamsServiceImpl streamsService = new StreamsServiceImpl(mockCps);

        // When...
        List<IStream> streams = streamsService.getStreams();

        // Then...
        assertThat(streams).isEmpty();
    }

    @Test
    public void testGetStreamsReturnsOneStreamOK() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();

        String streamName1 = "stream1";
        String streamDescription1 = "this is the first test stream!";
        String streamObr1 = "mvn:my.company/my.company.obr/0.0.1/obr";
        String streamMavenRepo1 = "https://my.company/maven/repository";
        String streamTestCatalog1 = "https://my.company/maven/repository/testcatalog.json";
        
        addStreamToCps(mockCps, streamName1, streamDescription1, streamMavenRepo1, streamObr1, streamTestCatalog1);

        StreamsServiceImpl streamsService = new StreamsServiceImpl(mockCps);

        // When...
        List<IStream> streams = streamsService.getStreams();

        // Then...
        assertThat(streams).hasSize(1);

        IStream stream = streams.get(0);
        assertThat(stream.getName()).isEqualTo(streamName1);
        assertThat(stream.getDescription()).isEqualTo(streamDescription1);
        assertThat(stream.getMavenRepositoryUrl()).isEqualTo(new URL(streamMavenRepo1));
        assertThat(stream.getTestCatalogUrl()).isEqualTo(new URL(streamTestCatalog1));

        List<IOBR> obrs = stream.getObrs();
        assertThat(obrs).hasSize(1);

        IOBR obr = obrs.get(0);
        assertThat(obr.getGroupId()).isEqualTo("my.company");
        assertThat(obr.getArtifactId()).isEqualTo("my.company.obr");
        assertThat(obr.getVersion()).isEqualTo("0.0.1");
    }

    @Test
    public void testGetStreamsReturnsMultipleStreamsOK() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();

        String streamName1 = "stream1";
        String streamDescription1 = "this is the first test stream!";
        String streamObr1 = "mvn:my.company/my.company.obr/0.0.1/obr";
        String streamMavenRepo1 = "https://my.company/maven/repository";
        String streamTestCatalog1 = "https://my.company/maven/repository/testcatalog.json";
        
        String streamName2 = "stream2";
        String streamDescription2 = "this is the second test stream!";
        String streamObr2 = "mvn:my.0ther.company/my.0ther.company.obr/v1-SNAPSHOT/obr,mvn:my.group/my.group.obr/0.0.1/obr";
        String streamMavenRepo2 = "https://my.other.company/maven/repository";
        String streamTestCatalog2 = "https://my.other.company/maven/repository/testcatalog.json";

        addStreamToCps(mockCps, streamName1, streamDescription1, streamMavenRepo1, streamObr1, streamTestCatalog1);
        addStreamToCps(mockCps, streamName2, streamDescription2, streamMavenRepo2, streamObr2, streamTestCatalog2);

        StreamsServiceImpl streamsService = new StreamsServiceImpl(mockCps);

        // When...
        List<IStream> streams = streamsService.getStreams();

        // Then...
        assertThat(streams).hasSize(2);
        assertThat(streams).extracting("name").contains(streamName1, streamName2);
        assertThat(streams).extracting("description").contains(streamDescription1, streamDescription2);
        assertThat(streams).extracting("mavenRepositoryUrl").contains(new URL(streamMavenRepo1), new URL(streamMavenRepo2));
        assertThat(streams).extracting("testCatalogUrl").contains(new URL(streamTestCatalog1), new URL(streamTestCatalog2));
    }

    @Test
    public void testGetStreamByNameReturnsCorrectStreamOK() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();

        String streamName1 = "stream1";
        String streamDescription1 = "this is the first test stream!";
        String streamObr1 = "mvn:my.company/my.company.obr/0.0.1/obr";
        String streamMavenRepo1 = "https://my.company/maven/repository";
        String streamTestCatalog1 = "https://my.company/maven/repository/testcatalog.json";
        
        String streamName2 = "stream2";
        String streamDescription2 = "this is the second test stream!";
        String streamObr2 = "mvn:my.0ther.company/my.0ther.company.obr/v1-SNAPSHOT/obr,mvn:my.group/my.group.obr/0.0.1/obr";
        String streamMavenRepo2 = "https://my.other.company/maven/repository";
        String streamTestCatalog2 = "https://my.other.company/maven/repository/testcatalog.json";

        addStreamToCps(mockCps, streamName1, streamDescription1, streamMavenRepo1, streamObr1, streamTestCatalog1);
        addStreamToCps(mockCps, streamName2, streamDescription2, streamMavenRepo2, streamObr2, streamTestCatalog2);

        StreamsServiceImpl streamsService = new StreamsServiceImpl(mockCps);

        // When...
        IStream stream = streamsService.getStreamByName(streamName1);

        // Then...
        assertThat(stream.getName()).isEqualTo(streamName1);
        assertThat(stream.getDescription()).isEqualTo(streamDescription1);
        assertThat(stream.getMavenRepositoryUrl()).isEqualTo(new URL(streamMavenRepo1));
        assertThat(stream.getTestCatalogUrl()).isEqualTo(new URL(streamTestCatalog1));

        List<IOBR> obrs = stream.getObrs();
        assertThat(obrs).hasSize(1);

        IOBR obr = obrs.get(0);
        assertThat(obr.getGroupId()).isEqualTo("my.company");
        assertThat(obr.getArtifactId()).isEqualTo("my.company.obr");
        assertThat(obr.getVersion()).isEqualTo("0.0.1");
    }

    @Test
    public void testGetStreamByNameWithNoMatchingStreamReturnsNullOk() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();

        String streamName1 = "stream1";
        String streamDescription1 = "this is the first test stream!";
        String streamObr1 = "mvn:my.company/my.company.obr/0.0.1/obr";
        String streamMavenRepo1 = "https://my.company/maven/repository";
        String streamTestCatalog1 = "https://my.company/maven/repository/testcatalog.json";
        
        String streamName2 = "stream2";
        String streamDescription2 = "this is the second test stream!";
        String streamObr2 = "mvn:my.0ther.company/my.0ther.company.obr/v1-SNAPSHOT/obr,mvn:my.group/my.group.obr/0.0.1/obr";
        String streamMavenRepo2 = "https://my.other.company/maven/repository";
        String streamTestCatalog2 = "https://my.other.company/maven/repository/testcatalog.json";

        addStreamToCps(mockCps, streamName1, streamDescription1, streamMavenRepo1, streamObr1, streamTestCatalog1);
        addStreamToCps(mockCps, streamName2, streamDescription2, streamMavenRepo2, streamObr2, streamTestCatalog2);

        StreamsServiceImpl streamsService = new StreamsServiceImpl(mockCps);

        // When...
        IStream stream = streamsService.getStreamByName("not-a-known-stream");

        // Then...
        assertThat(stream).isNull();
    }

    @Test
    public void testGetStreamsWithInvalidStreamOBRThrowsError() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();

        String streamName1 = "stream1";
        String streamDescription1 = "this is the first test stream!";
        String streamObr1 = "mvn:!?not.a.valid!!!/obr/URL!/obr";
        String streamMavenRepo1 = "https://my.company/maven/repository";
        String streamTestCatalog1 = "https://my.company/maven/repository/testcatalog.json";
        
        addStreamToCps(mockCps, streamName1, streamDescription1, streamMavenRepo1, streamObr1, streamTestCatalog1);

        StreamsServiceImpl streamsService = new StreamsServiceImpl(mockCps);

        // When...
        StreamsException thrown = catchThrowableOfType(() -> {
            streamsService.getStreams();
        }, StreamsException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Invalid OBR provided");
    }

    @Test
    public void testGetStreamsWithMissingOBRMvnPrefixThrowsError() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();

        String streamName1 = "stream1";
        String streamDescription1 = "this is the first test stream!";
        String streamObr1 = "this.obr.is.missing/the.mvn.prefix/0.0.1/obr";
        String streamMavenRepo1 = "https://my.company/maven/repository";
        String streamTestCatalog1 = "https://my.company/maven/repository/testcatalog.json";
        
        addStreamToCps(mockCps, streamName1, streamDescription1, streamMavenRepo1, streamObr1, streamTestCatalog1);

        StreamsServiceImpl streamsService = new StreamsServiceImpl(mockCps);

        // When...
        StreamsException thrown = catchThrowableOfType(() -> {
            streamsService.getStreams();
        }, StreamsException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Missing 'mvn:' protocol prefix");
    }

    @Test
    public void testGetStreamsWithTooManyOBRPartsThrowsError() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();

        String streamName1 = "stream1";
        String streamDescription1 = "this is the first test stream!";
        String streamObr1 = "mvn:this/obr/has/too/many/slashes/obr";
        String streamMavenRepo1 = "https://my.company/maven/repository";
        String streamTestCatalog1 = "https://my.company/maven/repository/testcatalog.json";
        
        addStreamToCps(mockCps, streamName1, streamDescription1, streamMavenRepo1, streamObr1, streamTestCatalog1);

        StreamsServiceImpl streamsService = new StreamsServiceImpl(mockCps);

        // When...
        StreamsException thrown = catchThrowableOfType(() -> {
            streamsService.getStreams();
        }, StreamsException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Invalid OBR URL provided. OBRs should be in the form 'mvn:groupId/artifactId/version/obr'");
    }

    @Test
    public void testGetStreamsWithTooFewOBRPartsThrowsError() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();

        String streamName1 = "stream1";
        String streamDescription1 = "this is the first test stream!";
        String streamObr1 = "mvn:this.obr.has/too.few.parts";
        String streamMavenRepo1 = "https://my.company/maven/repository";
        String streamTestCatalog1 = "https://my.company/maven/repository/testcatalog.json";
        
        addStreamToCps(mockCps, streamName1, streamDescription1, streamMavenRepo1, streamObr1, streamTestCatalog1);

        StreamsServiceImpl streamsService = new StreamsServiceImpl(mockCps);

        // When...
        StreamsException thrown = catchThrowableOfType(() -> {
            streamsService.getStreams();
        }, StreamsException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Invalid OBR URL provided. OBRs should be in the form 'mvn:groupId/artifactId/version/obr'");
    }

    @Test
    public void testGetStreamsWithInvalidStreamNameThrowsError() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();

        String streamName1 = "Not a valid stream name!";
        String streamDescription1 = "this is the first test stream!";
        String streamObr1 = "mvn:my.company/my.company.obr/0.0.1/obr";
        String streamMavenRepo1 = "https://my.company/maven/repository";
        String streamTestCatalog1 = "https://my.company/maven/repository/testcatalog.json";
        
        addStreamToCps(mockCps, streamName1, streamDescription1, streamMavenRepo1, streamObr1, streamTestCatalog1);

        StreamsServiceImpl streamsService = new StreamsServiceImpl(mockCps);

        // When...
        StreamsException thrown = catchThrowableOfType(() -> {
            streamsService.getStreams();
        }, StreamsException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Invalid stream name provided");
    }

    @Test
    public void testGetStreamsWithInvalidMavenRepoUrlThrowsError() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();

        String streamName1 = "stream1";
        String streamDescription1 = "this is the first test stream!";
        String streamObr1 = "mvn:my.company/my.company.obr/0.0.1/obr";
        String streamMavenRepo1 = "[ not $ a valid maven repo URL!!";
        String streamTestCatalog1 = "https://my.company/maven/repository/testcatalog.json";
        
        addStreamToCps(mockCps, streamName1, streamDescription1, streamMavenRepo1, streamObr1, streamTestCatalog1);

        StreamsServiceImpl streamsService = new StreamsServiceImpl(mockCps);

        // When...
        StreamsException thrown = catchThrowableOfType(() -> {
            streamsService.getStreams();
        }, StreamsException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Invalid maven repository URL provided");
    }

    @Test
    public void testGetStreamsWithInvalidTestCatalogUrlThrowsError() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();

        String streamName1 = "stream1";
        String streamDescription1 = "this is the first test stream!";
        String streamObr1 = "mvn:my.company/my.company.obr/0.0.1/obr";
        String streamMavenRepo1 = "https://my.company/maven/repository";
        String streamTestCatalog1 = "[ not a valid $ testcatalog URL!";
        
        addStreamToCps(mockCps, streamName1, streamDescription1, streamMavenRepo1, streamObr1, streamTestCatalog1);

        StreamsServiceImpl streamsService = new StreamsServiceImpl(mockCps);

        // When...
        StreamsException thrown = catchThrowableOfType(() -> {
            streamsService.getStreams();
        }, StreamsException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("Invalid testcatalog URL provided");
    }

    @Test
    public void testGetStreamsWithUnknownStreamPropertyIgnoresPropertyOk() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();

        String streamName1 = "stream1";
        String streamDescription1 = "this is the first test stream!";
        String streamObr1 = "mvn:my.company/my.company.obr/0.0.1/obr";
        String streamMavenRepo1 = "https://my.company/maven/repository";
        String streamTestCatalog1 = "https://my.company/maven/repository/testcatalog.json";
        
        addStreamToCps(mockCps, streamName1, streamDescription1, streamMavenRepo1, streamObr1, streamTestCatalog1);
        mockCps.setProperty("test.stream." + streamName1 + ".unknown", "this is an unknown property which should be ignored");

        StreamsServiceImpl streamsService = new StreamsServiceImpl(mockCps);

        // When...
        List<IStream> streams = streamsService.getStreams();

        // Then...
        assertThat(streams).hasSize(1);

        IStream stream = streams.get(0);
        assertThat(stream.getName()).isEqualTo(streamName1);
        assertThat(stream.getDescription()).isEqualTo(streamDescription1);
        assertThat(stream.getMavenRepositoryUrl()).isEqualTo(new URL(streamMavenRepo1));
        assertThat(stream.getTestCatalogUrl()).isEqualTo(new URL(streamTestCatalog1));

        List<IOBR> obrs = stream.getObrs();
        assertThat(obrs).hasSize(1);

        IOBR obr = obrs.get(0);
        assertThat(obr.getGroupId()).isEqualTo("my.company");
        assertThat(obr.getArtifactId()).isEqualTo("my.company.obr");
        assertThat(obr.getVersion()).isEqualTo("0.0.1");
    }

    @Test
    public void testGetStreamByNameWithTrailingOBRSlashStripsSlashOk() throws Exception {
        // Given...
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();

        String streamName1 = "stream1";
        String streamDescription1 = "this is the first test stream!";
        String streamObr1 = "mvn:my.company/my.company.obr/0.0.1/obr/";
        String streamMavenRepo1 = "https://my.company/maven/repository";
        String streamTestCatalog1 = "https://my.company/maven/repository/testcatalog.json";

        addStreamToCps(mockCps, streamName1, streamDescription1, streamMavenRepo1, streamObr1, streamTestCatalog1);

        StreamsServiceImpl streamsService = new StreamsServiceImpl(mockCps);

        // When...
        IStream stream = streamsService.getStreamByName(streamName1);

        // Then...
        assertThat(stream.getName()).isEqualTo(streamName1);
        assertThat(stream.getDescription()).isEqualTo(streamDescription1);
        assertThat(stream.getMavenRepositoryUrl()).isEqualTo(new URL(streamMavenRepo1));
        assertThat(stream.getTestCatalogUrl()).isEqualTo(new URL(streamTestCatalog1));

        List<IOBR> obrs = stream.getObrs();
        assertThat(obrs).hasSize(1);

        IOBR obr = obrs.get(0);
        String expectedObrString = "mvn:my.company/my.company.obr/0.0.1/obr";
        assertThat(obr.toString()).isEqualTo(expectedObrString);
        assertThat(obr.getGroupId()).isEqualTo("my.company");
        assertThat(obr.getArtifactId()).isEqualTo("my.company.obr");
        assertThat(obr.getVersion()).isEqualTo("0.0.1");
    }
}
