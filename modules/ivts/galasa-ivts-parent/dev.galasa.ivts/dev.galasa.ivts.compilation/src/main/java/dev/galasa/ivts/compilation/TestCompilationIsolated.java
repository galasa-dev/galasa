/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ivts.compilation;

import static org.assertj.core.api.Assertions.*;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.docker.DockerContainer;
import dev.galasa.docker.IDockerContainer;
import dev.galasa.docker.IDockerExec;

@Test
public class TestCompilationIsolated {

    private static final int DOCKER_COMMAND_TIMEOUT_MILLISECONDS = 60 * 1000;
    private static final String TEST_PROJECT_DIRECTORY = "/galasa/dev.galasa.example";

    @Logger
    public Log logger;

    @DockerContainer(image = "galasa-dev/compilation-isolated:main")
    public IDockerContainer compilationIsolatedContainer;

    /*
     * Runs and Gradle build against a test project using managers from 
     * the Isolated build. Passes if "BUILD SUCCESSFUL" appears in the output.
     */
    @Test
    public void testProjectCanCompileUsingIsolatedBuild() throws Exception {
        // Given...
        logger.info("Compilation Test");
        logger.info("Running Gradle Build");

        String[] buildCommandArray = new String[]{
            "gradle",
            "--project-dir="+TEST_PROJECT_DIRECTORY,
            "--no-daemon",
            "--console=plain",
            "-PsourceMaven=/galasa/maven",
            "build"
        };

        // When...
        logger.info("Issuing Command: " + String.join(" ", buildCommandArray));
        IDockerExec gradleBuildCommandExec = compilationIsolatedContainer.exec(DOCKER_COMMAND_TIMEOUT_MILLISECONDS, buildCommandArray);

        gradleBuildCommandExec.waitForExec(DOCKER_COMMAND_TIMEOUT_MILLISECONDS);
        String gradleBuildResults = gradleBuildCommandExec.getCurrentOutput();

        // Then...
        assertThat(gradleBuildResults).contains("BUILD SUCCESSFUL");
        logger.info("OUTPUT FOR TEST: " + gradleBuildResults);
    }
}
