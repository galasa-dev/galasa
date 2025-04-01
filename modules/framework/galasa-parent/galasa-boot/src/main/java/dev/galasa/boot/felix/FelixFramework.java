/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot.felix;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.text.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.felix.bundlerepository.Capability;
import org.apache.felix.bundlerepository.Reason;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Requirement;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;
import org.apache.felix.framework.FrameworkFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;

import dev.galasa.boot.BootLogger;
import dev.galasa.boot.LauncherException;
import dev.galasa.boot.ResourceManagementConfiguration;

/**
 * Felix framework run test class
 */
public class FelixFramework {

    private static final String EXTRA_FRAMEWORK_BUNDLES_PROP  = "framework.extra.bundles";
    private static final String EXTRA_API_SERVER_BUNDLES_PROP = "api.extra.bundles";

    protected Framework framework;

    protected RepositoryAdmin repositoryAdmin;

    private BootLogger logger = new BootLogger();

    private boolean loadConsole = false;
    
    private String bootJarLoacation = null;

    private Bundle obrBundle;
        
    private File felixCache;

    /**
     * Initialise and start the Felix framework. Install required bundles and the
     * OBRs. Install the Galasa framework bundle
     * 
     * @param bundleRepositories the supplied OBRs
     * @param boostrapProperties
     * @param localMavenRepo
     * @param remoteMavenRepos
     * @throws LauncherException      if there is a problem initialising the
     *                                framework
     * @throws ClassNotFoundException
     */
    public void buildFramework(List<String> bundleRepositories, Properties boostrapProperties, URL localMavenRepo,
            List<URL> remoteMavenRepos, String galasaHome) throws LauncherException, ClassNotFoundException {
        logger.debug("Building Felix Framework...");

        File galasaDirectory = new File(galasaHome);
        String cacheDirectory = "felix-cache";
        
        this.felixCache = new File(galasaDirectory, cacheDirectory);
        try {
            FileUtils.deleteDirectory(felixCache);
            
            this.felixCache.mkdirs();

            FrameworkFactory frameworkFactory = new FrameworkFactory();

            HashMap<String, Object> frameworkProperties = new HashMap<>();
            // frameworkProperties.put("felix.log.level", "4");
            // frameworkProperties.put("ds.showtrace", "true");
            frameworkProperties.put(Constants.FRAMEWORK_STORAGE, felixCache.getAbsolutePath());
            frameworkProperties.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
            frameworkProperties.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
                    "org.apache.felix.bundlerepository; version=2.1, dev.galasa.framework, sun.misc, com.sun.net.httpserver, com.sun.management, org.xml.sax, sun.nio.ch");
            framework = frameworkFactory.newFramework(frameworkProperties);
            logger.debug("Initializing Felix Framework");
            framework.init();
            logger.debug("Starting Felix Framework");
            framework.start();
            logger.debug("Felix Framework started");

            logger.debug("Installing required OSGi bundles");
            // Only required when running the Launcher class outside of the galasa-boot.jar, e.g. from Eclipse
            bootJarLoacation = boostrapProperties.getProperty("dev.galasa.boot.jar.location");
            // *** Load dependencies for the maven repo url handler
            installBundle("org.apache.felix.scr.jar", true);
            installBundle("dev.galasa.framework.log4j2.bridge.jar", false);
            installBundle("log4j-api.jar", true);
            installBundle("log4j-core.jar", true);

            installBundle("dev.galasa.framework.maven.repository.spi.jar", true);
            installBundle("dev.galasa.framework.maven.repository.jar", true);
            loadMavenRepositories(localMavenRepo, remoteMavenRepos);

            // Install and start the Felix OBR bundle
            obrBundle = installBundle("org.apache.felix.bundlerepository.jar", true);

            // Load the OSGi Bundle Repositories
            loadBundleRepositories(bundleRepositories);

            // Install and start the Felix OSGi console if required
            loadConsole = Boolean.parseBoolean(boostrapProperties.getProperty("dev.galasa.core.load.console", "false"));
            if (loadConsole) {
                loadBundle("org.apache.felix.gogo.runtime");
                loadBundle("org.apache.felix.gogo.command");
                loadBundle("org.apache.felix.gogo.shell");
            }

            // Load the galasa-framework bundle
            logger.debug("installing Framework bundle");
            loadBundle("dev.galasa.framework");

            // Load extra bundles from the bootstrap
            String extraBundles = boostrapProperties.getProperty(EXTRA_FRAMEWORK_BUNDLES_PROP);
            if (extraBundles != null) {
                loadBundlesList(extraBundles);
            }
        } catch (IOException | BundleException e) {
            throw new LauncherException("Unable to initialise the Felix framework", e);
        }
    }

    private void loadExtraApiBundles(Properties boostrapProperties) throws LauncherException {
        String extraApiBundles = boostrapProperties.getProperty(EXTRA_API_SERVER_BUNDLES_PROP);
        if (extraApiBundles != null) {
            loadBundlesList(extraApiBundles);
        }
    }

    private void loadBundlesList(String extraBundles) throws LauncherException {
        String[] bundlesList = extraBundles.split(",");
        for (String extraBundle : bundlesList) {
            extraBundle = extraBundle.trim();
            if (!extraBundle.isEmpty()) {
                loadBundle(extraBundle);
            }
        }
    }

    private void loadMavenRepositories(URL localMavenRepo, List<URL> remoteMavenRepos) throws LauncherException {

        // Get the framework bundle
        Bundle frameWorkBundle = getBundle("dev.galasa.framework.maven.repository");

        // Get the dev.galasa.framework.TestRunner class service
        String classString = "dev.galasa.framework.maven.repository.spi.IMavenRepository";
        ServiceReference<?>[] serviceReferences;
        try {
            serviceReferences = frameWorkBundle.getBundleContext().getServiceReferences(classString, null);
        } catch (InvalidSyntaxException e) {
            throw new LauncherException("Unable to get framework service reference", e);
        }
        if (serviceReferences == null || serviceReferences.length != 1) {
            throw new LauncherException("Unable to get single reference to GalasaMavenRepository service: "
                    + ((serviceReferences == null) ? 0 : serviceReferences.length) + " service(s) returned");
        }
        Object service = frameWorkBundle.getBundleContext().getService(serviceReferences[0]);
        if (service == null) {
            throw new LauncherException("Unable to get GalasaMavenRepository service");
        }

        // Get the GalasaMavenRepositoryr#setRepositories() method
        Method runTestMethod;
        try {
            runTestMethod = service.getClass().getMethod("setRepositories", URL.class, List.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new LauncherException("Unable to get Framework Maven Repository method", e);
        }

        // Invoke the setRepositories method
        try {
            runTestMethod.invoke(service, localMavenRepo, remoteMavenRepos);
        } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            throw new LauncherException(e.getCause());
        }
    }

    /**
     * Run the supplied test
     * 
     * @param boostrapProperties  the bootstrap properties
     * @param overridesProperties the override properties
     * @throws LauncherException
     */
    public void runTest(Properties boostrapProperties, Properties overridesProperties) throws LauncherException {

        // Get the framework bundle
        Bundle frameWorkBundle = getBundle("dev.galasa.framework");

        // Get the dev.galasa.framework.TestRunner class service
        String classString = "dev.galasa.framework.TestRunner";
        if (overridesProperties.containsKey("framework.run.gherkintest")) {
            classString = "dev.galasa.framework.GherkinTestRunner";
        }
        String filterString = "(" + Constants.OBJECTCLASS + "=" + classString + ")";
        ServiceReference<?>[] serviceReferences;
        try {
            serviceReferences = frameWorkBundle.getBundleContext().getServiceReferences(classString, filterString);
        } catch (InvalidSyntaxException e) {
            throw new LauncherException("Unable to get framework service reference", e);
        }
        if (serviceReferences == null || serviceReferences.length != 1) {
            throw new LauncherException("Unable to get single reference to TestRunner service: "
                    + ((serviceReferences == null) ? 0 : serviceReferences.length) + " service(s) returned");
        }
        Object service = frameWorkBundle.getBundleContext().getService(serviceReferences[0]);
        if (service == null) {
            throw new LauncherException("Unable to get TestRunner service");
        }

        // Get the dev.galasa.framework.TestRunner#runTest(String testBundleName, String
        // testClassName) method
        Method runTestMethod;
        try {
            runTestMethod = service.getClass().getMethod("runTest", Properties.class, Properties.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new LauncherException("Unable to get Framework test runner method", e);
        }

        // Invoke the runTest method
        logger.debug("Invoking runTest()");
        try {
            runTestMethod.invoke(service, boostrapProperties, overridesProperties);
        } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            throw new LauncherException(e.getCause());
        }
    }

    /**
     * Run the Resource Management Server
     * 
     * @param boostrapProperties  the bootstrap properties
     * @param overridesProperties the override properties
     * @param bundles
     * @param health
     * @param metrics
     * @throws LauncherException
     */
    public void runResourceManagement(Properties boostrapProperties, Properties overridesProperties,
            List<String> bundles, Integer metrics, Integer health, ResourceManagementConfiguration resourceManagementConfig) throws LauncherException {

        // Get the framework bundle
        Bundle frameWorkBundle = getBundle("dev.galasa.framework");

        if (!bundles.isEmpty()) {
            // *** Load extra bundles
            for (String bundle : bundles) {
                try {
                    loadBundle(bundle);
                } catch (Exception e) {
                    throw new LauncherException("Failed to load extra bundle " + bundle, e);
                }
            }
        }

        // Load the resource management bundle
        loadBundle("dev.galasa.framework.resource.management");

        // *** Set up ports if present
        if (metrics != null) {
            overridesProperties.put("framework.resource.management.metrics.port", metrics.toString());
        }
        if (health != null) {
            overridesProperties.put("framework.resource.management.health.port", health.toString());
        }

        // Get the dev.galasa.framework.TestRunner class service
        String classString = "dev.galasa.framework.resource.management.internal.ResourceManagement";
        String filterString = "(" + Constants.OBJECTCLASS + "=" + classString + ")";
        ServiceReference<?>[] serviceReferences;
        try {
            serviceReferences = frameWorkBundle.getBundleContext().getServiceReferences(classString, filterString);
        } catch (InvalidSyntaxException e) {
            throw new LauncherException("Unable to get framework service reference", e);
        }
        if (serviceReferences == null || serviceReferences.length != 1) {
            throw new LauncherException("Unable to get single reference to ResourceManagement service: "
                    + ((serviceReferences == null) ? 0 : serviceReferences.length) + " service(s) returned");
        }
        Object service = frameWorkBundle.getBundleContext().getService(serviceReferences[0]);
        if (service == null) {
            throw new LauncherException("Unable to get ResourceManagement service");
        }

        // Get the dev.galasa.framework.resource.management.internal.ResourceManagement#run(...) method
        Method runMethod;
        try {
            runMethod = service.getClass().getMethod(
                "run",
                Properties.class,
                Properties.class,
                String.class,
                List.class,
                List.class
            );
        } catch (NoSuchMethodException | SecurityException e) {
            throw new LauncherException("Unable to get Framework resource management run method", e);
        }

        // Invoke the run method
        logger.debug("Invoking resource management run()");
        try {
            runMethod.invoke(
                service,
                boostrapProperties,
                overridesProperties,
                resourceManagementConfig.getStream(),
                resourceManagementConfig.getIncludesGlobPatterns(),
                resourceManagementConfig.getExcludesGlobPatterns()
            );
        } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            throw new LauncherException(e.getCause());
        }

    }

    /**
     * Run the Metrics Server Server
     * 
     * @param boostrapProperties  the bootstrap properties
     * @param overridesProperties the override properties
     * @param bundles
     * @param health
     * @param metrics
     * @throws LauncherException
     */
    public void runMetricsServer(Properties boostrapProperties, Properties overridesProperties, List<String> bundles,
            Integer metrics, Integer health) throws LauncherException {

        // Get the framework bundle
        Bundle frameWorkBundle = getBundle("dev.galasa.framework");

        if (!bundles.isEmpty()) {
            // *** Load extra bundles
            for (String bundle : bundles) {
                try {
                    loadBundle(bundle);
                } catch (Exception e) {
                    throw new LauncherException("Failed to load extra bundle " + bundle, e);
                }
            }
        } else {
            // *** Load all bundles that have IResourceManagementProvider service
            HashSet<String> bundlesToLoad = new HashSet<>();

            for (Repository repository : repositoryAdmin.listRepositories()) {
                if (repository.getResources() != null) {
                    resourceSearch: for (Resource resource : repository.getResources()) {
                        if (resource.getCapabilities() != null) {
                            for (Capability capability : resource.getCapabilities()) {
                                if ("service".equals(capability.getName())) {
                                    Map<String, Object> properties = capability.getPropertiesAsMap();
                                    String services = (String) properties.get("objectClass");
                                    if (services == null) {
                                        services = (String) properties.get("objectClass:List<String>");
                                    }
                                    if (services != null) {
                                        String[] split = services.split(",");

                                        for (String service : split) {
                                            if ("dev.galasa.framework.spi.IMetricsProvider".equals(service)) {
                                                bundlesToLoad.add(resource.getSymbolicName());
                                                continue resourceSearch;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for (String bundle : bundlesToLoad) {
                if (!isBundleActive(bundle)) {
                    loadBundle(bundle);
                }
            }
        }

        // *** Set up ports if present
        if (metrics != null) {
            overridesProperties.put("framework.metrics.port", metrics.toString());
        }
        if (health != null) {
            overridesProperties.put("framework.metrics.health.port", health.toString());
        }

        // Get the dev.galasa.framework.TestRunner class service
        String classString = "dev.galasa.framework.metrics.MetricsServer";
        String filterString = "(" + Constants.OBJECTCLASS + "=" + classString + ")";
        ServiceReference<?>[] serviceReferences;
        try {
            serviceReferences = frameWorkBundle.getBundleContext().getServiceReferences(classString, filterString);
        } catch (InvalidSyntaxException e) {
            throw new LauncherException("Unable to get framework service reference", e);
        }
        if (serviceReferences == null || serviceReferences.length != 1) {
            throw new LauncherException("Unable to get single reference to MetricsServer service: "
                    + ((serviceReferences == null) ? 0 : serviceReferences.length) + " service(s) returned");
        }
        Object service = frameWorkBundle.getBundleContext().getService(serviceReferences[0]);
        if (service == null) {
            throw new LauncherException("Unable to get MetricsServer service");
        }

        // Get the dev.galasa.framework.TestRunner#runTest(String testBundleName, String
        // testClassName) method
        Method runTestMethod;
        try {
            runTestMethod = service.getClass().getMethod("run", Properties.class, Properties.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new LauncherException("Unable to get Framework MetricsServer run method", e);
        }

        // Invoke the runTest method
        logger.debug("Invoking MetricsServer run()");
        try {
            runTestMethod.invoke(service, boostrapProperties, overridesProperties);
        } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            throw new LauncherException(e.getCause());
        }

    }

    
    /**
     * Setup the Ecosystem
     * 
     * @param boostrapProperties  the bootstrap properties
     * @param overridesProperties the override properties
     * @throws LauncherException
     */
    public void runSetupEcosystem(Properties boostrapProperties, Properties overridesProperties) throws LauncherException {

        // Get the framework bundle
        Bundle frameWorkBundle = getBundle("dev.galasa.framework");

        // Get the dev.galasa.framework.SetupEcosystem class service
        String classString = "dev.galasa.framework.SetupEcosystem";
        String filterString = "(" + Constants.OBJECTCLASS + "=" + classString + ")";

        ServiceReference<?>[] serviceReferences;
        try {
            serviceReferences = frameWorkBundle.getBundleContext().getServiceReferences(classString, filterString);
        } catch (InvalidSyntaxException e) {
            throw new LauncherException("Unable to get framework service reference", e);
        }
        if (serviceReferences == null || serviceReferences.length != 1) {
            throw new LauncherException("Unable to get single reference to SetupEcosystem service: "
                    + ((serviceReferences == null) ? 0 : serviceReferences.length) + " service(s) returned");
        }

        Object service = frameWorkBundle.getBundleContext().getService(serviceReferences[0]);
        if (service == null) {
            throw new LauncherException("Unable to get SetupEcosystem service");
        }

        // Get the dev.galasa.framework.SetupEcosystem#setup() method
        Method runSetupMethod;
        try {
            runSetupMethod = service.getClass().getMethod("setup", Properties.class, Properties.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new LauncherException("Unable to get Framework SetupEcosystem setup method", e);
        }

        // Invoke the setup method
        logger.debug("Invoking SetupEcosystem setup()");
        try {
            runSetupMethod.invoke(service, boostrapProperties, overridesProperties);
        } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            throw new LauncherException(e.getCause());
        }

    }

    /**
     * Validate the Ecosystem
     * 
     * @param boostrapProperties  the bootstrap properties
     * @param overridesProperties the override properties
     * @throws LauncherException
     */
    public void runValidateEcosystem(Properties boostrapProperties, Properties overridesProperties) throws LauncherException {

        // Get the framework bundle
        Bundle frameWorkBundle = getBundle("dev.galasa.framework");

        // Get the dev.galasa.framework.ValidateEcosystem class service
        String classString = "dev.galasa.framework.ValidateEcosystem";
        String filterString = "(" + Constants.OBJECTCLASS + "=" + classString + ")";

        ServiceReference<?>[] serviceReferences;
        try {
            serviceReferences = frameWorkBundle.getBundleContext().getServiceReferences(classString, filterString);
        } catch (InvalidSyntaxException e) {
            throw new LauncherException("Unable to get framework service reference", e);
        }
        if (serviceReferences == null || serviceReferences.length != 1) {
            throw new LauncherException("Unable to get single reference to SetupEcosystem service: "
                    + ((serviceReferences == null) ? 0 : serviceReferences.length) + " service(s) returned");
        }

        Object service = frameWorkBundle.getBundleContext().getService(serviceReferences[0]);
        if (service == null) {
            throw new LauncherException("Unable to get SetupEcosystem service");
        }

        // Get the dev.galasa.framework.SetupEcosystem#setup() method
        Method runSetupMethod;
        try {
            runSetupMethod = service.getClass().getMethod("setup", Properties.class, Properties.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new LauncherException("Unable to get Framework SetupEcosystem setup method", e);
        }

        // Invoke the setup method
        logger.debug("Invoking SetupEcosystem setup()");
        try {
            runSetupMethod.invoke(service, boostrapProperties, overridesProperties);
        } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            throw new LauncherException(e.getCause());
        }

    }

    public void runWebApiServer(Properties boostrapProperties, Properties overridesProperties, List<String> bundles,
            Integer metrics, Integer health) throws LauncherException {

        // Get the Jetty server running
        loadBundle("org.apache.felix.http.servlet-api");
        loadBundle("org.apache.felix.http.jetty");
        loadBundle("org.apache.felix.fileinstall");

        // Get the framework bundle
        Bundle frameWorkBundle = getBundle("dev.galasa.framework");
        // Get the framework bundle
        loadBundle("dev.galasa.framework.api");

        // Load any extra bundles that should only be available when running the API server
        loadExtraApiBundles(boostrapProperties);

        // Get the dev.galasa.framework.api.internal.ApiStartup class service
        String classString = "dev.galasa.framework.api.internal.ApiStartup";
        String filterString = "(" + Constants.OBJECTCLASS + "=" + classString + ")";
        ServiceReference<?>[] serviceReferences;
        try {
            serviceReferences = frameWorkBundle.getBundleContext().getServiceReferences(classString, filterString);
        } catch (InvalidSyntaxException e) {
            throw new LauncherException("Unable to get framework service reference", e);
        }
        if (serviceReferences == null || serviceReferences.length != 1) {
            throw new LauncherException("Unable to get single reference to ApiStartup service: "
                    + ((serviceReferences == null) ? 0 : serviceReferences.length) + " service(s) returned");
        }
        Object service = frameWorkBundle.getBundleContext().getService(serviceReferences[0]);
        if (service == null) {
            throw new LauncherException("Unable to get ApiStartup service");
        }

        // ApiStartup.run() method
        Method runTestMethod;
        try {
            runTestMethod = service.getClass().getMethod("run", Properties.class, Properties.class, List.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new LauncherException("Unable to get Framework ApiStartup run method", e);
        }

        // Invoke the runTest method
        logger.debug("Invoking ApiStartup run()");
        try {
            runTestMethod.invoke(service, boostrapProperties, overridesProperties, bundles);
        } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            throw new LauncherException(e.getCause());
        }
    }

    /**
     * Run the Kubernetes Controller Server
     * 
     * @param boostrapProperties  the bootstrap properties
     * @param overridesProperties the override properties
     * @param bundles
     * @param health
     * @param metrics
     * @throws LauncherException
     */
    public void runK8sController(Properties boostrapProperties, Properties overridesProperties, List<String> bundles,
            Integer metrics, Integer health) throws LauncherException {

        // Get the framework bundle
        Bundle frameWorkBundle = getBundle("dev.galasa.framework");
        loadBundle("dev.galasa.framework.k8s.controller");

        // *** Set up ports if present
        if (metrics != null) {
            overridesProperties.put("framework.controller.metrics.port", metrics.toString());
        }
        if (health != null) {
            overridesProperties.put("framework.controller.health.port", health.toString());
        }

        // Get the dev.galasa.framework.TestRunner class service
        String classString = "dev.galasa.framework.k8s.controller.K8sController";
        String filterString = "(" + Constants.OBJECTCLASS + "=" + classString + ")";
        ServiceReference<?>[] serviceReferences;
        try {
            serviceReferences = frameWorkBundle.getBundleContext().getServiceReferences(classString, filterString);
        } catch (InvalidSyntaxException e) {
            throw new LauncherException("Unable to get framework service reference", e);
        }
        if (serviceReferences == null || serviceReferences.length != 1) {
            throw new LauncherException("Unable to get single reference to K8sController service: "
                    + ((serviceReferences == null) ? 0 : serviceReferences.length) + " service(s) returned");
        }
        Object service = frameWorkBundle.getBundleContext().getService(serviceReferences[0]);
        if (service == null) {
            throw new LauncherException("Unable to get K8sController service");
        }

        // Get the run method
        Method runTestMethod;
        try {
            runTestMethod = service.getClass().getMethod("run", Properties.class, Properties.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new LauncherException("Unable to get K8sController run method", e);
        }

        // Invoke the runTest method
        logger.debug("Invoking k8s controller run()");
        try {
            runTestMethod.invoke(service, boostrapProperties, overridesProperties);
        } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            throw new LauncherException(e.getCause());
        }

    }

    /**
     * Run the Docker Controller Server
     * 
     * @param boostrapProperties  the bootstrap properties
     * @param overridesProperties the override properties
     * @param bundles
     * @param health
     * @param metrics
     * @throws LauncherException
     */
    public void runDockerController(Properties boostrapProperties, Properties overridesProperties, List<String> bundles,
            Integer metrics, Integer health) throws LauncherException {

        // Get the framework bundle
        Bundle frameWorkBundle = getBundle("dev.galasa.framework");
        loadBundle("dev.galasa.framework.docker.controller");

        // *** Set up ports if present
        if (metrics != null) {
            overridesProperties.put("framework.controller.metrics.port", metrics.toString());
        }
        if (health != null) {
            overridesProperties.put("framework.controller.health.port", health.toString());
        }

        // Get the dev.galasa.framework.TestRunner class service
        String classString = "dev.galasa.framework.docker.controller.DockerController";
        String filterString = "(" + Constants.OBJECTCLASS + "=" + classString + ")";
        ServiceReference<?>[] serviceReferences;
        try {
            serviceReferences = frameWorkBundle.getBundleContext().getServiceReferences(classString, filterString);
        } catch (InvalidSyntaxException e) {
            throw new LauncherException("Unable to get framework service reference", e);
        }
        if (serviceReferences == null || serviceReferences.length != 1) {
            throw new LauncherException("Unable to get single reference to DockerController service: "
                    + ((serviceReferences == null) ? 0 : serviceReferences.length) + " service(s) returned");
        }
        Object service = frameWorkBundle.getBundleContext().getService(serviceReferences[0]);
        if (service == null) {
            throw new LauncherException("Unable to get DockerController service");
        }

        // Get the run method
        Method runTestMethod;
        try {
            runTestMethod = service.getClass().getMethod("run", Properties.class, Properties.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new LauncherException("Unable to get DockerController run method", e);
        }

        // Invoke the runTest method
        logger.debug("Invoking Docker controller run()");
        try {
            runTestMethod.invoke(service, boostrapProperties, overridesProperties);
        } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            throw new LauncherException(e.getCause());
        }

    }

    /**
     * Stop the Felix framework
     * 
     * @throws LauncherException
     * @throws InterruptedException
     */
    public void stopFramework() throws LauncherException, InterruptedException {
        logger.debug("Stopping Felix framework");
        if (framework == null) {
            return;
        }
        try {
            framework.stop();
        } catch (BundleException e) {
            throw new LauncherException("Unable to stop the Felix framework", e);
        }

        framework.waitForStop(30000);
        
        logger.debug("Felix framework stopped");
        
        try {
            FileUtils.deleteDirectory(felixCache);
        } catch (IOException e) {
        }
    }

    /**
     * Load the supplied OBRs
     * 
     * @param bundleRepositories
     * @throws LauncherException
     */
    private void loadBundleRepositories(List<String> bundleRepositories) throws LauncherException {
        // Get the OBR RepositoryAdmin service and methods
        ServiceReference<?> serviceReference = obrBundle.getBundleContext()
                .getServiceReference(RepositoryAdmin.class.getName());
        if ((serviceReference != null)) {
            repositoryAdmin = (RepositoryAdmin) obrBundle.getBundleContext().getService(serviceReference);
        } else {
            throw new LauncherException("Unable to get OBR RepositoryAdmin service");
        }

        for (String bundleRepository : bundleRepositories) {
            logger.trace("Loading OBR OSGi Bundle Repository " + bundleRepository);
            Repository repository;
            try {
                repository = repositoryAdmin.addRepository(bundleRepository);
            } catch (Exception e) {
                throw new LauncherException("Unable to load repository " + bundleRepository, e);
            }

            if (logger.isTraceEnabled()) {
                // Print repository content
                logger.trace("Loaded repository " + repository.getName() + " from URI " + repository.getURI());
                Resource[] resources = repository.getResources();
                String sp3 = "   ";
                for (Resource resource : resources) {
                    logger.trace(sp3 + resource.getId());
                    logger.trace(sp3 + sp3 + resource.getURI());
                    logger.trace(sp3 + sp3 + resource.getSymbolicName());
                    logger.trace(sp3 + sp3 + resource.getPresentationName());
                    logger.trace(sp3 + sp3 + "requirements:");
                    Requirement[] requirements = resource.getRequirements();
                    for (Requirement requirement : requirements) {
                        logger.trace("        " + requirement.getFilter() + " optional=" + requirement.isOptional());
                    }
                }
            }
        }
    }

    /**
     * Load a bundle from the OSGi Bundle Repository
     * 
     * @param bundleSymbolicName
     * @throws LauncherException
     */
    private void loadBundle(String bundleSymbolicName) throws LauncherException {

        logger.trace("Installing bundle " + bundleSymbolicName);
        Resolver resolver = repositoryAdmin.resolver();
        String filterString = "(symbolicname=" + bundleSymbolicName + ")";
        Resource[] resources = null;
        try {
            resources = repositoryAdmin.discoverResources(filterString);
        } catch (InvalidSyntaxException e) {
            throw new LauncherException("Unable to discover repoistory resources", e);
        }
        try {
            if (resources.length == 0) {
                throw new LauncherException("Unable to locate bundle \"" + bundleSymbolicName + "\" in OBR repository");
            }
            addResource(bundleSymbolicName, resolver, resources[0]); // *** get first resource
        } catch (LauncherException e) {
            throw new LauncherException("Unable to install bundle \"" + bundleSymbolicName + "\" from OBR repository",
                    e);
        }
    }

    /**
     * Add the Resource to the Resolver and resolve
     * 
     * @param bundleSymbolicName
     * @param resolver
     * @param resource
     * @throws LauncherException
     */
    private void addResource(String bundleSymbolicName, Resolver resolver, Resource resource) throws LauncherException {
        logger.trace("Resouce: " + resource);
        resolver.add(resource);

        boolean resourceHasReferenceUrl = false;
        if (resource.getURI().startsWith("reference:")) {
            resourceHasReferenceUrl = true;
        }

        if (resolver.resolve()) {
            Resource[] requiredResources = resolver.getRequiredResources();
            for (Resource requiredResource : requiredResources) {
                if (requiredResource.getURI().startsWith("reference:")) {
                    resourceHasReferenceUrl = true;
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("  RequiredResource: " + requiredResource.getSymbolicName());
                }
            }
            Resource[] optionalResources = resolver.getOptionalResources();
            for (Resource optionalResource : optionalResources) {
                if (optionalResource.getURI().startsWith("reference:")) {
                    resourceHasReferenceUrl = true;
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("  OptionalResource: " + optionalResource.getSymbolicName());
                }
            }

            if (!resourceHasReferenceUrl) {
                resolver.deploy(Resolver.START);
            } else {
                // *** The Resolver can't cope with reference: URIs which is valid for Felix.
                // *** So we have to manually install and start the bundles if ANY bundle
                // *** is a reference
                ArrayList<Bundle> bundlesToStart = new ArrayList<>();
                try {
                    Resource[] startRequiredResources = resolver.getRequiredResources();
                    for (Resource requiredResource : startRequiredResources) {
                        bundlesToStart.add(
                                this.framework.getBundleContext().installBundle(requiredResource.getURI().toString()));
                    }
                    Resource[] startOptionalResources = resolver.getOptionalResources();
                    for (Resource optionalResource : startOptionalResources) {
                        bundlesToStart.add(
                                this.framework.getBundleContext().installBundle(optionalResource.getURI().toString()));
                    }

                    bundlesToStart.add(this.framework.getBundleContext().installBundle(resource.getURI().toString()));
                    for (Bundle bundle : bundlesToStart) {
                        bundle.start();
                    }
                } catch (Exception e) {
                    throw new LauncherException("Unable to install bundles outside of resolver", e);
                }
            }

            if (!isBundleActive(bundleSymbolicName)) {
                String msg = MessageFormat.format("Bundle ''{0}'' failed to install and activate",bundleSymbolicName);
                throw new LauncherException(msg);
            }

            printBundles();
        } else {
            logger.error("Unable to resolve " + resource.toString());
            Reason[] unsatisfiedRequirements = resolver.getUnsatisfiedRequirements();
            for (Reason reason : unsatisfiedRequirements) {
                logger.error("Unsatisfied requirement: " + reason.getRequirement());
            }
            throw new LauncherException("Unable to resolve bundle " + bundleSymbolicName);
        }
    }

    /**
     * Return the installed Bundle object for the bundle symbolic name
     * 
     * @param bundleSymbolicName
     * @return The bundle object
     * @throws LauncherException
     */
    private Bundle getBundle(String bundleSymbolicName) throws LauncherException {
        Bundle[] bundles = framework.getBundleContext().getBundles();
        for (Bundle bundle : bundles) {
            if (bundle.getSymbolicName().contentEquals(bundleSymbolicName)) {
                return bundle;
            }
        }

        throw new LauncherException("Unable to find bundle with Bundle-SymbolicName=" + bundleSymbolicName);
    }

    /**
     * Install a bundle from class path and optionally start it
     * 
     * @param bundleJar
     * @param start
     * @return The installed bundle
     * @throws BundleException
     * @throws LauncherException
     */
    private Bundle installBundle(String bundleJar, boolean start) throws LauncherException, BundleException {

        String bundleLocation = null;
        // Bundle location different when running from jar or IDE
        if (isJar()) {
            bundleLocation = this.getClass().getClassLoader().getResource("bundle/" + bundleJar).toExternalForm();
        } else {
            bundleLocation = "jar:file:/" + bootJarLoacation + "!/bundle/" + bundleJar;
        }
        logger.trace("bundleLocation: " + bundleLocation);
        Bundle bundle = framework.getBundleContext().installBundle(bundleLocation);
        if (start) {
            bundle.start();
        }

        printBundles();

        return bundle;
    }

    /**
     * Determine if this class is running from a jar file
     * 
     * @return true or false
     * @throws LauncherException
     */
    private boolean isJar() throws LauncherException {
        URL resourceURL = this.getClass().getClassLoader().getResource("");
        if (resourceURL == null) {
            resourceURL = this.getClass().getResource("");
        }
        if (resourceURL == null) {
            throw new LauncherException("Unable to determine if running from a jar file");
        }
        logger.trace("isJar resource URL protocol: " + resourceURL.getProtocol());
        return Objects.equals(resourceURL.getProtocol(), "jar");
    }

    /**
     * Is the supplied active in the OSGi framework
     * 
     * @param bundleSymbolicName
     * @return true or false
     */
    private boolean isBundleActive(String bundleSymbolicName) {
        Bundle[] bundles = framework.getBundleContext().getBundles();
        for (Bundle bundle : bundles) {
            if (bundle.getSymbolicName().equals(bundleSymbolicName) && bundle.getState() == Bundle.ACTIVE) {
                return true;
            }
        }

        return false;
    }

    /**
     * Print the currently installed bundles and their state
     */
    private void printBundles() {
        if (!logger.isTraceEnabled()) {
            return;
        }
        // Get the bundles
        Bundle[] bundles = framework.getBundleContext().getBundles();
        // Format and print the bundle Id, State, Symbolic name and Version.
        StringBuilder messageBuffer = new StringBuilder(2048);
        messageBuffer.append("Bundle status:");

        for (Bundle bundle : bundles) {
            String gitHash = "";
            try {
                URL githashUrl = bundle.getEntry("/META-INF/git.hash");
                if (githashUrl != null) {
                    try (InputStream is = githashUrl.openStream()) {
                        gitHash = "-" + IOUtils.toString(is, StandardCharsets.UTF_8);
                    }
                }
            } catch(Exception e) {}
            
            String bundleId = String.valueOf(bundle.getBundleId());
            messageBuffer.append("\n").append(String.format("%5s", bundleId)).append("|")
                    .append(String.format("%-11s", getBundleStateLabel(bundle))).append("|     |")
                    .append(bundle.getSymbolicName()).append(" (")
                    .append(bundle.getVersion()).append(gitHash).append(")");
       }

        logger.trace(messageBuffer.toString());
    }

    /**
     * Convert bundle state to string
     * 
     * @param bundle
     * @return The bundle state
     */
    private String getBundleStateLabel(Bundle bundle) {
        switch (bundle.getState()) {
            case Bundle.UNINSTALLED:
                return "Uninstalled";
            case Bundle.INSTALLED:
                return "Installed";
            case Bundle.RESOLVED:
                return "Resolved";
            case Bundle.STARTING:
                return "Starting";
            case Bundle.STOPPING:
                return "Stopping";
            case Bundle.ACTIVE:
                return "Active";
            default:
                return "<Unknown (" + bundle.getState() + ")>";
        }
    }

}