/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractGherkinManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IGherkinManager;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.textscan.spi.ITextScannerManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.IZosManager;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zos3270.ITerminal;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.Zos3270ManagerException;
import dev.galasa.zos3270.Zos3270Terminal;
import dev.galasa.zos3270.common.screens.TerminalSize;
import dev.galasa.zos3270.internal.gherkin.Gherkin3270Coordinator;
import dev.galasa.zos3270.internal.properties.ExtraBundles;
import dev.galasa.zos3270.internal.properties.Zos3270PropertiesSingleton;
import dev.galasa.zos3270.spi.IZos3270ManagerSpi;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.zos3270.spi.Zos3270TerminalImpl;

@Component(service = { IManager.class, IGherkinManager.class })
public class Zos3270ManagerImpl extends AbstractGherkinManager implements IZos3270ManagerSpi {
    protected static final String                       NAMESPACE     = "zos3270";

    private static final Log                            logger        = LogFactory.getLog(Zos3270ManagerImpl.class);

    private IDynamicStatusStoreService                  dss;

    private IZosManagerSpi                              zosManager;
    private ITextScannerManagerSpi						textScannerManager;

    private ArrayList<Zos3270TerminalImpl>              terminals     = new ArrayList<>();

    private int                                         terminalCount = 0;
    
    private Gherkin3270Coordinator                      gherkinCoordinator;

    /*
     * (non-Javadoc)
     * 
     * @see
     * dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.
     * IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);

        if(galasaTest.isJava()) {
            // *** Check to see if any of our annotations are present in the test class
            // *** If there is, we need to activate
            List<AnnotatedField> ourFields = findAnnotatedFields(Zos3270ManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers,galasaTest);
            }
        } else if (galasaTest.isGherkin()) {
            this.gherkinCoordinator = new Gherkin3270Coordinator(this, galasaTest.getGherkinTest());
            if (this.gherkinCoordinator.registerStatements()) {
                youAreRequired(allManagers, activeManagers,galasaTest);
            }
        }

        try {
            this.dss = framework.getDynamicStatusStoreService(NAMESPACE);
            Zos3270PropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (Exception e) {
            throw new Zos3270ManagerException("Unable to request framework services", e);
        }

    }

    @Override
    public Result endOfTestMethod(@NotNull GalasaMethod galasaMethod, @NotNull Result currentResult, Throwable currentException)
            throws ManagerException {

        super.endOfTestMethod(galasaMethod, currentResult, currentException);

        if (galasaMethod.isGherkin()) {
            // The end of a test method in gherkin equates to the end of the scenario.
            // So we need to free up terminals so their state doesn't leech into the next scenario.
            // A scenario equates to a java method.
            disconnectAllTerminals();
        }

        return currentResult;
    }

    private void disconnectAllTerminals() throws Zos3270ManagerException {
        for( Zos3270TerminalImpl terminal: terminals) {
            if (terminal.isConnected()) {
                disconnectTerminal(terminal);
            }
        }
    }

    @Override
    public List<String> extraBundles(@NotNull IFramework framework) throws ManagerException {
        try {
        	Zos3270PropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new Zos3270ManagerException("Unable to request framework services", e);
        }

        return ExtraBundles.get();
    }

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        zosManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosManagerSpi.class);
        if (zosManager == null) {
            throw new Zos3270ManagerException("The zOS Manager is not available");
        }
        
        textScannerManager = addDependentManager(allManagers, activeManagers, galasaTest, ITextScannerManagerSpi.class);
        if (textScannerManager == null) {
        	throw new Zos3270ManagerException("The Text Scanner Manager is not available");
        }
    }

    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        return (otherManager instanceof IZosManager) || (otherManager instanceof ITextScannerManagerSpi);
    }

    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        if (this.gherkinCoordinator != null) {
            this.gherkinCoordinator.provisionGenerate();
            return;
        }
        
        // *** Auto generate the fields
        generateAnnotatedFields(Zos3270ManagerField.class);
    }

    @GenerateAnnotatedField(annotation = Zos3270Terminal.class)
    public ITerminal generateTerminal(Field field, List<Annotation> annotations) throws Zos3270ManagerException {
        Zos3270Terminal terminalAnnotation = field.getAnnotation(Zos3270Terminal.class);

        // *** Default the tag to primary
        String tag = defaultString(terminalAnnotation.imageTag(), "PRIMARY").toUpperCase();
        // *** Default the tag to primary
        boolean autoConnect = terminalAnnotation.autoConnect();

        TerminalSize primaryTerminalSize   = new TerminalSize(terminalAnnotation.primaryColumns(), terminalAnnotation.primaryRows());
        TerminalSize alternateTerminalSize = new TerminalSize(terminalAnnotation.alternateColumns(), terminalAnnotation.alternateRows());
        
        return generateTerminal(tag, autoConnect, primaryTerminalSize, alternateTerminalSize);
    }
    
    public Zos3270TerminalImpl generateTerminal(String imageTag, boolean autoConnect, TerminalSize primarySize, TerminalSize alternateSize) throws Zos3270ManagerException {
        // *** Ask the zosManager for the image for the Tag
        try {
            IZosImage image = this.zosManager.provisionImageForTag(imageTag);
            IIpHost host = image.getIpHost();

            terminalCount++;
            String terminaId = "term" + (terminalCount);

            Zos3270TerminalImpl terminal = new Zos3270TerminalImpl(terminaId, host.getHostname(), host.getTelnetPort(),
                    host.isTelnetPortTls(), getFramework(), autoConnect, image, primarySize, alternateSize, textScannerManager);
            
            this.terminals.add(terminal);
            logger.info("Generated a terminal for zOS Image tagged " + imageTag);

            return terminal;
        } catch (Exception e) {
            throw new Zos3270ManagerException("Unable to generate Terminal for zOS Image tagged " + imageTag, e);
        }
    }
    
    @Override
    public void provisionStart() throws ManagerException, ResourceUnavailableException {
        if (terminals.isEmpty()) {
            return;
        }

        logger.info("Connecting zOS3270 Terminals");
        for (Zos3270TerminalImpl terminal : terminals) {
            try {
                if (terminal.doAutoConnect()) {
                    terminal.connect();
                    logger.trace("Connected zOS 3270 Terminal " + terminal.getId());
                } else {
                    logger.trace("AutoConnect flag is false for: " + terminal.getId());
                }
                
            } catch (NetworkException e) {
                logger.info("Failed to connect zOS 3270 Terminal to " + terminal.getHostPort(), e);
            }
        }
    }

    @Override
    public void provisionStop() {
        logger.trace("Disconnecting terminals");
        for (Zos3270TerminalImpl terminal : terminals) {
            disconnectTerminal(terminal);
        }
    }

    private void disconnectTerminal(Zos3270TerminalImpl terminal) {
        String terminalId = terminal.getId();
        logger.info("Disconnecting terminal "+terminalId);
        try {
            terminal.writeRasOutput();
            terminal.flushTerminalCache();
            terminal.disconnect();
        } catch (TerminalInterruptedException e) {
            logger.warn("Thread interrupted whilst disconnecting terminals", e);
            Thread.currentThread().interrupt();
        }
    }

    protected IConfigurationPropertyStoreService getCps() throws Zos3270ManagerException {
        return Zos3270PropertiesSingleton.cps();
    }

    protected IDynamicStatusStoreService getDss() {
        return this.dss;
    }

    public IZosManagerSpi getZosManager() {
        return this.zosManager;
    }

    /**
     * Get a CPS property from the zos3270 namespace.
     * 
     * The Gherkin sister-classes need to be able to retrieve properties from the CPS.
     *
     * @param fullPropertyName the name of the property you want. Including the namespace, which must
     * match {@link Zos3270ManagerImpl#NAMESPACE}
     */
    public String getCpsProperty(String fullPropertyName) throws Zos3270ManagerException {

        String propertyValue;

        if (!fullPropertyName.startsWith(Zos3270ManagerImpl.NAMESPACE+".")) {
            // This manager can only get properties from the zos3270 namespace.
            throw new Zos3270ManagerException(
                "Program logic error. CPS property name must start with '"+Zos3270ManagerImpl.NAMESPACE+".' for the Zos3270 manager to access it."+
                " Property"+fullPropertyName+" cannot be retrieved.");
        }

        try {

            // We get something like "zos3270.gherkin.terminal.rows" as input.
            // The cps we are using is already pinned to the zos3270 namespace, so we don't need to 
            // pass that. It is implicitly given.


            String[] propNameParts = fullPropertyName.split("\\.");
            // Skip the namespace zos3270 part.
            String prefix = propNameParts[1]; 
            String suffix = propNameParts[propNameParts.length-1]; 
            // allocate space for the infixes.
            String [] infixes = new String[propNameParts.length-3];
            System.arraycopy( propNameParts, 2, infixes, 0, propNameParts.length-3 );

            propertyValue = getCps().getProperty(prefix, suffix, infixes);
            logger.info("Property requested:"+fullPropertyName+" value:"+propertyValue);

        } catch (ConfigurationPropertyStoreException ex) {
            throw new Zos3270ManagerException("Failed to retrieve the CPS property "+fullPropertyName , ex );
        }

        return propertyValue;
    }

}
