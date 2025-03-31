/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.maven.repository.internal;

import static org.assertj.core.api.Assertions.*;

import java.net.URL;
import org.junit.Test;

public class GalasaMavenUrlHandlerServiceTest {

    
    @Test
    public void TestCanCreateHandlerServiceObject() {
        new GalasaMavenUrlHandlerService();
    }

    @Test 
    public void TestCanBuildAnArtifactURLWithoutTrailingSlashOnOBR() throws Exception {
        GalasaMavenUrlHandlerService service = new GalasaMavenUrlHandlerService();

        URL repositoryUrl = new URL("http://myhost/myrepository");

        URL url = service.buildArtifactUrl(repositoryUrl, "myGroupId", "myArtifactId", "0.myVersion.0", "myFileName");

        assertThat(url.toString()).doesNotContain("//myGroupId");
    }

    @Test 
    public void TestCanBuildAnArtifactURLWithTrailingSlashOnOBR() throws Exception {
        GalasaMavenUrlHandlerService service = new GalasaMavenUrlHandlerService();

        URL repositoryUrl = new URL("http://myhost/myrepository/");

        URL url = service.buildArtifactUrl(repositoryUrl, "myGroupId", "myArtifactId", "0.myVersion.0", "myFileName");

        assertThat(url.toString()).doesNotContain("//myGroupId");
    }
}
