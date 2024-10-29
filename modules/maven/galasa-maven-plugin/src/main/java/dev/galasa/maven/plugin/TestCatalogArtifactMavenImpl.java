/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.maven.plugin;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

import dev.galasa.plugin.common.ErrorRaiser;
import dev.galasa.plugin.common.TestCatalogArtifact;

public class TestCatalogArtifactMavenImpl implements TestCatalogArtifact<MojoExecutionException> {

    private Artifact testCatalogArtifact;
    private ErrorRaiser<MojoExecutionException> errorRaiser;

    public TestCatalogArtifactMavenImpl(Artifact testCatalogArtifact, ErrorRaiser<MojoExecutionException> errorRaiser ) {
        this.errorRaiser = errorRaiser;
        this.testCatalogArtifact = testCatalogArtifact ;
    }

    @Override
    public void transferTo(OutputStream outputStream) throws MojoExecutionException {
        try {
            FileUtils.copyFile(this.testCatalogArtifact.getFile(), outputStream);
        } catch( IOException ex ) {
            errorRaiser.raiseError(ex,ex.toString());
        }
    }

}
