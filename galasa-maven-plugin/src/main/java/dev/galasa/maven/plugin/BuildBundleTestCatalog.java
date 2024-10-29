/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.maven.plugin;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Manifest;

import org.apache.commons.io.FileUtils;
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
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Build a test catalog of all the tests within the bundle. The Test Class type
 * needs @Test to be included
 */
@Mojo(name = "bundletestcat", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class BuildBundleTestCatalog extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject       project;

    @Component
    private MavenProjectHelper projectHelper;

    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File               outputDirectory;

    @Parameter(defaultValue = "${project.compileClasspathElements}", readonly = true, required = true)
    private List<String>       classpathElements;

    // This spelling of the property is old/wrong/deprecated.
    @Parameter(defaultValue = "${galasa.skip.bundletestcatatlog}", readonly = true, required = false)
    private boolean            skipBundleTestCatalogOldSpelling;
    
    @Parameter(defaultValue = "${galasa.skip.bundletestcatalog}", readonly = true, required = false)
    private boolean            skipBundleTestCatalog;
    
    private boolean skip = (skipBundleTestCatalog || skipBundleTestCatalogOldSpelling);

    public void execute() throws MojoExecutionException, MojoFailureException {
        
        if (skip) {
            getLog().info("Skipping Bundle Test Catalog build");
            return;
        }

        if (!"bundle".equals(project.getPackaging())) {
            getLog().info("Skipping Bundle Test Catalog build, not a bundle project");
            return;
        }

        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        Path manifestPath = Paths.get(outputDirectory.toURI()).resolve("classes").resolve("META-INF")
                .resolve("MANIFEST.MF");
        if (!Files.exists(manifestPath)) {
            throw new MojoExecutionException(
                    "Unable to build Test Catalog as the META-INF/MANIFEST.MF file is missing");
        }

        try {
            Manifest manifest = new Manifest(Files.newInputStream(manifestPath));

            String bundleName = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
            if (bundleName == null || bundleName.trim().isEmpty()) {
                throw new MojoExecutionException(
                        "Unable to determine the Bundle-SymbolicName in the META-INF/MANIFEST.MF file");
            }

            // *** Calculate the classpath
            ArrayList<URL> classpathURLs = new ArrayList<URL>();
            getLog().debug("Classpath elements:-");
            for (String element : classpathElements) {
                File file = new File(element);
                classpathURLs.add(file.toURI().toURL());
                getLog().debug("  " + file.toURI().toURL());
            }

            ClassLoader thisLoad = getClass().getClassLoader();
            ClassLoader load = new URLClassLoader(classpathURLs.toArray(new URL[classpathURLs.size()]), thisLoad);

            Class<?> annotationTest = ReflectionUtils.forName("dev.galasa.Test", load);
            Class<?> annotationSharedEnv = ReflectionUtils.forName("dev.galasa.SharedEnvironment", load);
            Class<?> annotationBuilder = ReflectionUtils.forName("dev.galasa.framework.spi.TestCatalogBuilder", load);
            Class<?> annotationBuilderInterface = ReflectionUtils
                    .forName("dev.galasa.framework.spi.ITestCatalogBuilder", load);

            if (annotationTest == null || annotationSharedEnv == null || annotationBuilder == null || annotationBuilderInterface == null) {
                getLog().warn(
                        "Ignoring bundle for test catalog processing because the annotations are missing on the classpath");
                getLog().warn("dev.galasa.Test=" + annotationSharedEnv);
                getLog().warn("dev.galasa.SharedEnvironment=" + annotationTest);
                getLog().warn("dev.galasa.framework.spi.TestCatalogBuilder=" + annotationBuilder);
                getLog().warn("dev.galasa.framework.spi.ITestCatalogBuilder=" + annotationBuilderInterface);
                return;
            }

            // *** Set up reflections
            ConfigurationBuilder configuration = new ConfigurationBuilder();
            configuration.addClassLoaders(load);
            configuration.addUrls(classpathURLs);
            configuration.addScanners(new SubTypesScanner(), new TypeAnnotationsScanner());

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
                        Object instance = klass.newInstance();
                        catalogTestBuilders.put(instance,
                                klass.getMethod("appendTestCatalog", JsonObject.class, JsonObject.class, Class.class));
                        catalogSenvBuilders.put(instance,
                                klass.getMethod("appendTestCatalogForSharedEnvironment", JsonObject.class, Class.class));
                        getLog().debug("Found test catalog builder class " + klass.getName());
                    } catch (Exception e) {
                        getLog().warn("Ignoring test catalog builder class " + klass.getName(), e);
                    }
                }
            }

            // *** Locate all the test classes on the classpath
            @SuppressWarnings("unchecked")
            Set<Class<?>> sourceTestClasses = reflections
            .getTypesAnnotatedWith((Class<? extends Annotation>) annotationTest);

            // *** Create the JSON Template
            JsonObject jsonRoot = new JsonObject();
            JsonObject jsonClasses = new JsonObject();
            jsonRoot.add("classes", jsonClasses);
            JsonObject jsonPackages = new JsonObject();
            jsonRoot.add("packages", jsonPackages);
            JsonObject jsonBundles = new JsonObject();
            jsonRoot.add("bundles", jsonBundles);
            JsonObject jsonSharedEnv = new JsonObject();
            jsonRoot.add("sharedEnvironments", jsonSharedEnv);

            JsonObject jsonBundle = new JsonObject();
            jsonBundles.add(bundleName, jsonBundle);
            JsonObject jsonBundlePackages = new JsonObject();
            jsonBundle.add("packages", jsonBundlePackages);

            getLog().info("Building the Test Catalog for this bundle:-");
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

                getLog().info("     " + testClassName);

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
                getLog().info("     " + senvClassName);

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
            String testCatlog = gson.toJson(jsonRoot);

            File fileTestCatalog = new File(outputDirectory, "testcatalog.json");
            FileUtils.writeStringToFile(fileTestCatalog, testCatlog, "utf-8");

            projectHelper.attachArtifact(project, "json", "testcatalog", fileTestCatalog);

            if (testCount == 0) {
                getLog().info("Test catalog built with no test classes defined");
            } else if (testCount == 1) {
                getLog().info("Test catalog built with 1 test class");
            } else {
                getLog().info("Test catalog built with " + testCount + " test classes");
            }
            if (senvCount == 0) {
                getLog().info("Test catalog built with no shared environments defined");
            } else if (senvCount == 1) {
                getLog().info("Test catalog built with 1 shared environment");
            } else {
                getLog().info("Test catalog built with " + testCount + " shared environments");
            }
        } catch (Throwable t) {
            throw new MojoExecutionException("Problem processing the test catalog for the bundle", t);
        }

    }

}
