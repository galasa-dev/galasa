/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.gradle.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.tasks.Jar;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class TestCatalogBuildTask extends DefaultTask {

    private SourceSet sourceSet;
    private File testCatalog;

    @TaskAction
    public void buildTestCatalog() throws Exception {

        //*** Ensure the meta dir exists
        testCatalog.getParentFile().mkdirs();
        
        //*** Set up the root object
        JsonObject jsonRoot = new JsonObject();
        JsonObject jsonMetadata = new JsonObject();
        jsonRoot.add("metadata", jsonMetadata);
        JsonObject jsonClasses = new JsonObject();
        jsonRoot.add("classes", jsonClasses);
        JsonObject jsonPackages = new JsonObject();
        jsonRoot.add("packages", jsonPackages);
        JsonObject jsonBundles = new JsonObject();
        jsonRoot.add("bundles", jsonBundles);
        JsonObject jsonSharedEnv = new JsonObject();
        jsonRoot.add("sharedEnvironments", jsonSharedEnv);
        
        //*** Create Metadata
        jsonMetadata.addProperty("generated", Instant.now().toString());
        jsonMetadata.addProperty("name", getProject().getName());

        //*** Find the classpaths to use
        ArrayList<URL> classpathURLs = new ArrayList<URL>();
        
        for(File c : this.sourceSet.getOutput().getFiles()) {
            classpathURLs.add(c.toURI().toURL());
        }
        for(File c : this.sourceSet.getCompileClasspath().getFiles()) {
            classpathURLs.add(c.toURI().toURL());
        }
        URL[] classPaths = classpathURLs.toArray(new URL[classpathURLs.size()]);

        ClassLoader thisLoad = getClass().getClassLoader();
        ClassLoader load = new URLClassLoader(classpathURLs.toArray(classPaths), thisLoad);


        //*** Get all the annotations
        Class<?> annotationTest = ReflectionUtils.forName("dev.galasa.Test", load);
        Class<?> annotationSharedEnv = ReflectionUtils.forName("dev.galasa.SharedEnvironment", load);
        Class<?> annotationBuilder = ReflectionUtils.forName("dev.galasa.framework.spi.TestCatalogBuilder", load);
        Class<?> annotationBuilderInterface = ReflectionUtils
                .forName("dev.galasa.framework.spi.ITestCatalogBuilder", load);        


        // *** Set up reflections
        ConfigurationBuilder configuration = new ConfigurationBuilder();
        configuration.addClassLoaders(load);
        configuration.addUrls(classpathURLs);
        configuration.addScanners(new SubTypesScanner(), new TypeAnnotationsScanner());

        Reflections.log = null; // TODO need to hide a lot,  but need a better way of doing this.
        // TODO maybe create our own logger to write to debug.
        Reflections reflections = new Reflections(configuration);

        // *** Locate all the Test Catalog Builders on the classpath
        HashMap<Object, Method> catalogTestBuilders = new HashMap<>();
        HashMap<Object, Method> catalogSenvBuilders = new HashMap<>();
        @SuppressWarnings("unchecked")
        Set<Class<?>> testCatalogBuilderClasses = reflections
        .getTypesAnnotatedWith((Class<? extends Annotation>) annotationBuilder);
        for (Class<?> klass : testCatalogBuilderClasses) {
            // *** Have to do reflection here, becuase of the different classpaths
            if (annotationBuilderInterface.isAssignableFrom(klass)) {
                try {
                    Object instance = klass.getDeclaredConstructor().newInstance();
                    catalogTestBuilders.put(instance,
                            klass.getMethod("appendTestCatalog", JsonObject.class, JsonObject.class, Class.class));
                    catalogSenvBuilders.put(instance,
                            klass.getMethod("appendTestCatalogForSharedEnvironment", JsonObject.class, Class.class));
                    getLogger().debug("Found test catalog builder class " + klass.getName());
                } catch (Exception e) {
                    getLogger().warn("Ignoring test catalog builder class " + klass.getName(), e);
                }
            }
        }
        
        String bundleName = getProject().getName();

        // *** Locate all the test classes on the classpath
        @SuppressWarnings("unchecked")
        Set<Class<?>> sourceTestClasses = reflections
        .getTypesAnnotatedWith((Class<? extends Annotation>) annotationTest);

        // *** Create the JSON Template
        JsonObject jsonBundle = new JsonObject();
        jsonBundles.add(bundleName, jsonBundle);
        JsonObject jsonBundlePackages = new JsonObject();
        jsonBundle.add("packages", jsonBundlePackages);

        getLogger().info("Building the Test Catalog for this bundle:-");
        int testCount = 0;
        for (Class<?> sourceTestClass : sourceTestClasses) {
            testCount++;
            String fullName = bundleName + "/" + sourceTestClass.getName();
            String testClassName = sourceTestClass.getName();
            String packageName = null;
            if (sourceTestClass.getPackage() != null) {
                packageName = sourceTestClass.getPackage().getName();
            } else {
                packageName = "default";
            }

            getLogger().info("     " + testClassName);

            // *** Create the main test class descriptor
            JsonObject jsonTestClass = new JsonObject();
            jsonTestClass.addProperty("name", testClassName);
            jsonTestClass.addProperty("bundle", bundleName);
            jsonTestClass.addProperty("shortName", sourceTestClass.getSimpleName());
            jsonTestClass.addProperty("package", packageName);
            jsonClasses.add(fullName, jsonTestClass);

            // *** Add to the package list
            JsonArray jsonPackage = jsonPackages.getAsJsonArray(packageName);
            if (jsonPackage == null) {
                jsonPackage = new JsonArray();
                jsonPackages.add(packageName, jsonPackage);
            }
            jsonPackage.add(fullName);

            // *** Add to the bundle package list
            jsonPackage = jsonBundlePackages.getAsJsonArray(packageName);
            if (jsonPackage == null) {
                jsonPackage = new JsonArray();
                jsonBundlePackages.add(packageName, jsonPackage);
            }
            jsonPackage.add(fullName);

            // *** Call each Catalog Builder in turn to append data to the root and the
            // class
            for (Entry<Object, Method> builder : catalogTestBuilders.entrySet()) {
                builder.getValue().invoke(builder.getKey(), jsonRoot, jsonTestClass, sourceTestClass);
            }
        }

        //*** Build list of shared environments

        @SuppressWarnings("unchecked")
        Set<Class<?>> sourceSenvClasses = reflections
        .getTypesAnnotatedWith((Class<? extends Annotation>) annotationSharedEnv);
        int senvCount = 0;
        for (Class<?> sourceSenvClass : sourceSenvClasses) {
            senvCount++;
            String fullName = bundleName + "/" + sourceSenvClass.getName();
            String senvClassName = sourceSenvClass.getName();
            String packageName = null;
            if (sourceSenvClass.getPackage() != null) {
                packageName = sourceSenvClass.getPackage().getName();
            } else {
                packageName = "default";
            }
            getLogger().info("     " + senvClassName);

            // *** Create the main test class descriptor
            JsonObject jsonSenvClass = new JsonObject();
            jsonSenvClass.addProperty("name", senvClassName);
            jsonSenvClass.addProperty("bundle", bundleName);
            jsonSenvClass.addProperty("shortName", sourceSenvClass.getSimpleName());
            jsonSenvClass.addProperty("package", packageName);
            jsonSharedEnv.add(fullName, jsonSenvClass);

            // *** Call each Catalog Builder in turn to append data to the
            // class
            for (Entry<Object, Method> builder : catalogSenvBuilders.entrySet()) {
                builder.getValue().invoke(builder.getKey(), jsonSenvClass, sourceSenvClass);
            }
        }


        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String testCatalog = gson.toJson(jsonRoot);

        try (FileOutputStream os = new FileOutputStream(this.testCatalog)) {
            os.write(testCatalog.getBytes(StandardCharsets.UTF_8));
        }
        
        if (testCount == 0) {
            getLogger().info("Test catalog built with no test classes defined");
        } else if (testCount == 1) {
            getLogger().info("Test catalog built with 1 test class");
        } else {
            getLogger().info("Test catalog built with " + testCount + " test classes");
        }
        if (senvCount == 0) {
            getLogger().info("Test catalog built with no shared environments defined");
        } else if (senvCount == 1) {
            getLogger().info("Test catalog built with 1 shared environment");
        } else {
            getLogger().info("Test catalog built with " + testCount + " shared environments");
        }

    }

    public void apply() {
        SourceSetContainer sourceSets = getProject().getExtensions().getByType(SourceSetContainer.class);
        this.sourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

        //*** If any source file has changed, we must rerun this task
        getInputs().files(this.sourceSet.getAllSource().getFiles());

        //*** We are dependent on the generic Classes task
        Task classes = getProject().getTasks().getByName("classes");
        getDependsOn().add(classes);

        //*** Make sure the jar task is dependent on us
        Jar jarTask = (Jar) getProject().getTasks().getByName("jar");
        jarTask.getDependsOn().add(this);

        //*** Create the directories so Gradle does not moan
        Directory dirGenTestCatalog = getProject().getLayout().getBuildDirectory().dir("gentestcatalog").get();
        Directory dirGenTestCatalogMeta = dirGenTestCatalog.dir("META-INF");
        //*** dont need to create the file here, just indicate where it will be on the outputs
        this.testCatalog = dirGenTestCatalogMeta.file("testcatalog.json").getAsFile();

        //*** Tell the JAR task we want to it to include the meta-inf and json file
        jarTask.from(dirGenTestCatalog);

        //*** Mark the meta-inf file as output for caching purposes
        getOutputs().dir(dirGenTestCatalog);
    }

}
