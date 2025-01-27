/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.net.URL;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.*;
import dev.galasa.framework.spi.auth.IAuthStore;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.AuthStoreException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.ServiceScope;

import dev.galasa.framework.internal.auth.FrameworkAuthStoreService;
import dev.galasa.framework.internal.cps.FrameworkConfigurationPropertyService;
import dev.galasa.framework.internal.creds.FrameworkCredentialsService;
import dev.galasa.framework.internal.dss.FrameworkDynamicStatusStoreService;
import dev.galasa.framework.internal.ras.FrameworkMultipleResultArchiveStore;
import dev.galasa.framework.internal.rbac.RBACServiceImpl;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.creds.ICredentialsStore;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;

// I know that the IFramework class isn't strictly necessary, but it does seem to make a
// difference to whether the OSGi framework can load it or not.
// Something about it only looking for implementations of IFramework explicitly, rather than
// looking for all the things which are super-interfaces of IFramework also.
// So we leave in the IFramework below, and supress any warning we might get.
@SuppressWarnings("unused")
@Component(immediate = true, scope = ServiceScope.SINGLETON)
public class Framework implements IFramework, IShuttableFramework {

    private final static Log                   logger           = LogFactory.getLog(Framework.class);

    private static final Pattern               namespacePattern = Pattern.compile("[a-z0-9]+");
    private static final String                ERROR_MESSAGE_TEMPLATE_NAMESPACE_INVALID_CHARACTERS =
        "Invalid namespace '%s'. Valid namespaces are 1 or more characters of 'a'-'z' and '0'-'9'.";

    private Properties                         overrideProperties;
    private final Properties                   recordProperties = new Properties();

    private IConfigurationPropertyStore        cpsStore;
    private IDynamicStatusStore                dssStore;
    private IResultArchiveStoreService         rasService;
    private IConfidentialTextService           ctsService;
    private ICredentialsStore                  credsStore;
    private IEventsService                     eventsService;
    private IAuthStore                         authStore;
    private RBACService                        rbacService;

    private IConfigurationPropertyStoreService cpsFramework;
    @SuppressWarnings("unused")
    private ICredentialsService                credsFramework;

    private String                             runName;

    private final Random                       random;

    private FrameworkRuns                      frameworkRuns;

    private TestRunLogCapture                  testRunLogCapture;

    private IRun                               run;

    private Environment env ;

    public Framework() {
        this( new SystemEnvironment() );
    }

    protected Framework(Environment env) {
        this.env = env ;
        this.random = new Random();
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        logger.info("Framework service activated");
        logger.info("Framework version = " + FrameworkVersion.getBundleVersion());
        logger.info("Framework build   = " + FrameworkVersion.getBundleBuild());
        try {
            bundleContext.addServiceListener(new ManagerServiceListener(), "(objectClass=dev.galasa.framework.spi.IManager)");
        } catch (InvalidSyntaxException e) {
            logger.error("Unable to add ManagerServiceListener", e);
        }
    }

    @Deactivate
    public void deactivate() {
        if (this.testRunLogCapture != null) {
            this.testRunLogCapture.shutdown();
        }
        logger.info("Framework service deactivated");
    }

    public void setFrameworkProperties(Properties overridesProperties) {
        this.overrideProperties = overridesProperties;
    }

    @Override
    public boolean isInitialised() {
        if (cpsStore != null && dssStore != null && rasService != null && ctsService != null && credsStore != null) {
            return true;
        }

        return false;
    }

    public boolean isShutdown() {
        if (cpsStore == null && dssStore == null && rasService == null && ctsService == null && credsStore == null) {
            return true;
        }

        return false;
    }

    @Override
    public @NotNull IConfigurationPropertyStoreService getConfigurationPropertyService(@NotNull String namespace)
            throws ConfigurationPropertyStoreException {
        if (this.cpsStore == null) {
            throw new ConfigurationPropertyStoreException("The Configuration Property Store has not been initialised");
        }

        try {
            validateNamespace(namespace);
        } catch (FrameworkException e) {
            throw new ConfigurationPropertyStoreException("Unable to provide Configuration Property Store", e);
        }

        return new FrameworkConfigurationPropertyService(this, this.cpsStore, this.overrideProperties,
                this.recordProperties, namespace);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * dev.galasa.framework.spi.IFramework#getDynamicStatusStore(java.lang.String)
     */
    @Override
    public @NotNull IDynamicStatusStoreService getDynamicStatusStoreService(@NotNull String namespace)
            throws DynamicStatusStoreException {
        if (this.dssStore == null) {
            throw new DynamicStatusStoreException("The Dynamic Status Store has not been initialised");
        }

        try {
            validateNamespace(namespace);
        } catch (FrameworkException e) {
            throw new DynamicStatusStoreException("Unable to provide Dynamic Status Store", e);
        }

        return new FrameworkDynamicStatusStoreService(this, this.dssStore, namespace);
    }

    /**
     * Validate the namespace adheres to naming standards
     *
     * @param namespace - the namespace to check
     * @throws ConfigurationPropertyStoreException - if the namespace does meet the
     *                                             standards
     */
    private void validateNamespace(String namespace) throws FrameworkException {
        if (namespace == null) {
            throw new FrameworkException( "Namespace has not been provided");
        }

        final Matcher matcher = namespacePattern.matcher(namespace);
        if (!matcher.matches()) {
            throw new FrameworkException( String.format(ERROR_MESSAGE_TEMPLATE_NAMESPACE_INVALID_CHARACTERS,namespace));

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IFramework#getResultArchiveStore()
     */
    @Override
    public @NotNull IResultArchiveStore getResultArchiveStore() {
        return this.rasService;
    }

    protected IResultArchiveStoreService getResultArchiveStoreService() {
        return this.rasService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IFramework#getResourcePoolingService()
     */
    @Override
    public @NotNull IResourcePoolingService getResourcePoolingService() {
        return new FrameworkResourcePoolingService();
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IFramework#getConfidentialTextService()
     */
    @Override
    public @NotNull IConfidentialTextService getConfidentialTextService() {
        return this.ctsService;
    }

    @Override
    public @NotNull ICredentialsService getCredentialsService() throws CredentialsException {
        if (this.credsStore == null) {
            throw new CredentialsException("The Credentials Store has not been initialised");
        }

        return new FrameworkCredentialsService(this, this.credsStore);
    }

    public IEventsService getEventsService() {
        return this.eventsService;
    }

    /**
     * Set the new Configuration Property Store Service
     *
     * @param cpsStore - The new CPS
     * @throws ConfigurationPropertyStoreException - If a CPS has already be
     *                                             registered
     */
    protected void setConfigurationPropertyStore(@NotNull IConfigurationPropertyStore cpsStore)
            throws ConfigurationPropertyStoreException {
        if (this.cpsStore != null) {
            throw new ConfigurationPropertyStoreException(
                    "Invalid 2nd registration of the Config Property Store Service detected");
        }

        this.cpsStore = cpsStore;
        this.cpsFramework = getConfigurationPropertyService("framework");
    }

    public void setDynamicStatusStore(@NotNull IDynamicStatusStore dssStore) throws DynamicStatusStoreException {
        if (this.dssStore != null) {
            throw new DynamicStatusStoreException(
                    "Invalid 2nd registration of the Dynamic Status Store Service detected");
        }

        this.dssStore = dssStore;
    }

    public void setAuthStore(@NotNull IAuthStore authStore) throws AuthStoreException {
        if (this.authStore != null) {
            throw new AuthStoreException("Invalid second registration of the Auth Store Service detected");
        }

        this.authStore = authStore;
    }

    /**
     * Add a new Result Archive Store Service to the framework, eventually we will
     * have the ability to have multiples RASs running
     *
     * @param resultArchiveStoreService - a new result archive store service
     * @throws ResultArchiveStoreException
     */
    public void addResultArchiveStoreService(@NotNull IResultArchiveStoreService resultArchiveStoreService)
            throws ResultArchiveStoreException {

        if (this.rasService == null) {
            this.rasService = resultArchiveStoreService;
            return;
        }

        if (this.rasService instanceof FrameworkMultipleResultArchiveStore) {
            ((FrameworkMultipleResultArchiveStore) this.rasService)
                    .addResultArchiveStoreService(resultArchiveStoreService);
            return;
        }

        this.rasService = new FrameworkMultipleResultArchiveStore(this, this.rasService);
        ((FrameworkMultipleResultArchiveStore) this.rasService).addResultArchiveStoreService(resultArchiveStoreService);
    }

    public void setEventsService(@NotNull IEventsService eventsService) throws EventsException {
        if (this.eventsService != null) {
            throw new EventsException("Invalid 2nd registration of the Events Service detected");
        }
        this.eventsService = eventsService;
    }

    public void setConfidentialTextService(@NotNull IConfidentialTextService confidentialTextService)
            throws ConfidentialTextException {
        if (this.ctsService != null) {
            throw new ConfidentialTextException("Invalid 2nd registration of the Confidential Text Service detected");
        }
        this.ctsService = confidentialTextService;
    }

    public void setCredentialsStore(@NotNull ICredentialsStore credsStore) throws CredentialsException {
        if (this.credsStore != null) {
            throw new CredentialsException("Invalid 2nd registration of the Credentials Store Service detected");
        }
        this.credsStore = credsStore;
    }

    /**
     * Retrieve the active CPS Service
     *
     * @return The CPS Service
     */
    protected IConfigurationPropertyStore getConfigurationPropertyStore() {
        return this.cpsStore;
    }

    /**
     * Retrieve the active DSS Service
     *
     * @return The DSS service
     */
    protected IDynamicStatusStore getDynamicStatusStore() {
        return this.dssStore;
    }

    protected ICredentialsStore getCredentialsStore() {
        return this.credsStore;
    }


    @Override
    public IAuthStore getAuthStore() {
        return this.authStore;
    }

    @Override
    public IAuthStoreService getAuthStoreService() {
        return new FrameworkAuthStoreService(authStore);
    }

    @Override
    public Random getRandom() {
        return this.random;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IFramework#getTestRunName()
     */
    @Override
    public String getTestRunName() {
        return this.runName;
    }

    /**
     * Set the run name if it is a test run
     * 
     * @param runName The run name
     * @throws FrameworkException
     */
    public void setTestRunName(String runName) throws FrameworkException {
        this.runName = runName;

        this.run = getFrameworkRuns().getRun(runName);
    }

    @Override
    public IRun getTestRun() {
        return this.run;
    }

    @Override
    public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
        if (this.frameworkRuns == null) {
            this.frameworkRuns = new FrameworkRuns(this);
        }

        return this.frameworkRuns;
    }

    @Override
    public Properties getRecordProperties() {
        Properties clone = (Properties) this.recordProperties.clone();
        return clone;
    }

    public void installLogCapture() {
        if (this.testRunLogCapture != null) {
            return;
        }

        this.testRunLogCapture = new TestRunLogCapture(this);

    }

    @Override
    public void shutdown() throws FrameworkException {
        shutdown(null);
    }

    public void shutdown(Log shutdownLogger) throws FrameworkException {
        if (isShutdown()) {
            return;
        }

        if (shutdownLogger == null) {
            shutdownLogger = logger;
        }

        boolean error = false;

        shutdownLogger.info("Shutting down the framework");

        // *** Shutdown the Events Service
        if (this.eventsService != null) {
            try {
                shutdownLogger.trace("Shutting down the Events Service");
                this.eventsService.shutdown();
                this.eventsService = null;
            } catch (Throwable t) {
                error = true;
                shutdownLogger.error("Failed to shutdown the Events Service", t);
            }
        }

        // *** Shutdown the Confidential Text Service
        if (this.ctsService != null) {
            try {
                shutdownLogger.trace("Shutting down the Confidential Text Service");
                this.ctsService.shutdown();
                this.ctsService = null;
            } catch (Throwable t) {
                error = true;
                shutdownLogger.error("Failed to shutdown the Confidential Text Service", t);
            }
        }

        // *** Shutdown the Credentials Service
        if (this.credsStore != null) {
            try {
                shutdownLogger.trace("Shutting down the Credentials Service");
                this.credsStore.shutdown();
                this.credsStore = null;
            } catch (Throwable t) {
                error = true;
                shutdownLogger.error("Failed to shutdown the Credentials Service", t);
            }
        }

        // *** Shutdown the Result Archive Store
        if (this.rasService != null) {
            try {
                shutdownLogger.trace("Shutting down the Result Archive Store");
                this.rasService.shutdown();
                this.rasService = null;
            } catch (Throwable t) {
                error = true;
                shutdownLogger.error("Failed to shutdown the Result Archive Store", t);
            }
        }

        // *** Shutdown the Dynamic Status Store
        if (this.dssStore != null) {
            try {
                shutdownLogger.trace("Shutting down the Dynamic Status Store");
                this.dssStore.shutdown();
                this.dssStore = null;
            } catch (Throwable t) {
                error = true;
                shutdownLogger.error("Failed to shutdown the Dynamic Status Store", t);
            }
        }

        // *** Shutdown the Configuration Property Store
        if (this.cpsStore != null) {
            try {
                shutdownLogger.trace("Shutting down the Configuration Properties Store");
                this.cpsStore.shutdown();
                this.cpsStore = null;
            } catch (Throwable t) {
                error = true;
                shutdownLogger.error("Failed to shutdown the Configuration Property Store", t);
            }
        }

        // *** All done
        if (error) {
            throw new FrameworkException("Shutdown did not complete successfully, see log");
        } else {
            shutdownLogger.info("Framework shutdown");
        }
    }

    @Override
    public URL getApiUrl(Api api) throws FrameworkException {
        if (api == null) {
            throw new FrameworkException("api has not been provided");
        }

        try {
            String urlProperty = AbstractManager.nulled(cpsFramework.getProperty(api.getProperty(), "url"));

            if (urlProperty != null) {
                return new URL(urlProperty);
            }

            String bootstrapProperty = AbstractManager.nulled(cpsFramework.getProperty("bootstrap", "url"));
            if (bootstrapProperty == null) {
                throw new FrameworkException("Unable to derive the URL for api " + api
                        + " as the framework.bootstrap.url property is missing");
            }
            if (!bootstrapProperty.endsWith("/bootstrap")) {
                throw new FrameworkException("Unable to derive the URL for api " + api
                        + " as the framework.bootstrap.url property does not end with /bootstrap");
            }

            urlProperty = bootstrapProperty.substring(0, bootstrapProperty.length() - 10) + "/" + api.getSuffix();

            return new URL(urlProperty);
        } catch (Exception e) {
            throw new FrameworkException("Unable to determine URL of API " + api, e);
        }
    }

    @Override
    public SharedEnvironmentRunType getSharedEnvironmentRunType() throws ConfigurationPropertyStoreException {
        String sePhase = AbstractManager.nulled(cpsFramework.getProperty("run","shared.environment.phase"));
        if (sePhase == null) {
            return null;
        }
        
        switch(sePhase) {
            case "BUILD":
                return SharedEnvironmentRunType.BUILD;
            case "DISCARD":
                return SharedEnvironmentRunType.DISCARD;
            default:
                return null;
        }
    }

	@Override
	public @NotNull ICertificateStoreService getCertificateStoreService() {
		return null;
	}

    @Override
    public RBACService getRBACService() throws RBACException {
        if (this.rbacService == null) {
            try {
                IDynamicStatusStoreService dssService = getDynamicStatusStoreService("rbac");
                this.rbacService = new RBACServiceImpl(dssService, getAuthStoreService(), this.env);
            } catch (DynamicStatusStoreException e) {
                throw new RBACException("Failed to initialise Role-Based Access Control service", e);
            }
        }
        return this.rbacService;
    }

}