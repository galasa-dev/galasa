/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.maven.plugin.galasa;

import java.net.URL;
import org.junit.Test;

import org.apache.maven.project.MavenProject;

public class DeployTestCatalogTest { 

    @Test
    public void testCanCreateDeployTestCatalog() {
        new DeployTestCatalog();
    }

    @Test
    public void testSkipCatalogDeployOldSpellingStillSkipsDoingWork() throws Exception {
        DeployTestCatalog command = new DeployTestCatalog();
        MockMavenLog mockLog = new MockMavenLog();
        command.setLog(mockLog);

        command.testStream = "myTestStream";
        command.bootstrapUrl = new URL("http://myBootstrapUrl");

        MavenProject project = new MavenProject();
        project.setPackaging("galasa-obr");
        command.project = project;

        // The old spelling is detected.
        command.skipDeployTestCatalogOldSpelling = true;

        command.execute();

        String expectedLogRecord = "INFO:Skipping Deploy Test Catalog - because the property galasa.skip.deploytestcatalog or galasa.skip.bundletestcatalog is set";
        mockLog.assertContainsRecord(expectedLogRecord);
    }

    @Test
    public void testSkipCatalogDeployNewSpellingStillSkipsDoingWork() throws Exception {
        DeployTestCatalog command = new DeployTestCatalog();
        MockMavenLog mockLog = new MockMavenLog();
        command.setLog(mockLog);

        command.testStream = "myTestStream";
        command.bootstrapUrl = new URL("http://myBootstrapUrl");

        MavenProject project = new MavenProject();
        project.setPackaging("galasa-obr");
        command.project = project;

        // The old spelling is detected.
        command.skipDeployTestCatalog = true;

        command.execute();

        String expectedLogRecord = "INFO:Skipping Deploy Test Catalog - because the property galasa.skip.deploytestcatalog or galasa.skip.bundletestcatalog is set";
        mockLog.assertContainsRecord(expectedLogRecord);
    }

    @Test
    public void testSkipCatalogDeployNewAndOldSpellingStillSkipsDoingWork() throws Exception {
        DeployTestCatalog command = new DeployTestCatalog();
        MockMavenLog mockLog = new MockMavenLog();
        command.setLog(mockLog);

        command.testStream = "myTestStream";
        command.bootstrapUrl = new URL("http://myBootstrapUrl");

        MavenProject project = new MavenProject();
        project.setPackaging("galasa-obr");
        command.project = project;

        // The old spelling is detected.
        command.skipDeployTestCatalog = true;
        command.skipDeployTestCatalogOldSpelling = true;

        command.execute();

        String expectedLogRecord = "INFO:Skipping Deploy Test Catalog - because the property galasa.skip.deploytestcatalog or galasa.skip.bundletestcatalog is set";
        mockLog.assertContainsRecord(expectedLogRecord);
    }

    @Test
    public void testSkipBundleCatalogNewSpellingStillSkipsDoingWork() throws Exception {
        DeployTestCatalog command = new DeployTestCatalog();
        MockMavenLog mockLog = new MockMavenLog();
        command.setLog(mockLog);

        command.testStream = "myTestStream";
        command.bootstrapUrl = new URL("http://myBootstrapUrl");

        MavenProject project = new MavenProject();
        project.setPackaging("galasa-obr");
        command.project = project;

        // The old spelling is detected.
        command.skipBundleTestCatalog = true;
        
        command.execute();

        String expectedLogRecord = "INFO:Skipping Deploy Test Catalog - because the property galasa.skip.deploytestcatalog or galasa.skip.bundletestcatalog is set";
        mockLog.assertContainsRecord(expectedLogRecord);
    }

    @Test
    public void testSkipBundleCatalogOldpellingStillSkipsDoingWork() throws Exception {
        DeployTestCatalog command = new DeployTestCatalog();
        MockMavenLog mockLog = new MockMavenLog();
        command.setLog(mockLog);

        command.testStream = "myTestStream";
        command.bootstrapUrl = new URL("http://myBootstrapUrl");

        MavenProject project = new MavenProject();
        project.setPackaging("galasa-obr");
        command.project = project;

        // The old spelling is detected.
        command.skipBundleTestCatalogOldSpelling = true;
        
        command.execute();

        String expectedLogRecord = "INFO:Skipping Deploy Test Catalog - because the property galasa.skip.deploytestcatalog or galasa.skip.bundletestcatalog is set";
        mockLog.assertContainsRecord(expectedLogRecord);
    }

    @Test
    public void testSkipsIfNoArtifactPresent() throws Exception {
        DeployTestCatalog command = new DeployTestCatalog();
        MockMavenLog mockLog = new MockMavenLog();
        command.setLog(mockLog);

        command.testStream = "myTestStream";
        command.bootstrapUrl = new URL("http://myBootstrapUrl");

        MavenProject project = new MavenProject();
        project.setPackaging("galasa-obr");
        command.project = project;

        command.execute();

        String expectedLogRecord = "WARN:Skipping Bundle Test Catalog deploy, no test catalog artifact present";
        mockLog.assertContainsRecord(expectedLogRecord);
    }

    @Test
    public void testSkipsWorkIfProjectNotAnOBR() throws Exception {
        DeployTestCatalog command = new DeployTestCatalog();
        MockMavenLog mockLog = new MockMavenLog();
        command.setLog(mockLog);

        command.testStream = "myTestStream";
        command.bootstrapUrl = new URL("http://myBootstrapUrl");

        MavenProject project = new MavenProject();
        project.setPackaging("not-a-galasa-obr");
        command.project = project;


        command.execute();
        
        String expectedLogRecord = "INFO:Skipping Bundle Test Catalog deploy, not a galasa-obr project";
        mockLog.assertContainsRecord(expectedLogRecord);
    }

    @Test
    public void testSkipsIfStreamNotSpecified() throws Exception {
        DeployTestCatalog command = new DeployTestCatalog();
        MockMavenLog mockLog = new MockMavenLog();
        command.setLog(mockLog);

        // command.testStream = "myTestStream";

        MavenProject project = new MavenProject();
        command.project = project;

        command.execute();

        String expectedLogRecord = "WARN:Skipping Deploy Test Catalog - test stream name is missing";
        mockLog.assertContainsRecord(expectedLogRecord);
    }

    @Test
    public void testSkipsIfBootstrapNotProvided() throws Exception {
        DeployTestCatalog command = new DeployTestCatalog();
        MockMavenLog mockLog = new MockMavenLog();
        command.setLog(mockLog);

        command.testStream = "myTestStream";
        // command.bootstrapUrl = new URL("http://myBootstrapUrl");

        MavenProject project = new MavenProject();
        command.project = project;

        command.execute();

        String expectedLogRecord = "WARN:Skipping Deploy Test Catalog - Bootstrap URL is missing";
        mockLog.assertContainsRecord(expectedLogRecord);
    }

    @Test
    public void testSkipsNonObrProjects() throws Exception {
        DeployTestCatalog command = new DeployTestCatalog();
        MockMavenLog mockLog = new MockMavenLog();
        command.setLog(mockLog);

        command.testStream = "myTestStream";
        command.bootstrapUrl = new URL("http://myBootstrapUrl");

        MavenProject project = new MavenProject();
        command.project = project;

        command.execute();

        String expectedLogRecord = "INFO:Skipping Bundle Test Catalog deploy, not a galasa-obr project";
        mockLog.assertContainsRecord(expectedLogRecord);
    }




    
 
    // This is my exploration unit test.
    //
    // The unit tests are not yet complete, as they don't test the last piece where the test catalog file is
    // sent off to the URL.
    // 
    // @SuppressWarnings("deprecation")
    // @Test
    // public void testXXX() throws Exception {
    //     DeployTestCatalog command = new DeployTestCatalog();
    //     MockMavenLog mockLog = new MockMavenLog();
    //     command.setLog(mockLog);

    //     command.testStream = "myTestStream";
    //     command.bootstrapUrl = new URL("http://myBootstrapUrl/bootstrap");

    //     MavenProject project = new MavenProject();
    //     project.setPackaging("galasa-obr");
    //     command.project = project;

    //     MockArtifact testCatalogArtifact = new MockArtifact();
    //     project.addAttachedArtifact(testCatalogArtifact);
    //     testCatalogArtifact.type = "json";
    //     testCatalogArtifact.classifier = "testcatalog";
        
    //     // Set a mock boostrap loader...
    //     command.bootstrapLoader = new BootstrapLoader() {
    //         @Override
    //         public Properties getBootstrapProperties(URL bootstrapUrl, Log log) throws MojoExecutionException {
    //             return new Properties();
    //         }
    //     };

    //     command.galasaAccessToken="my:token";

    //     command.authFactory = new AuthenticationServiceFactory() {
    //         @Override
    //         public AuthenticationService newAuthenticationService(URL apiServerUrl, String galasaAccessToken,
    //                 HttpClient httpClient) throws AuthenticationException {
    //             return new AuthenticationService() {

    //                 @Override
    //                 public String getJWT() throws AuthenticationException {
    //                    return "myJWT";
    //                 }
    //             };
    //         }
    //     };

    //     command.execute();

    //     String expectedLogRecord = "INFO:Skipping Deploy Test Catalog - because the property galasa.skip.deploytestcatalog or galasa.skip.bundletestcatalog is set";
    //     mockLog.assertContainsRecord(expectedLogRecord);
    // }
}
