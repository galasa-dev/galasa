/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.maven.plugin;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Store a git commit hash into the META-INF folder
 * 
 * @author Michael Baylis
 *
 */
@Mojo(name = "gitcommithash", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, threadSafe = true, requiresProject = true)
public class StoreGitCommitHash extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File         outputDirectory;

    @Parameter(defaultValue = "${env.GIT_COMMIT}", property = "gitCommitHash", required = false)
    private String       hash;

    public void execute() throws MojoExecutionException, MojoFailureException {

        if (!"bundle".equals(project.getPackaging()) && !"jar".equals(project.getPackaging())
                && !"eclipse-plugin".equals(project.getPackaging())) {
            return;
        }

        if (hash == null || hash.trim().isEmpty()) {
            hash = "unknown";
        }
        hash = hash.trim();

        File galasaFile = new File(outputDirectory, "galasa");
        File metaInf = new File(galasaFile, "META-INF");
        File hashFile = new File(metaInf, "git.hash");

        if (!metaInf.exists()) {
            metaInf.mkdirs();
        }

        try {
            FileUtils.write(hashFile, hash, "UTF-8");
            getLog().info("Written git hash " + hash + " to META-INF/git.hash");
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to write hash", e);
        }

        Resource resource = new Resource();
        resource.setDirectory(galasaFile.getAbsolutePath());
        project.addResource(resource);

    }

}
