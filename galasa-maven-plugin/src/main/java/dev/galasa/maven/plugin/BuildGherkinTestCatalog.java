/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.maven.plugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

@Mojo(name = "gherkintestcat", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class BuildGherkinTestCatalog extends AbstractMojo {

    @Component
    private MavenProjectHelper projectHelper;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject       project;

    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File               outputDirectory;
    
    // This spelling of the property is old/wrong/deprecated.
    @Parameter(defaultValue = "${galasa.skip.gherkintestcatatlog}", readonly = true, required = false)
    private boolean            skipGherkinTestCatalogOldSpelling;
    
    @Parameter(defaultValue = "${galasa.skip.gherkintestcatalog}", readonly = true, required = false)
    private boolean            skipGherkinTestCatalog;
    
    private boolean skip = (skipGherkinTestCatalog || skipGherkinTestCatalogOldSpelling);

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (skip) {
                getLog().info("Skipping Gherkin Test Catalog build");
                return;
            }

            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }

            // *** Create the JSON Template
            JsonObject jsonRoot = new JsonObject();
            JsonObject jsonFeatures = new JsonObject();
            jsonRoot.add("gherkin", jsonFeatures);

            List<Path> featureFiles = new ArrayList<>();
            Files.list(project.getBasedir().toPath()).forEach(new ConsumeDirectory(featureFiles));

            for(Path feature : featureFiles) {
                String featureName = project.getBasedir().toPath().relativize(feature).toString();
                JsonObject featureJson = new JsonObject();
                featureJson.addProperty("name", featureName);
                String fileName = feature.getFileName().toString();
                featureJson.addProperty("shortName", fileName.substring(0, fileName.length() - 8));
                String maven = project.getGroupId() + "/" + project.getArtifactId() + "/" + project.getVersion();
                featureJson.addProperty("maven",  maven);

                jsonFeatures.add(featureName, featureJson);
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String testCatalog = gson.toJson(jsonRoot);

            File fileTestCatalog = new File(outputDirectory, "testcatalog.json");
            FileUtils.writeStringToFile(fileTestCatalog, testCatalog, "utf-8");

            projectHelper.attachArtifact(project, "json", "testcatalog", fileTestCatalog);
        } catch (Throwable t) {
            throw new MojoExecutionException("Problem processing the test catalog for the bundle", t);
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