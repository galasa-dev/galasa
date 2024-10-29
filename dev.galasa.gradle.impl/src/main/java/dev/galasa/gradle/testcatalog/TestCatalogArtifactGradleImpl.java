/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.gradle.testcatalog;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import dev.galasa.plugin.common.ErrorRaiser;
import dev.galasa.plugin.common.TestCatalogArtifact;

public class TestCatalogArtifactGradleImpl implements TestCatalogArtifact<TestCatalogException> {

    private Path testCatalogArtifact;
    private ErrorRaiser<TestCatalogException> errorRaiser;

    public TestCatalogArtifactGradleImpl(Path testCatalogArtifact, ErrorRaiser<TestCatalogException> errorRaiser ) {
        this.errorRaiser = errorRaiser;
        this.testCatalogArtifact = testCatalogArtifact ;
    }

    @Override
    public void transferTo(OutputStream outputStream) throws TestCatalogException {
        try {
            Files.copy(testCatalogArtifact, outputStream);        
        } catch( IOException ex ) {
            errorRaiser.raiseError(ex,ex.toString());
        }
    }

}
