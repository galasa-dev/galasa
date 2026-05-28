/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.streams;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import dev.galasa.framework.mocks.MockOBR;
import dev.galasa.framework.mocks.MockStream;
import dev.galasa.framework.spi.streams.IOBR;
import dev.galasa.framework.spi.streams.StreamsException;

public class TestStreamPropertiesTransform {

    @Test
    public void testGetStreamAsPropertiesWithAllFieldsReturnsCorrectProperties() throws Exception {
        // Given...
        String streamName = "testStream";
        String description = "Test stream description";
        String mavenRepoUrl = "https://maven.example.com/repo";
        String testCatalogUrl = "https://maven.example.com/testcatalog.json";
        String secretName = "my-maven-secret";

        List<IOBR> obrs = new ArrayList<>();
        obrs.add(new MockOBR("dev.galasa", "dev.galasa.obr", "0.1.0"));

        MockStream stream = new MockStream();
        stream.setName(streamName);
        stream.setDescription(description);
        stream.setMavenRepositoryUrl(mavenRepoUrl);
        stream.setTestCatalogUrl(testCatalogUrl);
        stream.setMavenSecretName(secretName);
        stream.setObrs(obrs);

        StreamPropertiesTransform transform = new StreamPropertiesTransform();

        // When...
        Map<String, String> properties = transform.getStreamAsProperties(stream);

        // Then...
        assertThat(properties).hasSize(5);
        assertThat(properties.get("test.stream.testStream.description")).isEqualTo(description);
        assertThat(properties.get("test.stream.testStream.location")).isEqualTo(testCatalogUrl);
        assertThat(properties.get("test.stream.testStream.repo")).isEqualTo(mavenRepoUrl);
        assertThat(properties.get("test.stream.testStream.repo.secret.name")).isEqualTo(secretName);
        assertThat(properties.get("test.stream.testStream.obr")).isEqualTo("mvn:dev.galasa/dev.galasa.obr/0.1.0/obr");
    }

    @Test
    public void testGetStreamAsPropertiesWithMultipleOBRsReturnsCommaSeparatedList() throws Exception {
        // Given...
        String streamName = "testStream";
        String mavenRepoUrl = "https://maven.example.com/repo";
        String testCatalogUrl = "https://maven.example.com/testcatalog.json";

        List<IOBR> obrs = new ArrayList<>();
        obrs.add(new MockOBR("dev.galasa", "dev.galasa.obr", "0.1.0"));
        obrs.add(new MockOBR("com.example", "example.obr", "1.2.3"));
        obrs.add(new MockOBR("org.test", "test.obr", "2.0.0-SNAPSHOT"));

        MockStream stream = new MockStream();
        stream.setName(streamName);
        stream.setMavenRepositoryUrl(mavenRepoUrl);
        stream.setTestCatalogUrl(testCatalogUrl);
        stream.setObrs(obrs);

        StreamPropertiesTransform transform = new StreamPropertiesTransform();

        // When...
        Map<String, String> properties = transform.getStreamAsProperties(stream);

        // Then...
        String expectedObrString = "mvn:dev.galasa/dev.galasa.obr/0.1.0/obr,mvn:com.example/example.obr/1.2.3/obr,mvn:org.test/test.obr/2.0.0-SNAPSHOT/obr";
        assertThat(properties.get("test.stream.testStream.obr")).isEqualTo(expectedObrString);
    }

    @Test
    public void testGetStreamAsPropertiesWithoutDescriptionOmitsDescriptionProperty() throws Exception {
        // Given...
        String streamName = "testStream";
        String mavenRepoUrl = "https://maven.example.com/repo";
        String testCatalogUrl = "https://maven.example.com/testcatalog.json";

        List<IOBR> obrs = new ArrayList<>();
        obrs.add(new MockOBR("dev.galasa", "dev.galasa.obr", "0.1.0"));

        MockStream stream = new MockStream();
        stream.setName(streamName);
        stream.setDescription(null);
        stream.setMavenRepositoryUrl(mavenRepoUrl);
        stream.setTestCatalogUrl(testCatalogUrl);
        stream.setObrs(obrs);

        StreamPropertiesTransform transform = new StreamPropertiesTransform();

        // When...
        Map<String, String> properties = transform.getStreamAsProperties(stream);

        // Then...
        assertThat(properties).hasSize(3);
        assertThat(properties).doesNotContainKey("test.stream.testStream.description");
        assertThat(properties.get("test.stream.testStream.location")).isEqualTo(testCatalogUrl);
        assertThat(properties.get("test.stream.testStream.repo")).isEqualTo(mavenRepoUrl);
        assertThat(properties.get("test.stream.testStream.obr")).isEqualTo("mvn:dev.galasa/dev.galasa.obr/0.1.0/obr");
    }

    @Test
    public void testGetStreamAsPropertiesWithoutSecretNameOmitsSecretNameProperty() throws Exception {
        // Given...
        String streamName = "testStream";
        String mavenRepoUrl = "https://maven.example.com/repo";
        String testCatalogUrl = "https://maven.example.com/testcatalog.json";

        List<IOBR> obrs = new ArrayList<>();
        obrs.add(new MockOBR("dev.galasa", "dev.galasa.obr", "0.1.0"));

        MockStream stream = new MockStream();
        stream.setName(streamName);
        stream.setMavenRepositoryUrl(mavenRepoUrl);
        stream.setTestCatalogUrl(testCatalogUrl);
        stream.setMavenSecretName(null);
        stream.setObrs(obrs);

        StreamPropertiesTransform transform = new StreamPropertiesTransform();

        // When...
        Map<String, String> properties = transform.getStreamAsProperties(stream);

        // Then...
        assertThat(properties).hasSize(3);
        assertThat(properties).doesNotContainKey("test.stream.testStream.repo.secret.name");
        assertThat(properties.get("test.stream.testStream.location")).isEqualTo(testCatalogUrl);
        assertThat(properties.get("test.stream.testStream.repo")).isEqualTo(mavenRepoUrl);
        assertThat(properties.get("test.stream.testStream.obr")).isEqualTo("mvn:dev.galasa/dev.galasa.obr/0.1.0/obr");
    }

    @Test
    public void testGetStreamAsPropertiesWithNullTestCatalogUrlThrowsException() throws Exception {
        // Given...
        String streamName = "testStream";
        String mavenRepoUrl = "https://maven.example.com/repo";

        List<IOBR> obrs = new ArrayList<>();
        obrs.add(new MockOBR("dev.galasa", "dev.galasa.obr", "0.1.0"));

        MockStream stream = new MockStream();
        stream.setName(streamName);
        stream.setMavenRepositoryUrl(mavenRepoUrl);
        stream.setTestCatalogUrl(null);
        stream.setObrs(obrs);

        StreamPropertiesTransform transform = new StreamPropertiesTransform();

        // When...
        StreamsException thrown = catchThrowableOfType(StreamsException.class, () -> {
            transform.getStreamAsProperties(stream);
        });

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("testcatalog URL is not set");
    }

    @Test
    public void testGetStreamAsPropertiesWithNullMavenRepoUrlThrowsException() throws Exception {
        // Given...
        String streamName = "testStream";
        String testCatalogUrl = "https://maven.example.com/testcatalog.json";

        List<IOBR> obrs = new ArrayList<>();
        obrs.add(new MockOBR("dev.galasa", "dev.galasa.obr", "0.1.0"));

        MockStream stream = new MockStream();
        stream.setName(streamName);
        stream.setMavenRepositoryUrl(null);
        stream.setTestCatalogUrl(testCatalogUrl);
        stream.setObrs(obrs);

        StreamPropertiesTransform transform = new StreamPropertiesTransform();

        // When...
        StreamsException thrown = catchThrowableOfType(StreamsException.class, () -> {
            transform.getStreamAsProperties(stream);
        });

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("maven repository URL is not set");
    }

    @Test
    public void testGetStreamAsPropertiesWithNullOBRsThrowsException() throws Exception {
        // Given...
        String streamName = "testStream";
        String mavenRepoUrl = "https://maven.example.com/repo";
        String testCatalogUrl = "https://maven.example.com/testcatalog.json";

        MockStream stream = new MockStream();
        stream.setName(streamName);
        stream.setMavenRepositoryUrl(mavenRepoUrl);
        stream.setTestCatalogUrl(testCatalogUrl);
        stream.setObrs(null);

        StreamPropertiesTransform transform = new StreamPropertiesTransform();

        // When...
        StreamsException thrown = catchThrowableOfType(StreamsException.class, () -> {
            transform.getStreamAsProperties(stream);
        });

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("no OBRs have been set");
    }

    @Test
    public void testGetStreamAsPropertiesWithEmptyOBRsThrowsException() throws Exception {
        // Given...
        String streamName = "testStream";
        String mavenRepoUrl = "https://maven.example.com/repo";
        String testCatalogUrl = "https://maven.example.com/testcatalog.json";

        MockStream stream = new MockStream();
        stream.setName(streamName);
        stream.setMavenRepositoryUrl(mavenRepoUrl);
        stream.setTestCatalogUrl(testCatalogUrl);
        stream.setObrs(new ArrayList<>());

        StreamPropertiesTransform transform = new StreamPropertiesTransform();

        // When...
        StreamsException thrown = catchThrowableOfType(StreamsException.class, () -> {
            transform.getStreamAsProperties(stream);
        });

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("no OBRs have been set");
    }

    @Test
    public void testGetStreamAsPropertiesWithStreamNameContainingDotsAndDashesWorks() throws Exception {
        // Given...
        String streamName = "my-test.stream_name";
        String mavenRepoUrl = "https://maven.example.com/repo";
        String testCatalogUrl = "https://maven.example.com/testcatalog.json";

        List<IOBR> obrs = new ArrayList<>();
        obrs.add(new MockOBR("dev.galasa", "dev.galasa.obr", "0.1.0"));

        MockStream stream = new MockStream();
        stream.setName(streamName);
        stream.setMavenRepositoryUrl(mavenRepoUrl);
        stream.setTestCatalogUrl(testCatalogUrl);
        stream.setObrs(obrs);

        StreamPropertiesTransform transform = new StreamPropertiesTransform();

        // When...
        Map<String, String> properties = transform.getStreamAsProperties(stream);

        // Then...
        assertThat(properties).hasSize(3);
        assertThat(properties.get("test.stream.my-test.stream_name.location")).isEqualTo(testCatalogUrl);
        assertThat(properties.get("test.stream.my-test.stream_name.repo")).isEqualTo(mavenRepoUrl);
        assertThat(properties.get("test.stream.my-test.stream_name.obr")).isEqualTo("mvn:dev.galasa/dev.galasa.obr/0.1.0/obr");
    }
}
