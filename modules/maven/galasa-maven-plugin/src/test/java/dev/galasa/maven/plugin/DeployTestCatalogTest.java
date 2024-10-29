/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.maven.plugin;

import java.net.URL;
import org.junit.Test;
import org.apache.maven.project.MavenProject;

public class DeployTestCatalogTest { 

    @Test
    public void TestCanCreateDeployTestCatalog() {
        new DeployTestCatalog();
    }

    @Test
    public void TestSkipCatalogDeployOldSpellingStillSkipsDoingWork() throws Exception {
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
    public void TestSkipCatalogDeployNewSpellingStillSkipsDoingWork() throws Exception {
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
    public void TestSkipCatalogDeployNewAndOldSpellingStillSkipsDoingWork() throws Exception {
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
    public void TestSkipBundleCatalogNewSpellingStillSkipsDoingWork() throws Exception {
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
    public void TestSkipBundleCatalogOldpellingStillSkipsDoingWork() throws Exception {
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
    public void TestSkipsIfNoArtifactPresent() throws Exception {
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
    public void TestSkipsWorkIfProjectNotAnOBR() throws Exception {
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
    public void TestSkipsIfStreamNotSpecified() throws Exception {
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
    public void TestSkipsIfBootstrapNotProvided() throws Exception {
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
    public void TestSkipsNonObrProjects() throws Exception {
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
    // public void TestXXX() throws Exception {
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
