/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import dev.galasa.boot.BootLogger.Level;
import dev.galasa.boot.felix.FelixFramework;

/**
 * Galasa command line launcher.<br>
 * 
 * Required parameters:<br>
 * &nbsp;&nbsp;<code>--obr infra.obr --obr test.obr test.bundle/test.package.TestClass</code><br>
 * 
 * <ol>
 * <li>Start Felix OSGi framework</li>
 * <li>Loads required bundles</li>
 * <li>Loads OSGi Bundle repositories from one or more <code>--obr</code>
 * parameters</li>
 * <li>Loads test bundle <code>test.bundle</code> from OBR</li>
 * <li>Loads and runs test class <code>test.package.TestClass</code></li>
 * </ol>
 *
 */
public class Launcher {

    private static final String     OBR_OPTION                = "obr";
    private static final String     BOOTSTRAP_OPTION          = "bootstrap";
    private static final String     OVERRIDES_OPTION          = "overrides";
    private static final String     RESOURCEMANAGEMENT_OPTION = "resourcemanagement";
    private static final String     K8SCONTROLLER_OPTION      = "k8scontroller";
    private static final String     API_OPTION                = "api";
    private static final String     DOCKERCONTROLLER_OPTION   = "dockercontroller";
    private static final String     METRICSERVER_OPTION       = "metricserver";
    private static final String     TEST_OPTION               = "test";
    private static final String     RUN_OPTION                = "run";
    private static final String     GHERKIN_OPTION            = "gherkin";
    private static final String     BUNDLE_OPTION             = "bundle";
    private static final String     METRICS_OPTION            = "metrics";
    private static final String     HEALTH_OPTION             = "health";
    private static final String     LOCALMAVEN_OPTION         = "localmaven";
    private static final String     REMOTEMAVEN_OPTION        = "remotemaven";
    private static final String     TRACE_OPTION              = "trace";
    private static final String     FILE_OPTION               = "f";
    private static final String     FILE_OPTION_LONG          = "file";
    private static final String     DRY_RUN_OPTION            = "dryrun";
    private static final String     SETUPECO_OPTION           = "setupeco";
    private static final String     VALIDATEECO_OPTION        = "validateeco";

    private static final String     USER_HOME                 = "user.home";

    private static final BootLogger logger                    = new BootLogger();

    private List<String>            bundleRepositories        = new ArrayList<>();
    private String                  testName;
    private String                  testBundleName;
    private String                  testClassName;
    private String                  runName;
    private String                  gherkinName;

    private String                  galasaHome;

    private FelixFramework          felixFramework;

    private Properties              bootstrapProperties;
    private Properties              overridesProperties;

    private boolean                 isTestRun;
    private boolean                 isResourceManagement;
    private boolean                 isK8sController;
    private boolean                 isDockerController;
    private boolean                 isMetricsServer;
    private boolean                 isApiServer;
    private boolean                 isDryRun;
    private boolean                 setupEco;
    private boolean                 validateEco;

    private Integer                 metrics;
    private Integer                 health;

    private List<String>            bundles                   = new ArrayList<>();

    private URL                     localMavenRepo;
    private List<URL>               remoteMavenRepos          = new ArrayList<>();

    public Environment              env                       = new SystemEnvironment();

    /**
     * Launcher main method
     * 
     * @param args Arguments from the command line
     */
    public static void main(String[] args) {
        Launcher launcher = new Launcher();
        try {
            launcher.launch(args);
        } catch(Exception e) {
            logger.error("Exiting launcher due to exception",e);
            System.exit(16);
        }
        System.exit(0);
    }

    /**
     * Process supplied command line arguments and run the test
     * 
     * @param args test run parameters
     * @throws InterruptedException
     * @throws Exception
     */
    protected void launch(String[] args) throws LauncherException, InterruptedException {

        validateJavaLevel(env);

        this.galasaHome = getGalasaHome(env);

        felixFramework = new FelixFramework();

        // Build Felix framework and install required bundles
        try {
            processCommandLine(args);
        } catch (ParseException e) {
            throw new LauncherException("Unable to parse command line arguments", e);
        }
        logger.debug("OBR Repository Files: " + bundleRepositories);
        logger.debug("Launching Framework...");

        try {
            // Build the Framework
            buildFramework();

            if (isTestRun) {
                if (testBundleName != null && testClassName != null) {
                    // Run test class
                    logger.debug("Test Bundle: " + testBundleName);
                    logger.debug("Test Class: " + testClassName);
                    overridesProperties.setProperty("framework.run.testbundleclass", this.testName);
                } else if (runName != null) {
                    logger.debug("Test Run: " + runName);
                    overridesProperties.setProperty("framework.run.name", this.runName);
                } else {
                    logger.debug("Gherkin Run: " + gherkinName.toString());
                    overridesProperties.setProperty("framework.run.gherkintest", this.gherkinName);
                }

                felixFramework.runTest(bootstrapProperties, overridesProperties);
            } else if (isResourceManagement) {
                logger.debug("Resource Management");
                felixFramework.runResourceManagement(bootstrapProperties, overridesProperties, bundles, metrics, health);
            } else if (isK8sController) {
                logger.debug("Kubernetes Controller");
                felixFramework.runK8sController(bootstrapProperties, overridesProperties, bundles, metrics, health);
            } else if (isDockerController) {
                logger.debug("Docker Controller");
                felixFramework.runDockerController(bootstrapProperties, overridesProperties, bundles, metrics, health);
            } else if (isMetricsServer) {
                logger.debug("Metrics Server");
                felixFramework.runMetricsServer(bootstrapProperties, overridesProperties, bundles, metrics, health);
            } else if (isApiServer) {
                logger.debug("Web API Server");
                felixFramework.runWebApiServer(bootstrapProperties, overridesProperties, bundles, metrics, health);
            } else if (setupEco) {
                felixFramework.runSetupEcosystem(bootstrapProperties, overridesProperties);
            } else if (validateEco) {
                felixFramework.runValidateEcosystem(bootstrapProperties, overridesProperties);
            }

        } catch (LauncherException e) {
            logger.error("Unable to run test class", e);
            throw e;
        } finally {
            if (felixFramework != null) {
                felixFramework.stopFramework();
            }
        }

        logger.info("Boot complete");
    }

    private void buildFramework() throws LauncherException {
        logger.debug("Launching Framework...");
        try {
            felixFramework.buildFramework(bundleRepositories, this.bootstrapProperties, localMavenRepo,
                    remoteMavenRepos, galasaHome);
        } catch (Exception e) {
            throw new LauncherException("Unable to create and initialize Felix framework", e);
        }
    }


    protected String logCommandLineArguments(String[] args) {
        StringBuilder messageBuffer = new StringBuilder();
        messageBuffer.append("Supplied command line arguments: ");
        for (String arg : args) {
            if (arg.contains("DGALASA_JWT")){
                String[] argument = arg.split("=");
                messageBuffer.append(argument[0] + "=******* ");
            }else{
                messageBuffer.append(arg + " ");
            }
        }
        return messageBuffer.toString();
    }

    /**
     * Process the supplied command line augments
     * 
     * @param args supplied arguments
     * @throws ParseException
     */
    private void processCommandLine(String[] args) throws ParseException {

        logger.debug(logCommandLineArguments(args));

        Options options = new Options();
        Option propertyOption = Option.builder().longOpt("D").argName("property=value").hasArgs().valueSeparator()
                .numberOfArgs(2).desc("use value for given properties").build();
        options.addOption(propertyOption);
        options.addOption(null, OBR_OPTION, true, "Felix OBR Repository File name");
        options.addOption(null, BOOTSTRAP_OPTION, true, "Bootstrap properties file url");
        options.addOption(null, OVERRIDES_OPTION, true, "Overrides properties file url");
        options.addOption(null, RESOURCEMANAGEMENT_OPTION, false, "A Resource Management server");
        options.addOption(null, K8SCONTROLLER_OPTION, false, "A k8s Controller server");
        options.addOption(null, API_OPTION, false, "A Web App server, list bundles to load or ALL");
        options.addOption(null, DOCKERCONTROLLER_OPTION, false, "A Docker Controller server");
        options.addOption(null, METRICSERVER_OPTION, false, "A Metrics server");
        options.addOption(null, TEST_OPTION, true, "The test to run");
        options.addOption(null, RUN_OPTION, true, "The run name");
        options.addOption(null, GHERKIN_OPTION, true, "The gherkin test to run");
        options.addOption(null, BUNDLE_OPTION, true, "Extra bundles to load");
        options.addOption(null, METRICS_OPTION, true, "The port the metrics server will open, 0 to disable");
        options.addOption(null, HEALTH_OPTION, true, "The port the health server will open, 0 to disable");
        options.addOption(null, LOCALMAVEN_OPTION, true, "The local maven repository, defaults to ~/.m2/repository");
        options.addOption(null, REMOTEMAVEN_OPTION, true, "The remote maven repositories, defaults to central");
        options.addOption(null, TRACE_OPTION, false, "Enable TRACE logging");
        options.addOption(FILE_OPTION, FILE_OPTION_LONG, true, "File for data input/output");
        options.addOption(null, DRY_RUN_OPTION, false, "Perform a dry-run of the specified actions. Can be combined with \"" + FILE_OPTION_LONG + "\"");
        options.addOption(null, SETUPECO_OPTION, false, "Setup the Galasa Ecosystem");
        options.addOption(null, VALIDATEECO_OPTION, false, "Validate the Galasa Ecosystem");
        

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = null;
        commandLine = parser.parse(options, args);

        if (commandLine.hasOption("D")) {
            Properties commandLinePropety = commandLine.getOptionProperties("D");
            Set<Object> keys = commandLinePropety.keySet();
            for (Object key : keys) {
                System.setProperty((String) key, commandLinePropety.getProperty((String) key));
            }
        }

        if (commandLine.hasOption(TRACE_OPTION)) {
            logger.setLevel(Level.TRACE);
            System.setProperty("log4j2.configurationFile", "trace-log4j2.properties");
        }

        // *** Add any OBRs if coded
        if (commandLine.hasOption(OBR_OPTION)) {
            for (String option : commandLine.getOptionValues(OBR_OPTION)) {
                bundleRepositories.add(option);
            }
        }

        checkForBootstrap(commandLine);
        setStoresFromEnvironmentVariables(env,bootstrapProperties);
        checkForOverrides(commandLine);
        checkForBundles(commandLine);
        checkForMetricsPort(commandLine);
        checkForHealthPort(commandLine);
        checkForLocalMaven(commandLine);
        checkForRemoteMaven(commandLine);

        isTestRun = commandLine.hasOption(TEST_OPTION) || commandLine.hasOption(RUN_OPTION) || commandLine.hasOption(GHERKIN_OPTION);
        isResourceManagement = commandLine.hasOption(RESOURCEMANAGEMENT_OPTION);
        isK8sController = commandLine.hasOption(K8SCONTROLLER_OPTION);
        isDockerController = commandLine.hasOption(DOCKERCONTROLLER_OPTION);
        isMetricsServer = commandLine.hasOption(METRICSERVER_OPTION);
        isApiServer = commandLine.hasOption(API_OPTION);
        isDryRun = commandLine.hasOption(DRY_RUN_OPTION);
        setupEco = commandLine.hasOption(SETUPECO_OPTION);
        validateEco = commandLine.hasOption(VALIDATEECO_OPTION);

        if (isTestRun) {
            runName = commandLine.getOptionValue(RUN_OPTION);
            testName = commandLine.getOptionValue(TEST_OPTION);
            gherkinName = commandLine.getOptionValue(GHERKIN_OPTION);

            if (runName != null) {
                runName = runName.toUpperCase();
            } else if (testName != null) {
                String[] bundleclass = testName.split("/");
                if (bundleclass.length != 2) {
                    commandLineError("Error: Invalid test name format");
                }

                testBundleName = bundleclass[0];
                testClassName = bundleclass[1];
                if (testBundleName.isEmpty() || testClassName.isEmpty()) {
                    commandLineError("Error: Invalid test name format");
                }
            } else if (gherkinName != null) {
                return;
            } else {
                commandLineError("Error: A valid run, java test or gherkin test must be supplied");
            }
            return;
        }

        if (isResourceManagement) {
            return;
        }

        if (isK8sController) {
            return;
        }

        if (isDockerController) {
            return;
        }

        if (isMetricsServer) {
            return;
        }
        
        if (isApiServer) {
            return;
        }
        
        if (setupEco) {
            return;
        }

        if (validateEco) {
            return;
        }

        commandLineError(
                "Error: Must select either --" + TEST_OPTION
                		+ ", --" + RUN_OPTION
                		+ ", --" + GHERKIN_OPTION
                		+ ", --" + K8SCONTROLLER_OPTION
                		+ ", --" + METRICSERVER_OPTION
                		+ ", --" + RESOURCEMANAGEMENT_OPTION
                		+ ", --" + BUNDLE_OPTION
                        + ", --" + SETUPECO_OPTION
                        + ", --" + VALIDATEECO_OPTION
                        );
    }

    private void checkForRemoteMaven(CommandLine commandLine) {

        try {
            if (commandLine.hasOption(REMOTEMAVEN_OPTION)) {
                for (String option : commandLine.getOptionValues(REMOTEMAVEN_OPTION)) {
                    this.remoteMavenRepos.add(new URL(option.replaceFirst("/*$", "")));
                }
            }
            this.remoteMavenRepos.add(new URL("https://repo.maven.apache.org/maven2"));
        } catch (MalformedURLException e) {
            logger.error("invalid remote maven urls", e);
            commandLineError(null);
        }

    }

    private void checkForLocalMaven(CommandLine commandLine) {
        if (commandLine.hasOption(LOCALMAVEN_OPTION)) {
            String repo = commandLine.getOptionValue(LOCALMAVEN_OPTION);
            if (repo != null) { // *** Allowed with no parameter so that we can disable the local repository
                if ("disable".equals(repo)) {
                    this.localMavenRepo = null;
                } else {
                    try {
                        this.localMavenRepo = new URL(repo.replaceFirst("/*$", ""));
                    } catch (MalformedURLException e) {
                        logger.error("--localmaven has an invalid URL", e);
                        commandLineError(null);
                    }
                    if (!"file".equals(this.localMavenRepo.getProtocol())) {
                        commandLineError("--localmaven must be a file: URL");
                    }
                }
            }
        } else {
            try {
                this.localMavenRepo = new File(
                        env.getProperty(USER_HOME) + File.separator + ".m2" + File.separator + "repository").toURI()
                                .toURL();
            } catch (MalformedURLException e) {
                logger.error("internal error", e);
                commandLineError(null);
            }
        }
    }

    private void checkForMetricsPort(CommandLine commandLine) {
        if (commandLine.hasOption(METRICS_OPTION)) {
            String port = commandLine.getOptionValue(METRICS_OPTION);
            metrics = Integer.parseInt(port);
        }
    }

    private void checkForHealthPort(CommandLine commandLine) {
        if (commandLine.hasOption(HEALTH_OPTION)) {
            String port = commandLine.getOptionValue(HEALTH_OPTION);
            health = Integer.parseInt(port);
        }
    }

    private void checkForBundles(CommandLine commandLine) {
        if (commandLine.hasOption(BUNDLE_OPTION)) {
            for (String option : commandLine.getOptionValues(BUNDLE_OPTION)) {
                bundles.add(option);
            }
        }
    }

    /**
     * Retrieve the bootstrap URI from the command line and load the properties
     * file. If the the option is not provided, getGalasaHome is called to provide a path
     * which will be created if it does not exist.
     * 
     * @param commandLine - The command line instance
     */
    private void checkForBootstrap(CommandLine commandLine) {
        URI bootstrapUri = null;
        if (commandLine.hasOption(BOOTSTRAP_OPTION)) {
            String uri = commandLine.getOptionValue(BOOTSTRAP_OPTION);
            try {
                bootstrapUri = new URI(uri);
            } catch (URISyntaxException e) {
                logger.error("Invalid bootstrap URI provided", e);
                commandLineError(null);
            }
        } else {
            Path path = Paths.get(this.galasaHome, "bootstrap.properties");
            try {
                if (!path.toFile().exists()) {
                    if (!path.getParent().toFile().exists()) {
                        Files.createDirectories(path.getParent());
                    }
                    Files.createFile(path);
                }
                bootstrapUri = path.toUri();
            } catch (IOException e) {
                logger.error("Unable to create empty default bootstrap file " + path.toUri().toString(), e);
                commandLineError(null);
            }
        }

        try {
            URLConnection bootstrapConnection = bootstrapUri.toURL().openConnection();
            bootstrapConnection.setConnectTimeout(30000);
            bootstrapConnection.setReadTimeout(30000);
            try (InputStream is = bootstrapConnection.getInputStream()) {
                bootstrapProperties = new Properties();
                bootstrapProperties.load(is);
                bootstrapProperties.setProperty("framework.galasa.home",galasaHome);

            }
        } catch (IOException e) {
            logger.error("Unable to load bootstrap properties", e);
            commandLineError(null);
        }
    }

    /**
     * Retrieve the overrides URI from the command line and load the overrides file.
     * If the the option is not provided,
     * If the the option is not provided, getGalasaHome is called to provide a path
     * 
     * @param commandLine - The command line instance
     */
    private void checkForOverrides(CommandLine commandLine) {
        URI overridesUri = null;
        if (commandLine.hasOption(OVERRIDES_OPTION)) {
            String uri = commandLine.getOptionValue(OVERRIDES_OPTION);
            try {
                overridesUri = new URI(uri);
            } catch (URISyntaxException e) {
                logger.error("Invalid overrides URI provided", e);
                commandLineError(null);
            }
        } else {
            Path path = Paths.get(this.galasaHome, "overrides.properties");
            if (!path.toFile().exists()) {
                this.overridesProperties = new Properties();
                return;
            }
            overridesUri = path.toUri();
        }

        try (InputStream is = overridesUri.toURL().openStream()) {
            overridesProperties = new Properties();
            overridesProperties.load(is);
        } catch (IOException e) {
            logger.error("Unable to load overrides properties", e);
            commandLineError(null);
        }
    }

    /**
     * Issue command line options error and exit
     */
    private void commandLineError(String message) {
        if (message != null) {
            logger.error(message);
        }

        logger.error(
                "\nExample test run arguments: --obr infra.obr --obr test.obr --test test.bundle/test.package.TestClass\n"
                        + "Example Resource Management arguments: --obr infra.obr --obr test.obr --resourcemanagement");
        System.exit(-1);
    }

    public void validateJavaLevel(Environment env) throws LauncherException{
        String version = env.getProperty("java.version");
        logger.trace("Checking version of Java, found: " + version);
        if(version == null || version.isEmpty()){
            logger.error("Unable to determine Java version - will exit");
            throw new LauncherException("Unable to determine Java version - will exit");
        }

        if(version.startsWith("17") || version.startsWith("11")){
            logger.trace("Java version " + version + " validated");
            return;
        }

        logger.error("Galasa requires Java 11, we found: " + version + " will exit");
        throw new LauncherException("Galasa requires Java 11, we found: " + version + " will exit");
    }
    
    /**
     * Obtain the location of the galasa home directory
     * @return a String representing the location of the users Galasa home directory
     */
    public String getGalasaHome(Environment env) {
        // 1st: If GALASA_HOME is set as a system property then use that,
        // 2nd: If GALASA_HOME is set as a system environment variable, then use that.
        // 3rd: otherwise we use the calling users' home folder.
        String GALASA_HOME = "GALASA_HOME";
        String home = env.getProperty(GALASA_HOME);
        if( (home == null) || (home.trim().isEmpty())) {
            home = env.getenv(GALASA_HOME);
            if( (home == null) || (home.trim().isEmpty())) {
                home = env.getProperty(USER_HOME)+"/.galasa";
                logger.info("System property "+USER_HOME+" used to set value of home location.");
            } else {
                logger.info("Environment variable GALASA_HOME used to set value of home location.");
            }
        } else {
            logger.info("System property GALASA_HOME used to set value of home location.");
            // The system property value may be surrounded by " characters.
            // If so, strip them off.
            // We allow this because a path with strings in would be split
            // into separate system properties otherwise.
            home = stripLeadingAndTrailingQuotes(home);
        }
        logger.info("Galasa home location is "+home);

        return home;
    }

    /**
     * String the first double-quote and the last double-quote off
     * the begining and end of a string.
     * @param input
     * @return The stripped (or unaltered) string.
     */
    String stripLeadingAndTrailingQuotes(String input ) {
        String output = input ;
        if (output.startsWith("\"")) {
            output = output.replaceFirst("\"", "");
        }
        if (output.endsWith("\"")) {
            output = output.substring(0,output.length()-1);
        }
        return output;
    }


    /**
     * Read a set of environment variables that will override the values of the CPS, RAS, DSS and Creds store
     * if these are set then they override the value in the bootstrap   
     */
    public void setStoresFromEnvironmentVariables(Environment env, Properties bootstrap) {
        String CPS_ENV_VAR = "GALASA_CONFIG_STORE";
        String DSS_ENV_VAR = "GALASA_DYNAMICSTATUS_STORE";
        String RAS_ENV_VAR = "GALASA_RESULTARCHIVE_STORE";
        String CREDS_ENV_VAR = "GALASA_CREDENTIALS_STORE";
        String AUTH_ENV_VAR = "GALASA_AUTH_STORE";

        String cps = env.getenv(CPS_ENV_VAR);

        if( (cps != null) && (!cps.trim().isEmpty())){
            cps = cps.trim();
            logger.info(String.format("Environment variable: %s used to locate CPS location",CPS_ENV_VAR));
            bootstrap.setProperty("framework.config.store",cps);
        }

        String dss = env.getenv(DSS_ENV_VAR);
        if( (dss != null) && (!dss.trim().isEmpty())){
            dss = dss.trim();
            logger.info(String.format("Environment variable: %s used to locate DSS location",DSS_ENV_VAR));
            bootstrap.setProperty("framework.dynamicstatus.store",dss);
        }

        String ras = env.getenv(RAS_ENV_VAR);
        if( (ras != null) && (!ras.trim().isEmpty())){
            ras = ras.trim();
            logger.info(String.format("Environment variable: %s used to locate RAS location",RAS_ENV_VAR));
            bootstrap.setProperty("framework.resultarchive.store",ras);
        }

        String creds = env.getenv(CREDS_ENV_VAR);
        if( (creds != null) && (!creds.trim().isEmpty())){
            creds = creds.trim();
            logger.info(String.format("Environment variable: %s used to locate credentials store location",CREDS_ENV_VAR));
            bootstrap.setProperty("framework.credentials.store",creds);
        }

        String authStore = env.getenv(AUTH_ENV_VAR);
        if( (authStore != null) && (!authStore.trim().isEmpty())){
            authStore = authStore.trim();
            logger.info(String.format("Environment variable: %s used to locate auth store location",AUTH_ENV_VAR));
            bootstrap.setProperty("framework.auth.store",authStore);
        }
    }
}