/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.maven.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

@Mojo(name = "gherkinzip", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class BuildGherkinZip extends AbstractMojo {

    @Component
    private MavenProjectHelper projectHelper;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject       project;

    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File               outputDirectory;

    @Parameter(defaultValue = "${galasa.skip.gherkinzip}", readonly = true, required = false)
    private boolean            skip;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (skip) {
                getLog().info("Skipping Gherkin Zip build");
                return;
            }

            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }

            List<Path> featureFiles = new ArrayList<>();
            Files.list(project.getBasedir().toPath()).forEach(new ConsumeDirectory(featureFiles));

            Path zipFile = Paths.get(outputDirectory.getPath(), project.getArtifactId() + "-" + project.getVersion() + ".zip");

            FileOutputStream fos = new FileOutputStream(zipFile.toString());
            ZipOutputStream zos = new ZipOutputStream(fos);
            for(Path feature : featureFiles){
                getLog().info("Zipping " + feature);
                ZipEntry ze = new ZipEntry(project.getBasedir().toPath().relativize(feature).toString());
                zos.putNextEntry(ze);
                FileInputStream fis = new FileInputStream(feature.toString());
                IOUtils.copy(fis, zos);
                zos.closeEntry();
                fis.close();
            }
            zos.close();
            fos.close();

            projectHelper.attachArtifact(project, "zip", "gherkin", zipFile.toFile());

        } catch (Throwable t) {
            throw new MojoExecutionException("Problem processing creating zip for gherkin", t);
        }
    }

    private static class ConsumeDirectory implements Consumer<Path> {

        private final List<Path> files;

        public ConsumeDirectory(List<Path> files) {
            this.files = files;
        }

        @Override
        public void accept(Path path) {
            try {
                if(Files.isDirectory(path)) {
                    Files.list(path).forEach(new ConsumeDirectory(files));
                } else {
                    if(path.toFile().getName().endsWith(".feature")) {
                        files.add(path);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}