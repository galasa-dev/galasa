/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.runner;

import static org.assertj.core.api.Assertions.*;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import dev.galasa.ICredentials;
import dev.galasa.framework.MockCredentialsService;
import dev.galasa.framework.TestRunException;
import dev.galasa.framework.maven.repository.spi.IMavenRepository;
import dev.galasa.framework.mocks.MockMavenRepository;
import dev.galasa.framework.mocks.MockStream;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.CredentialsToken;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.ICredentialsService;

public class TestMavenRepositoryListBuilder {

    @Test
    public void testCanInstantiateMavenRepositoryListBuilder() {
        // Given...
        IMavenRepository mavenRepo = new MockMavenRepository();
        ICredentialsService credsService = new MockCredentialsService(new HashMap<>());

        // When...
        MavenRepositoryListBuilder builder = new MavenRepositoryListBuilder(mavenRepo, credsService);

        // Then...
        assertThat(builder).isNotNull();
    }

    @Test
    public void testAddMavenRepositoriesWithNullStreamDoesNotThrowException() throws Exception {
        // Given...
        IMavenRepository mavenRepo = new MockMavenRepository();
        ICredentialsService credsService = new MockCredentialsService(new HashMap<>());
        MavenRepositoryListBuilder builder = new MavenRepositoryListBuilder(mavenRepo, credsService);

        // When...
        builder.addMavenRepositories(null, null);

        // Then...
        assertThat(mavenRepo.getRemoteRepositories()).isEmpty();
    }

    @Test
    public void testAddMavenRepositoriesWithStreamAddsRepository() throws Exception {
        // Given...
        IMavenRepository mavenRepo = new MockMavenRepository();
        ICredentialsService credsService = new MockCredentialsService(new HashMap<>());
        MavenRepositoryListBuilder builder = new MavenRepositoryListBuilder(mavenRepo, credsService);

        MockStream stream = new MockStream();
        stream.setMavenRepositoryUrl("https://example.com/maven");

        // When...
        builder.addMavenRepositories(stream, null);

        // Then...
        assertThat(mavenRepo.getRemoteRepositories()).hasSize(1);
        assertThat(mavenRepo.getRemoteRepositories().get(0).toString()).isEqualTo("https://example.com/maven");
    }

    @Test
    public void testAddMavenRepositoriesWithMultipleRepositories() throws Exception {
        // Given...
        IMavenRepository mavenRepo = new MockMavenRepository();
        ICredentialsService credsService = new MockCredentialsService(new HashMap<>());
        MavenRepositoryListBuilder builder = new MavenRepositoryListBuilder(mavenRepo, credsService);

        String runRepoList = "https://example.com/maven2,https://example.com/maven3";

        // When...
        builder.addMavenRepositories(null, runRepoList);

        // Then...
        assertThat(mavenRepo.getRemoteRepositories()).hasSize(2);
        assertThat(mavenRepo.getRemoteRepositories().get(0).toString()).isEqualTo("https://example.com/maven2");
        assertThat(mavenRepo.getRemoteRepositories().get(1).toString()).isEqualTo("https://example.com/maven3");
    }

    @Test
    public void testAddMavenRepositoriesWithWhitespaceHandling() throws Exception {
        // Given...
        IMavenRepository mavenRepo = new MockMavenRepository();
        ICredentialsService credsService = new MockCredentialsService(new HashMap<>());
        MavenRepositoryListBuilder builder = new MavenRepositoryListBuilder(mavenRepo, credsService);

        String runRepoList = " https://example.com/maven1 , https://example.com/maven2 ";

        // When...
        builder.addMavenRepositories(null, runRepoList);

        // Then...
        assertThat(mavenRepo.getRemoteRepositories()).hasSize(2);
        assertThat(mavenRepo.getRemoteRepositories().get(0).toString()).isEqualTo("https://example.com/maven1");
        assertThat(mavenRepo.getRemoteRepositories().get(1).toString()).isEqualTo("https://example.com/maven2");
    }

    @Test
    public void testAddMavenRepositoriesSkipsEmptyEntries() throws Exception {
        // Given...
        IMavenRepository mavenRepo = new MockMavenRepository();
        ICredentialsService credsService = new MockCredentialsService(new HashMap<>());
        MavenRepositoryListBuilder builder = new MavenRepositoryListBuilder(mavenRepo, credsService);

        String runRepoList = "https://example.com/maven1,,https://example.com/maven2,  ,https://example.com/maven3";

        // When...
        builder.addMavenRepositories(null, runRepoList);

        // Then...
        assertThat(mavenRepo.getRemoteRepositories()).hasSize(3);
        assertThat(mavenRepo.getRemoteRepositories().get(0).toString()).isEqualTo("https://example.com/maven1");
        assertThat(mavenRepo.getRemoteRepositories().get(1).toString()).isEqualTo("https://example.com/maven2");
        assertThat(mavenRepo.getRemoteRepositories().get(2).toString()).isEqualTo("https://example.com/maven3");
    }

    @Test
    public void testAddMavenRepositoriesWithCredentials() throws Exception {
        // Given...
        MockMavenRepository mavenRepo = new MockMavenRepository();
        
        Map<String, ICredentials> credsMap = new HashMap<>();
        credsMap.put("maven-creds", new CredentialsUsernamePassword("testuser", "testpass"));
        ICredentialsService credsService = new MockCredentialsService(credsMap);
        
        MavenRepositoryListBuilder builder = new MavenRepositoryListBuilder(mavenRepo, credsService);

        MockStream stream = new MockStream();
        stream.setMavenRepositoryUrl("https://example.com/maven");
        stream.setMavenSecretName("maven-creds");

        // When...
        builder.addMavenRepositories(stream, null);

        // Then...
        assertThat(mavenRepo.getRemoteRepositories()).hasSize(1);
        assertThat(mavenRepo.getUsername()).isEqualTo("testuser");
        assertThat(mavenRepo.getPassword()).isEqualTo("testpass");
    }

    @Test
    public void testAddMavenRepositoriesWithNonUsernamePasswordCredentialsDoesNotAddRepository() throws Exception {
        // Given...
        MockMavenRepository mavenRepo = new MockMavenRepository();
        
        Map<String, ICredentials> credsMap = new HashMap<>();
        credsMap.put("maven-creds", new CredentialsToken("mytoken"));
        ICredentialsService credsService = new MockCredentialsService(credsMap);
        
        MavenRepositoryListBuilder builder = new MavenRepositoryListBuilder(mavenRepo, credsService);

        MockStream stream = new MockStream();
        stream.setMavenRepositoryUrl("https://example.com/maven");
        stream.setMavenSecretName("maven-creds");

        // When...
        Throwable thrown = catchThrowable(() -> {
            builder.addMavenRepositories(stream, null);
        });

        // Then...
        assertThat(mavenRepo.getRemoteRepositories()).isEmpty();
        assertThat(thrown).isInstanceOf(TestRunException.class);
        assertThat(thrown.getMessage()).contains("Unsupported credentials type provided");
    }

    @Test
    public void testAddMavenRepositoriesWithMissingCredentialsThrowsException() throws Exception {
        // Given...
        IMavenRepository mavenRepo = new MockMavenRepository();
        ICredentialsService credsService = new MockCredentialsService(new HashMap<>());
        MavenRepositoryListBuilder builder = new MavenRepositoryListBuilder(mavenRepo, credsService);

        MockStream stream = new MockStream();
        stream.setMavenRepositoryUrl("https://example.com/maven");
        stream.setMavenSecretName("non-existent-creds");

        // When...
        Throwable thrown = catchThrowable(() -> {
            builder.addMavenRepositories(stream, null);
        });

        // Then...
        assertThat(thrown).isInstanceOf(TestRunException.class);
        assertThat(thrown.getMessage()).contains("Could not find credentials with ID:");
        assertThat(thrown.getMessage()).contains("non-existent-creds");
    }

    @Test
    public void testAddMavenRepositoriesWithCredentialsServiceErrorThrowsException() throws Exception {
        // Given...
        IMavenRepository mavenRepo = new MockMavenRepository();
        MockCredentialsService credsService = new MockCredentialsService(new HashMap<>());
        credsService.setThrowError(true);
        
        MavenRepositoryListBuilder builder = new MavenRepositoryListBuilder(mavenRepo, credsService);

        MockStream stream = new MockStream();
        stream.setMavenRepositoryUrl("https://example.com/maven");
        stream.setMavenSecretName("maven-creds");

        // When...
        Throwable thrown = catchThrowable(() -> {
            builder.addMavenRepositories(stream, null);
        });

        // Then...
        assertThat(thrown).isInstanceOf(TestRunException.class);
        assertThat(thrown.getMessage()).contains("Failed to load maven credentials");
        assertThat(thrown.getCause()).isInstanceOf(CredentialsException.class);
    }

    @Test
    public void testAddMavenRepositoriesWithMalformedURLThrowsException() throws Exception {
        // Given...
        IMavenRepository mavenRepo = new MockMavenRepository();
        ICredentialsService credsService = new MockCredentialsService(new HashMap<>());
        MavenRepositoryListBuilder builder = new MavenRepositoryListBuilder(mavenRepo, credsService);

        String runRepoList = "not-a-valid-url";

        // When...
        Throwable thrown = catchThrowable(() -> {
            builder.addMavenRepositories(null, runRepoList);
        });

        // Then...
        assertThat(thrown).isInstanceOf(TestRunException.class);
        assertThat(thrown.getMessage()).contains("Unable to add remote maven repository");
        assertThat(thrown.getCause()).isInstanceOf(MalformedURLException.class);
    }
}
