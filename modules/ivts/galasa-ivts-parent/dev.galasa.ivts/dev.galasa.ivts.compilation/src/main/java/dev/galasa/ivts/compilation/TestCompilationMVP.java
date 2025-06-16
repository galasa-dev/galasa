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
public class TestCompilationMVP {

    private static final int DOCKER_COMMAND_TIMEOUT_MILLISECONDS = 60 * 1000;
    private static final String TEST_PROJECT_DIRECTORY = "/galasa/dev.galasa.example";

    @Logger
    public Log logger;

    @DockerContainer(image = "galasa-dev/compilation-mvp:main")
    public IDockerContainer compilationMvpContainer;

    /*
     * Runs and Gradle build against a test project using managers from 
     * the MVP build. Passes if "BUILD SUCCESSFUL" appears in the output.
     */
    @Test
    public void testProjectCanCompileUsingMvpBuild() throws Exception {
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
        IDockerExec gradleBuildCommandExec = compilationMvpContainer.exec(DOCKER_COMMAND_TIMEOUT_MILLISECONDS, buildCommandArray);

        gradleBuildCommandExec.waitForExec(DOCKER_COMMAND_TIMEOUT_MILLISECONDS);
        String gradleBuildResults = gradleBuildCommandExec.getCurrentOutput();

        // Then...
        assertThat(gradleBuildResults).contains("BUILD SUCCESSFUL");
        logger.info("OUTPUT FOR TEST: " + gradleBuildResults);
    }
}
