/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.gradle.testcatalog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskAction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TestCatalogBuildTask extends DefaultTask {

    private File testCatalog;
    private JsonObject jsonRoot;
    private JsonObject jsonMetadata;
    private JsonObject jsonClasses;
    private JsonObject jsonPackages;
    private JsonObject jsonBundles;
    private JsonObject jsonSharedEnv;
    private JsonObject jsonGherkin;

    private Gson gson;

    @TaskAction
    public void genobr() throws Exception {
        getLogger().debug("Building Testcatalog");

        this.gson = new GsonBuilder().setPrettyPrinting().create();

        //*** Set up the root object
        this.jsonRoot = new JsonObject();
        this.jsonMetadata = new JsonObject();
        this.jsonRoot.add("metadata", this.jsonMetadata);
        this.jsonClasses = new JsonObject();
        this.jsonRoot.add("classes", this.jsonClasses);
        this.jsonPackages = new JsonObject();
        this.jsonRoot.add("packages", this.jsonPackages);
        this.jsonBundles = new JsonObject();
        this.jsonRoot.add("bundles", this.jsonBundles);
        this.jsonSharedEnv = new JsonObject();
        this.jsonRoot.add("sharedEnvironments", this.jsonSharedEnv);
        this.jsonGherkin = new JsonObject();
        this.jsonRoot.add("gherkin", this.jsonGherkin);

        //*** Create Metadata
        Instant now = Instant.now();
        this.jsonMetadata.addProperty("generated", now.toString());
        this.jsonMetadata.addProperty("name", getProject().getName());
        
        //*** Create old properties
        this.jsonRoot.addProperty("name", getProject().getName());
        this.jsonRoot.addProperty("build", "gradle");
        this.jsonRoot.addProperty("version", getProject().getVersion().toString());
        this.jsonRoot.addProperty("built", now.toString());

        Configuration config = getProject().getConfigurations().getByName("bundle");
        for(ResolvedDependency dependency : config.getResolvedConfiguration().getFirstLevelModuleDependencies()) {
            processBundle(dependency);
        }

        String testCatalog = gson.toJson(jsonRoot);

        try (FileOutputStream os = new FileOutputStream(this.testCatalog)) {
            os.write(testCatalog.getBytes(StandardCharsets.UTF_8));
        }
    }



    private void processBundle(ResolvedDependency dependency) throws TestCatalogException {
        String id = dependency.getName();
        getLogger().debug("Processing bundle " + id);

        Iterator<ResolvedArtifact> artifactIterator = dependency.getAllModuleArtifacts().iterator();
        if (!artifactIterator.hasNext()) {
            getLogger().warn("No artifacts found for " + id);
            return;
        }

        ResolvedArtifact artifact = artifactIterator.next();

        File file = artifact.getFile();

        String fileName = file.getName();
        if (!fileName.endsWith(".jar")) {
            getLogger().warn("Bundle " + id + " does not end with .jar, ignoring");
            return;
        }

        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            ZipEntry entry = null;

            while((entry = zis.getNextEntry()) != null) {
                if ("META-INF/testcatalog.json".equals(entry.getName())) {
                    break;
                }
            }

            if (entry == null) {
                getLogger().warn("Bundle " + id + " does not include a META-INF/testcatalog.json, ignoring");
                return;
            }

            try (InputStreamReader reader = new InputStreamReader(zis)) {
                JsonObject testCatalogRoot = this.gson.fromJson(reader, JsonObject.class);
                
                // *** Append/replace all the test classes
                JsonObject subTestClasses = testCatalogRoot.getAsJsonObject("classes");
                if(subTestClasses != null) {
                    for (Entry<String, JsonElement> testClassEntry : subTestClasses.entrySet()) {
                        String name = testClassEntry.getKey();
                        JsonElement tc = testClassEntry.getValue();

                        jsonClasses.add(name, tc);
                    }
                }

                // *** Append to the packages
                JsonObject subPackages = testCatalogRoot.getAsJsonObject("packages");
                if(subPackages != null) {
                    for (Entry<String, JsonElement> packageEntry : subPackages.entrySet()) {
                        String name = packageEntry.getKey();
                        JsonArray list = (JsonArray) packageEntry.getValue();

                        JsonArray mergedPackage = jsonPackages.getAsJsonArray(name);
                        if (mergedPackage == null) {
                            mergedPackage = new JsonArray();
                            jsonPackages.add(name, mergedPackage);
                        }

                        for (int i = 0; i < list.size(); i++) {
                            String className = list.get(i).getAsString();
                            mergedPackage.add(className);
                        }
                    }
                }

                // *** Append/replace all the bundles
                JsonObject subBundles = testCatalogRoot.getAsJsonObject("bundles");
                if(subBundles != null) {
                    for (Entry<String, JsonElement> bundleEntry : subBundles.entrySet()) {
                        String name = bundleEntry.getKey();
                        JsonElement tc = bundleEntry.getValue();

                        jsonBundles.add(name, tc);
                    }
                }

                // *** Append/replace all the Shared Environments
                JsonObject subSenv = testCatalogRoot.getAsJsonObject("sharedEnvironments");
                if(subSenv != null) {
                    for (Entry<String, JsonElement> senvEntry : subSenv.entrySet()) {
                        String name = senvEntry.getKey();
                        JsonElement tc = senvEntry.getValue();

                        jsonSharedEnv.add(name, tc);
                    }
                }

                // *** Append/replace all the Gherkin
                JsonObject subGherkin = testCatalogRoot.getAsJsonObject("gherkin");
                if(subGherkin != null) {
                    for (Entry<String, JsonElement> gherkinEntry : subGherkin.entrySet()) {
                        String name = gherkinEntry.getKey();
                        JsonElement tc = gherkinEntry.getValue();

                        jsonGherkin.add(name, tc);
                    }
                }

            }

        } catch(Exception e) {
            throw new TestCatalogException("Failed to process bundle " + id, e);
        }
    }



    public void apply() {
        // Run during apply phase, need to decide on the output file name
        Provider<RegularFile> testCatalogFile = getProject().getLayout().getBuildDirectory().file("testcatalog.json");
        this.testCatalog = testCatalogFile.get().getAsFile();
        getOutputs().file(testCatalog);

        ConfigurationContainer configurations = getProject().getConfigurations();
        Configuration bundleConfiguration = configurations.findByName("bundle");

        getDependsOn().add(bundleConfiguration);
    }

}
