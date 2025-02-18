/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import javax.validation.constraints.NotNull;
import org.apache.commons.logging.*;
import org.osgi.framework.*;

import dev.galasa.framework.spi.*;
import dev.galasa.framework.spi.creds.*;

public class FrameworkInitialisation implements IFrameworkInitialisation {

    private static final String                      USER_HOME = "user.home";
    protected Framework                              framework;

    private final URI                                uriConfigurationPropertyStore;
    private final URI                                uriDynamicStatusStore;
    private final URI                                uriCredentialsStore;
    private final List<URI>                          uriResultArchiveStores;

    private final IConfigurationPropertyStoreService cpsFramework;
    private final IDynamicStatusStoreService         dssFramework;
    // private final ICredentialsStoreService credsFramework;

    private Log logger;
    private IFileSystem fileSystem ;

    private String galasaHome;
    
    public FrameworkInitialisation(
        Properties bootstrapProperties, 
        Properties overrideProperties
    ) throws URISyntaxException, InvalidSyntaxException, FrameworkException {
        this(bootstrapProperties, overrideProperties, null, 
        getBundleContext() , new FileSystem() , new SystemEnvironment(),
        GalasaFactory.getInstance().newDefaultInitStrategy());
    }


    /**
     * @deprecated Use one of the other constructors.
     */
    @Deprecated(since = "0.40.0", forRemoval = true)
    public FrameworkInitialisation(
        Properties bootstrapProperties, 
        Properties overrideProperties, 
        boolean isTestRun
    ) throws URISyntaxException, InvalidSyntaxException, FrameworkException {
        this(bootstrapProperties, overrideProperties, null, 
            getBundleContext() , new FileSystem(), new SystemEnvironment(),
            isTestRun? GalasaFactory.getInstance().newTestRunInitStrategy(): GalasaFactory.getInstance().newDefaultInitStrategy()
        );
    }

    public FrameworkInitialisation(
        Properties bootstrapProperties, 
        Properties overrideProperties, 
        IFrameworkInitialisationStrategy initStrategy
    ) throws URISyntaxException, InvalidSyntaxException, FrameworkException {
        this(bootstrapProperties, overrideProperties, null, 
            getBundleContext() , new FileSystem(), new SystemEnvironment(),
            initStrategy
        );
    }


    /**
     * @deprecated Use one of the other constructors.
     */
    @Deprecated(since = "0.40.0", forRemoval = true)
    public FrameworkInitialisation(
        Properties bootstrapProperties, 
        Properties overrideProperties,
        boolean isTestRun,
        Log initLogger
    ) throws URISyntaxException, InvalidSyntaxException, FrameworkException {
        this(bootstrapProperties, overrideProperties, initLogger, 
            getBundleContext(), new FileSystem(), new SystemEnvironment(),
            isTestRun? GalasaFactory.getInstance().newTestRunInitStrategy(): GalasaFactory.getInstance().newDefaultInitStrategy()
        );
    }

    /**
     * @deprecated Use one of the other constructors.
     */
    @Deprecated(since = "0.40.0", forRemoval = true)
    public FrameworkInitialisation(
        Properties bootstrapProperties, 
        Properties overrideProperties, 
        boolean isTestRun,
        Log initLogger,
        BundleContext bundleContext , 
        IFileSystem fileSystem,
        Environment env
    ) throws URISyntaxException, InvalidSyntaxException, FrameworkException {
        this(bootstrapProperties, overrideProperties, initLogger, bundleContext, fileSystem, env, 
            isTestRun? GalasaFactory.getInstance().newTestRunInitStrategy(): GalasaFactory.getInstance().newDefaultInitStrategy()
        );
    }
    
    /**
     * 
     * @param bootstrapProperties
     * @param overrideProperties
     * @param initLogger
     * @param bundleContext
     * @param fileSystem
     * @param env
     * @param initStrategy One of the initialisation strategies, depending on what you want to use the Framework for.
     * See `GalasaFactory.getInstance().newDefaultInitStrategy()` or one of the other `newXXXStrategy` methods.
     * @throws URISyntaxException
     * @throws InvalidSyntaxException
     * @throws FrameworkException
     */
    public FrameworkInitialisation(
        Properties bootstrapProperties, 
        Properties overrideProperties, 
        Log initLogger,
        BundleContext bundleContext , 
        IFileSystem fileSystem,
        Environment env,
        IFrameworkInitialisationStrategy initStrategy
    ) throws URISyntaxException, InvalidSyntaxException, FrameworkException {

        this.fileSystem = fileSystem;

        // Allows unit tests to inject the logger.
        if (initLogger == null) {
            logger = LogFactory.getLog(this.getClass());
        } else {
            logger = initLogger;
        }

        //load galasa home from the bootstrap
        this.galasaHome = bootstrapProperties.getProperty("framework.galasa.home");
        if (this.galasaHome == null) {
            this.galasaHome = getGalasaHome(env);
        }
        logger.info("Galasa home folder is "+this.galasaHome);

        // *** Copy the the bootstrap properties to the override properties so that they
        // are available to the managers
        overrideProperties.putAll(bootstrapProperties);

        logger.info("Initialising the Galasa Framework");

        this.framework = locateFramework(bundleContext);
        assertFrameworkNotAlreadyInitialised(this.framework);

        this.framework.setFrameworkProperties(overrideProperties);

        // The initialisation strategy may wish to install Log capture at this point ?
        initStrategy.startLoggingCapture(framework);

        this.uriConfigurationPropertyStore = locateConfigurationPropertyStore(
            this.logger, overrideProperties, this.fileSystem);
        this.cpsFramework = initialiseConfigurationPropertyStore(logger,bundleContext);

        this.uriDynamicStatusStore = locateDynamicStatusStore(
            this.logger, overrideProperties, this.cpsFramework, this.fileSystem);
        this.dssFramework = initialiseDynamicStatusStore(logger,bundleContext);

        initStrategy.setTestRunName(framework, this.cpsFramework);

        this.uriResultArchiveStores = createUriResultArchiveStores(overrideProperties, this.cpsFramework);
        logger.debug("Result Archive Stores are " + this.uriResultArchiveStores.toString());
        initialiseResultsArchiveStore(logger,bundleContext);

        this.uriCredentialsStore = locateCredentialsStore(
            this.logger,overrideProperties,this.cpsFramework,this.fileSystem);
        initialiseCredentialsStore(logger,bundleContext);

        initialiseConfidentialTextService(logger,bundleContext);

        initialiseEventsService(logger, bundleContext);
                
        if (framework.isInitialised()) {
            logger.info("Framework initialised");
        } else {
            logger.info("The Framework does not think it is initialised, but we didn't get any errors");
        }

        overrideProperties = initStrategy.applyOverrides(framework, this.dssFramework, overrideProperties);
        
    }

    private static BundleContext getBundleContext() {
        return FrameworkUtil.getBundle(FrameworkInitialisation.class).getBundleContext();
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
                String userHome = env.getProperty(USER_HOME);
                if ( userHome != null ) {
                    logger.info("System property "+USER_HOME+" ("+userHome+") used to set value of home location.");
                    home = userHome + "/.galasa";
                } else {
                    // On windows, the USERPROFILE environment variable will be set to C:/User/Name or similar.
                    String userProfile = env.getenv("USERPROFILE");
                    if (userProfile != null) {
                        logger.info("System property USERPROFILE ("+userProfile+") used to set value of home location.");
                        home = userProfile + "/.galasa";
                    } else {
                        // Just in case, fall back on a trusty default.
                        logger.info("Defaulting home to ~/.galasa");
                        home = "~/.galasa";
                    }
                }
                
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
    private String stripLeadingAndTrailingQuotes(String input ) {
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
     * Create an empty default property file if it doesn't already exist
     * 
     * @param propertyFile
     * @throws IOException
     */
    private void createIfMissing(URI propertyFile, IFileSystem fileSystem) {

        //if the URI is not using a file scheme then we cannot create it
        //so return
        if(!propertyFile.getScheme().startsWith("file")){
            return;
        }

        Path path = Paths.get(propertyFile);
        try {
            if (!fileSystem.exists(path)) {
                // Create the parent folders if they don't exist.
                if (! fileSystem.exists(path.getParent()) ) {
                    fileSystem.createDirectories(path.getParent());
                }
                // Create an empty file.
                logger.info("File "+path.toString()+" does not exist, so creating it.");
                fileSystem.createFile(path);
            }
        } catch (IOException e) {
            logger.error("Unable to create empty default property file " + path.toUri().toString(), e);
        }
    }

    private Framework locateFramework(BundleContext bundleContext) throws FrameworkException {
        ServiceReference<IFramework> frameworkService = bundleContext.getServiceReference(IFramework.class);
        if (frameworkService == null) {
            throw new FrameworkException("The framework service is missing");
        }
        return (Framework) bundleContext.getService(frameworkService);
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.IFrameworkInitialisation#
     * getBootstrapConfigurationPropertyStore()
     */
    @Override
    public @NotNull URI getBootstrapConfigurationPropertyStore() {
        return this.uriConfigurationPropertyStore;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * dev.galasa.framework.spi.IFrameworkInitialisation#getDynamicStatusStoreUri()
     */
    @Override
    public URI getDynamicStatusStoreUri() {
        return this.uriDynamicStatusStore;
    }

    @Override
    public URI getCredentialsStoreUri() {
        return this.uriCredentialsStore;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * dev.galasa.framework.spi.IFrameworkInitialisation#getResultArchiveStoreUris()
     */
    @Override
    public List<URI> getResultArchiveStoreUris() {
        return this.uriResultArchiveStores;
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.IFrameworkInitialisation#
     * registerConfigurationPropertyStoreService(dev.galasa.framework.spi.
     * IConfigurationPropertyStoreService)
     */
    @Override
    public void registerConfigurationPropertyStore(@NotNull IConfigurationPropertyStore configurationPropertyStore)
            throws ConfigurationPropertyStoreException {
        this.framework.setConfigurationPropertyStore(configurationPropertyStore);
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.IFrameworkInitialisation#
     * registerDynamicStatusStoreService(dev.galasa.framework.spi.
     * IDynamicStatusStoreService)
     */
    @Override
    public void registerDynamicStatusStore(@NotNull IDynamicStatusStore dynamicStatusStore)
            throws DynamicStatusStoreException {
        this.framework.setDynamicStatusStore(dynamicStatusStore);
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.IFrameworkInitialisation#
     * registerResultArchiveStoreService(dev.galasa.framework.spi.
     * IResultArchiveStoreService)
     */
    @Override
    public void registerResultArchiveStoreService(@NotNull IResultArchiveStoreService resultArchiveStoreService)
            throws ResultArchiveStoreException {
        this.framework.addResultArchiveStoreService(resultArchiveStoreService);

    }

    @Override
    public void registerConfidentialTextService(@NotNull IConfidentialTextService confidentialTextService)
            throws ConfidentialTextException {
        this.framework.setConfidentialTextService(confidentialTextService);
    }

    @Override
    public void registerCredentialsStore(@NotNull ICredentialsStore credentialsStore) throws CredentialsException {
        this.framework.setCredentialsStore(credentialsStore);
    }

    @Override
    public void registerEventsService(@NotNull IEventsService eventsService) throws EventsException {
        this.framework.setEventsService(eventsService);
    }
    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.IFrameworkInitialisation#getFramework()
     */
    @Override
    public @NotNull IFramework getFramework() {
        return this.framework;
    }

    public @NotNull IShuttableFramework getShutableFramework() {
        return this.framework;
    }

    public void shutdownFramework() {
        try {
            this.framework.shutdown(null);
        } catch(Exception e) {
            logger.fatal("Problem shutting down the Galasa framework",e);
        }
    }

	@Override
	public void registerCertificateStoreService(@NotNull ICertificateStoreService certificateStoreService)
			throws CertificateStoreException {
		
	}

    private void assertFrameworkNotAlreadyInitialised(Framework framework) throws FrameworkException {
        if (this.framework.isInitialised()) {
            throw new FrameworkException("The framework has already been initialised");
        }
    }

    /**
     * Find where the property store is located, or create a new one if it's not
     * 
     * Note: package level scope so we can unit test it.
     * @param logger
     * @param bootstrapProperties
     * @return
     * @throws URISyntaxException
     */
    URI locateConfigurationPropertyStore(
        Log logger ,
        Properties overrideProperties,
        IFileSystem fileSystem
    ) throws URISyntaxException {

        URI storeUri ;
        String propUri = overrideProperties.getProperty("framework.config.store");
        if ((propUri != null) && (!propUri.isEmpty())) {
            logger.debug("bootstrap property framework.config.store used to determine CPS location.");
        }

        if ((propUri == null) || propUri.isEmpty()) {
            Path path = Paths.get(this.galasaHome , "cps.properties");
            storeUri = path.toUri();
            logger.debug("galasa home used to determine CPS location.");
            createIfMissing(storeUri,fileSystem);
        } else {
            storeUri = new URI(propUri);
        }
        logger.debug("Configuration Property Store is " + storeUri.toString());
        return storeUri ;
    }


    // Find where the DSS should be, creating a new blank one if it's not already there.
    // Note: Not private so we can easily unit test it.
    URI locateDynamicStatusStore(
        Log logger,
        Properties overrideProperties,
        IConfigurationPropertyStoreService cpsFramework,
        IFileSystem fileSystem
    ) throws FrameworkException {

        URI uriDynamicStatusStore = null;
        try {
            String dssProperty = overrideProperties.getProperty("framework.dynamicstatus.store");
            if((dssProperty != null) && !dssProperty.isEmpty()){
                uriDynamicStatusStore = new URI(dssProperty);
                logger.debug("Obtained value of DSS from properties");
                logger.debug("Dynamic Status Store is " + uriDynamicStatusStore.toString());
                createIfMissing(uriDynamicStatusStore, fileSystem);
                return uriDynamicStatusStore;
            }

            dssProperty = cpsFramework.getProperty("dynamicstatus", "store");
            if((dssProperty != null) && !dssProperty.isEmpty()){
                uriDynamicStatusStore = new URI(dssProperty);
                logger.debug("Obtained value of DSS from CPS");
                logger.debug("Dynamic Status Store is " + uriDynamicStatusStore.toString());
                createIfMissing(uriDynamicStatusStore, fileSystem);
                return uriDynamicStatusStore;
            }

            uriDynamicStatusStore = Paths.get(this.galasaHome, "dss.properties").toUri();
            logger.debug("Dynamic Status Store is " + uriDynamicStatusStore.toString());
            logger.debug("Obtained value of DSS from galasa home");
            createIfMissing(uriDynamicStatusStore,fileSystem);
            return uriDynamicStatusStore;
        } catch (final Exception e) {
            throw new FrameworkException("Unable to resolve the Dynamic Status Store URI", e);
        }
    }



    private void processRASlocationProperty(String rasProperty , List<URI> uriResultArchiveStores) throws URISyntaxException {
        if((rasProperty != null) && !rasProperty.isEmpty()){
            final String[] rasPaths = rasProperty.split(",");
            for (final String rasPath : rasPaths) {
                if (!rasPath.trim().isEmpty()) {
                    logger.debug("Adding Result Archive Store location " + rasPath);
                    uriResultArchiveStores.add(new URI(rasPath));
                }
            }
        }
    }

    /**
     * Creates a list of URIs which refer to Result Archive Stores.
     *
     * @param cpsFramework
     * @return
     * @throws FrameworkException
     */
    List<URI> createUriResultArchiveStores(
        Properties overrideProperties,
        IConfigurationPropertyStoreService cpsFramework
    ) throws FrameworkException {

        ArrayList<URI> uriResultArchiveStores = new ArrayList<>(1);

        Path localRasPath = Paths.get(this.galasaHome, "ras");
        URI localRasUri = localRasPath.toUri();
        try {

            // Are we using a RAS store indicated by the overrides ?
            String rasProperty = overrideProperties.getProperty("framework.resultarchive.store");
            processRASlocationProperty(rasProperty,uriResultArchiveStores);

            
            if (uriResultArchiveStores.isEmpty()) {
                // We've not got a RAS store yet, so use the one configured in the CPS...
                rasProperty = cpsFramework.getProperty("resultarchive", "store");
                processRASlocationProperty(rasProperty,uriResultArchiveStores);
            }

            if(uriResultArchiveStores.isEmpty()) {
                // Neither environment nor cps have set the RAS location.
                // Default to a local RAS within galasaHome
                uriResultArchiveStores.add(localRasUri);
            }

            if(uriResultArchiveStores.isEmpty()){
                throw new FrameworkException("No Result Archive Store URIs were provided");
            }
        } catch (final FrameworkException e) {
            throw e;
        } catch (final Exception e) {
            throw new FrameworkException("Unable to resolve the Result Archive Store URIs", e);
        }


        // If resultarchive.store.include.default.local is set to TRUE (case insensitive)
        // then make sure the local RAS store is included in the list of RAS stores.
        boolean includeLocal = Boolean.parseBoolean(
            cpsFramework.getProperty("resultarchive.store", "include.default.local"));
        if (includeLocal) {
            if (! uriResultArchiveStores.contains(localRasUri)) {
                uriResultArchiveStores.add(localRasUri);
            }
        }

        return uriResultArchiveStores;
    }


    /**
     * Find the credentials store, creating it if it doesn't already exist.
     * @param logger
     * @param cpsFramework
     * @param fileSystem
     * @return
     * @throws FrameworkException
     */
    URI locateCredentialsStore(
        Log logger,
        Properties overrideProperties,
        IConfigurationPropertyStoreService cpsFramework,
        IFileSystem fileSystem
    ) throws FrameworkException {
        
        URI uriCredentialsStore ;
        // *** Work out the creds uri
        try {
            String credsProperty = overrideProperties.getProperty("framework.credentials.store");
            if((credsProperty != null) && !credsProperty.isEmpty()){
                uriCredentialsStore = new URI(credsProperty);
                logger.debug("Credentials Store (1) is " + uriCredentialsStore.toString());
                createIfMissing(uriCredentialsStore, fileSystem);
                return uriCredentialsStore;
            }

            credsProperty = cpsFramework.getProperty("credentials", "store");
            if((credsProperty != null) && !credsProperty.isEmpty()){
                uriCredentialsStore = new URI(credsProperty);
                logger.debug("Credentials Store (2) is " + uriCredentialsStore.toString());
                createIfMissing(uriCredentialsStore, fileSystem);
                return uriCredentialsStore;
            }
            uriCredentialsStore = Paths.get(this.galasaHome, "credentials.properties").toUri();
            createIfMissing(uriCredentialsStore,fileSystem);
            
        } catch (final Exception e) {
            throw new FrameworkException("Unable to resolve the Credentials Store URI", e);
        }
        logger.debug("Credentials Store (3) is " + uriCredentialsStore.toString());
        return uriCredentialsStore;
    }


    IConfigurationPropertyStoreService initialiseConfigurationPropertyStore(
        Log logger, BundleContext bundleContext ) throws FrameworkException,InvalidSyntaxException {

        logger.trace("Searching for CPS providers");
        final ServiceReference<?>[] cpsServiceReference = bundleContext
                .getAllServiceReferences(IConfigurationPropertyStoreRegistration.class.getName(), null);
        if ((cpsServiceReference == null) || (cpsServiceReference.length == 0)) {
            throw new FrameworkException("No Configuration Property Store Services have been found");
        }
        for (final ServiceReference<?> cpsReference : cpsServiceReference) {
            final IConfigurationPropertyStoreRegistration cpsStoreRegistration = (IConfigurationPropertyStoreRegistration) bundleContext
                    .getService(cpsReference);
            logger.trace("Found CPS Provider " + cpsStoreRegistration.getClass().getName());
            // Call out to the cpsStoreRegistration. 
            // We expect it to call back on the IFrameworkInitialisation.registerConfigurationPropertyStore call
            // to set the CPS object into the this.framework, so it can be retrieved by the 
            // this.framework.getConfigurationPropertyStore() call in a bit...
            cpsStoreRegistration.initialise(this);
        }
        if (this.framework.getConfigurationPropertyStore() == null) {
            throw new FrameworkException("Failed to initialise a Configuration Property Store, unable to continue");
        }
        logger.debug(
                "Selected CPS Service is " + this.framework.getConfigurationPropertyStore().getClass().getName());

        return this.framework.getConfigurationPropertyService("framework");
    }


    IDynamicStatusStoreService initialiseDynamicStatusStore(Log logger, BundleContext bundleContext ) throws InvalidSyntaxException, FrameworkException {

        logger.trace("Searching for DSS providers");
        final ServiceReference<?>[] dssServiceReference = bundleContext
                .getAllServiceReferences(IDynamicStatusStoreRegistration.class.getName(), null);
        if ((dssServiceReference == null) || (dssServiceReference.length == 0)) {
            throw new FrameworkException("No Dynamic Status Store Services have been found");
        }
        for (final ServiceReference<?> dssReference : dssServiceReference) {
            final IDynamicStatusStoreRegistration dssStoreRegistration = (IDynamicStatusStoreRegistration) bundleContext
                    .getService(dssReference);
            logger.trace("Found DSS Provider " + dssStoreRegistration.getClass().getName());
            // Some magic here: The dssStoreRegistration calls us back into the register the dss store,
            // which gets put into the Framework object, so when we call framework.getDynamicStatusStoreService()
            // it isn't null !
            dssStoreRegistration.initialise(this);
        }
        if (this.framework.getDynamicStatusStore() == null) {
            throw new FrameworkException("Failed to initialise a Dynamic Status Store, unable to continue");
        }
        logger.trace("Selected DSS Service is " + this.framework.getDynamicStatusStore().getClass().getName());
        
        return this.framework.getDynamicStatusStoreService("framework");
    }  


    private void initialiseResultsArchiveStore(Log logger, BundleContext bundleContext) throws FrameworkException, InvalidSyntaxException {
        this.logger.trace("Searching for RAS providers");
        final ServiceReference<?>[] rasServiceReference = bundleContext
                .getAllServiceReferences(IResultArchiveStoreRegistration.class.getName(), null);
        if ((rasServiceReference == null) || (rasServiceReference.length == 0)) {
            throw new FrameworkException("No Result Archive Store Services have been found");
        }
        for (final ServiceReference<?> rasReference : rasServiceReference) {
            final IResultArchiveStoreRegistration rasRegistration = (IResultArchiveStoreRegistration) bundleContext
                    .getService(rasReference);
            if (rasRegistration == null) {
                // Something went wrong. Can't find a RAS service to use.
                FrameworkException ex = new FrameworkException( new Exception("Unable to find RAS provider."));
                logger.error("Unable to find RAS provider.", ex);
                throw ex ;
            } else {
                logger.trace("Found RAS Provider " + rasRegistration.getClass().getName());
                // Magic here: The ras Registration calls back to this.registerResultArchiveStoreService()
                // which in turn sets the value into the framework, so that 
                // ramework.getResultArchiveStoreService() returns non-null.
                rasRegistration.initialise(this);
            }
        }
        if (this.framework.getResultArchiveStoreService() == null) {
            throw new FrameworkException("Failed to initialise a Result Archive Store, unable to continue");
        }
        logger.trace("Selected RAS Service is " + this.framework.getResultArchiveStoreService().getClass().getName());
    }


    private void initialiseCredentialsStore(
        Log logger, 
        BundleContext bundleContext 
    ) throws FrameworkException, InvalidSyntaxException {

        // *** Initialise the Credentials Store
        logger.trace("Searching for Creds providers");
        final ServiceReference<?>[] credsServiceReference = bundleContext
                .getAllServiceReferences(ICredentialsStoreRegistration.class.getName(), null);
        if ((credsServiceReference == null) || (credsServiceReference.length == 0)) {
            throw new FrameworkException("No Credentials Services have been found");
        }
        for (final ServiceReference<?> credsReference : credsServiceReference) {
            final ICredentialsStoreRegistration credsRegistration = (ICredentialsStoreRegistration) bundleContext
                    .getService(credsReference);
            logger.trace("Found Creds Provider " + credsRegistration.getClass().getName());
            // Magic happens here; The registration code calls back here to registerCredentialsStore()
            // which in turn pushes that to the framework, so later on, 
            // framework.getCredentialsStore() returns non-null.
            credsRegistration.initialise(this);
        }
        if (this.framework.getCredentialsStore() == null) {
            throw new FrameworkException("Failed to initialise a Credentials Store, unable to continue");
        }
        logger.trace("Selected Credentials Service is " + this.framework.getCredentialsStore().getClass().getName());
    }


    void initialiseConfidentialTextService(
        Log logger, 
        BundleContext bundleContext 
    ) throws FrameworkException, InvalidSyntaxException {

        // *** Initialise the Confidential Text Service
        logger.trace("Searching for Confidential Text Service providers");
        final ServiceReference<?>[] confidentialServiceReference = bundleContext
                .getAllServiceReferences(IConfidentialTextServiceRegistration.class.getName(), null);
        if ((confidentialServiceReference == null) || (confidentialServiceReference.length == 0)) {
            throw new FrameworkException("No Confidential Text Services have been found");
        }
        for (final ServiceReference<?> confidentialReference : confidentialServiceReference) {
            final IConfidentialTextServiceRegistration credsRegistration = (IConfidentialTextServiceRegistration) bundleContext
                    .getService(confidentialReference);
            logger.trace("Found Confidential Text Services Provider " + credsRegistration.getClass().getName());
            // Magic happens here; The registration code calls back here to registerConfidentialTextstore()
            // which in turn pushes that to the framework, so later on, 
            // framework.getConfidentialTextService returns non-null.
            credsRegistration.initialise(this);
        }
        if (this.framework.getConfidentialTextService() == null) {
            throw new FrameworkException("Failed to initialise a Confidential Text Services, unable to continue");
        }
        logger.trace("Selected Confidential Text Service is "
                + this.framework.getConfidentialTextService().getClass().getName());

    }

    void initialiseEventsService(
        Log logger,
        BundleContext bundleContext
    ) throws FrameworkException, InvalidSyntaxException {

        // *** Initialise the Events Service
        logger.trace("Searching for Events Service providers");
        final ServiceReference<?>[] eventServiceReference = bundleContext
            .getAllServiceReferences(IEventsServiceRegistration.class.getName(), null);

        if ((eventServiceReference == null) || (eventServiceReference.length == 0)) {
            throw new FrameworkException("No Events Services have been found");
        }
        for (final ServiceReference<?> eventsReference : eventServiceReference) {
            final IEventsServiceRegistration eventsRegistration = (IEventsServiceRegistration) bundleContext
                .getService(eventsReference);
            logger.trace("Found Events Services Provider " + eventsRegistration.getClass().getName());
            // Magic happens here; The registration code calls back here to registerEventsService()
            // which in turn pushes that to the framework, so later on, 
            // framework.getEventsService() returns non-null.
            eventsRegistration.initialise(this);
        }
        // The Events Service should not be mandatory so okay to not have one registered
        if (this.framework.getEventsService() != null) {
            logger.trace("Selected Events Service is " 
            + this.framework.getEventsService().getClass().getName());

        }
    }
}