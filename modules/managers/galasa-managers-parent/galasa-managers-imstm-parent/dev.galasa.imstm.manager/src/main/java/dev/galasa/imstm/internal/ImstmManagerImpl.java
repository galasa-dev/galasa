/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.imstm.IImsSystem;
import dev.galasa.imstm.IImsTerminal;
import dev.galasa.imstm.ImsSystem;
import dev.galasa.imstm.ImsTerminal;
import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.imstm.ImstmManagerField;
import dev.galasa.imstm.internal.dse.DseProvisioningImpl;
import dev.galasa.imstm.spi.IImsSystemLogonProvider;
import dev.galasa.imstm.spi.IImsSystemProvisioner;
import dev.galasa.imstm.spi.IImstmManagerSpi;
import dev.galasa.imstm.spi.ImsTerminalImpl;
import dev.galasa.textscan.spi.ITextScannerManagerSpi;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zos3270.TerminalInterruptedException;

@Component(service = { IManager.class })
public class ImstmManagerImpl extends AbstractManager implements IImstmManagerSpi {
    protected static final String NAMESPACE = "imstm";

    private static final Log logger = LogFactory.getLog(ImstmManagerImpl.class);
    private boolean isManagerRequired = false;
    private ImstmProperties properties; 

    private IZosManagerSpi zosManager;
    private ITextScannerManagerSpi textScanner;

    private final HashMap<String, IImsSystem> provisionedImsSystems = new HashMap<>();

    private final ArrayList<IImsSystemProvisioner> provisioners = new ArrayList<>();
    private final ArrayList<ImsTerminalImpl> terminals = new ArrayList<>();
    private final ArrayList<IImsSystemLogonProvider> logonProviders = new ArrayList<>();
    private final Map<IImsSystem, Integer> lastTerminalId = new HashMap<IImsSystem, Integer>(); 

    private String provisionType;  // Obtained from the imstm.provision.type CPS property
    
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        // Check to see if any of our annotations are present in the test class
        // If there is, we need to activate
        if(galasaTest.isJava()) {
            List<AnnotatedField> ourFields = findAnnotatedFields(ImstmManagerField.class);
            if (ourFields.isEmpty() && !isManagerRequired) {
                return;
            }

            youAreRequired(allManagers, activeManagers, galasaTest);
        }
    }

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        this.isManagerRequired = true;
        activeManagers.add(this);

        this.zosManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosManagerSpi.class);
        if (this.zosManager == null) {
            throw new ImstmManagerException("Unable to locate the zOS Manager, required for the IMS TM Manager");
        }
        this.textScanner = addDependentManager(allManagers, activeManagers, galasaTest, ITextScannerManagerSpi.class);
        if (this.textScanner == null) {
            throw new ImstmManagerException("The Text Scanner Manager is not available");
        }

        this.provisionType = properties.getProvisionType();
        this.provisioners.add(new DseProvisioningImpl(this, properties));
    }

    @Override
    public List<String> extraBundles(@NotNull IFramework framework) throws ManagerException {
        try {
            properties = new ImstmProperties(framework);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ImstmManagerException("Unable to request framework services", e);
        }

        return Collections.emptyList();
    }

    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        // We need zos to provision first
        if (this.zosManager == otherManager) { // NOSONAR - ignore return single statement rule as will prob need other managers soon
            return true;
        }

        return false;
    }

    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        // First, give the provisioners the opportunity to provision IMS systems
        for (IImsSystemProvisioner provisioner : provisioners) {
            provisioner.imsProvisionGenerate();
        }

        // Now provision all the individual annotations 

        List<AnnotatedField> annotatedFields = findAnnotatedFields(ImstmManagerField.class);

        for (AnnotatedField annotatedField : annotatedFields) {
            final Field field = annotatedField.getField();

            if (field.getType() == IImsSystem.class) {
                ImsSystem annotation = field.getAnnotation(ImsSystem.class);
                if (annotation != null) {
                    IImsSystem imsSystem = generateImsSystem(field, annotatedField.getAnnotations());
                    registerAnnotatedField(field, imsSystem);
                }
            }
        }

        // *** Auto generate the fields
        generateAnnotatedFields(ImstmManagerField.class);
    }

    /**
     * Not using the auto generate as we need all the IMS Systems generated before
     * any other annotated field
     */
    public IImsSystem generateImsSystem(Field field, List<Annotation> annotations) throws ManagerException {
        ImsSystem annotationIms = field.getAnnotation(ImsSystem.class);

        String tag = defaultString(annotationIms.imsTag(), "PRIMARY").toUpperCase();

        // Have we already got it
        IImsSystem system = this.provisionedImsSystems.get(tag);
        if (system != null) {
            return system;
        }

        for (IImsSystemProvisioner provisioner : provisioners) {
            IImsSystem newSystem = provisioner.provision(tag, annotationIms.imageTag(), annotations);
            if (newSystem != null) {
                this.provisionedImsSystems.put(tag, newSystem);
                return newSystem;
            }
        }

        throw new ImstmManagerException("Unable to provision IMS System tagged " + tag);
    }

    @GenerateAnnotatedField(annotation = ImsTerminal.class)
    public IImsTerminal generateImsTerminal(Field field, List<Annotation> annotations) throws ManagerException {
        ImsTerminal annotation = field.getAnnotation(ImsTerminal.class);

        String tag = defaultString(annotation.imsTag(), "PRIMARY").toUpperCase();
        String loginCredentialsTag = defaultString(annotation.loginCredentialsTag(), "").toUpperCase();
        
        IImsSystem system = this.provisionedImsSystems.get(tag);
        if (system == null) {
            throw new ImstmManagerException("Unable to setup IMS Terminal for field '" + field.getName() + "', for system with tag '"
            + tag + "' as a system with a matching 'imsTag' tag was not found, or the system was not provisioned.");
        }

        try {
            ImsTerminalImpl newTerminal = new ImsTerminalImpl(this, getFramework(), system, annotation.connectAtStartup(), this.textScanner, loginCredentialsTag);
            this.terminals.add(newTerminal);
            return newTerminal;
        } catch (TerminalInterruptedException e) {
            throw new ImstmManagerException(
                    "Unable to setup IMS Terminal for field " + field.getName() + ", tagged system " + tag, e);
        }
       
    }
    
    @Override
    public IImsTerminal generateImsTerminal(String tag) throws ImstmManagerException{
    	IImsSystem system = this.provisionedImsSystems.get(tag);
        if (system == null) {
            throw new ImstmManagerException("Unable to setup IMS Terminal for tag " + tag + ", no system was provisioned");
        }

        try {
            ImsTerminalImpl newTerminal = new ImsTerminalImpl(this, getFramework(), system, true, this.textScanner);
            this.terminals.add(newTerminal);
            return newTerminal;
        } catch (TerminalInterruptedException | ManagerException e) {
            throw new ImstmManagerException(
                    "Unable to setup IMS Terminal for tagged system " + tag, e);
        }
    }
    
    @Override
    public IImsSystem locateImsSystem(String tag) throws ImstmManagerException {
    	IImsSystem system = this.provisionedImsSystems.get(tag);
        if (system == null) {
            throw new ImstmManagerException("Unable to locate IMS System for tag " + tag);
        }
        return system;
    }
    
    @Override
    public void provisionBuild() throws ManagerException, ResourceUnavailableException {
        // First, give the provisioners the opportunity to build IMS systems
        for (IImsSystemProvisioner provisioner : provisioners) {
            provisioner.imsProvisionBuild();
        }

    }

    @Override
    public void provisionStart() throws ManagerException, ResourceUnavailableException {
        // Add the default Logon Provider incase one isn't supplied
        this.logonProviders.add(new ImstmDefaultLogonProvider(getFramework()));

        // First, give the provisioners the opportunity to start IMS systems
        for (IImsSystemProvisioner provisioner : provisioners) {
            provisioner.imsProvisionStart();
        }

        // Start the IMS Systems

        // Start the autoconnect terminals - in case they were not started during the above provisioner code
        logger.info("Connecting IMS Terminals");
        for (ImsTerminalImpl terminal : this.terminals) {
            if (terminal.isConnected()) {
                continue;
            }
            
            if (!terminal.isConnectAtStartup()) {
                continue;
            }
            
            if (!terminal.getImsSystem().isProvisionStart()) {
                continue;
            }
            
            try {
                terminal.connectToImsSystem();
            } catch (ImstmManagerException e) {
                throw new ImstmManagerException("Failed to connect to the " + terminal.getImsSystem(), e);
            }
        }
    }

    @Override
    public void provisionStop() {
        for (ImsTerminalImpl terminal : this.terminals) {
            try {
                terminal.writeRasOutput();
            	terminal.flushTerminalCache();
                terminal.disconnect();
            } catch (TerminalInterruptedException e) { // NOSONAR - wish to hide disconnect errors
            }
        }
        
        // Give the provisioners the opportunity to stop IMS systems
        for (IImsSystemProvisioner provisioner : provisioners) {
            provisioner.imsProvisionStop();
        }

    }
    
    @Override
    public void provisionDiscard() {
        // Give the provisioners the opportunity to discard IMS systems
        for (IImsSystemProvisioner provisioner : provisioners) {
            provisioner.imsProvisionDiscard();
        }
    }

    @Override
    public void registerProvisioner(IImsSystemProvisioner provisioner) {
        if (this.provisioners.contains(provisioner)) {
            return;
        }

        this.provisioners.add(provisioner);
    }

    public IZosManagerSpi getZosManager() {
        return this.zosManager;
    }

    public String getProvisionType() {
        return this.provisionType;
    }

    @Override
    @NotNull
    public List<IImsSystemLogonProvider> getLogonProviders() {
        return new ArrayList<>(this.logonProviders);
    }

	@Override
    public void imstmSystemStarted(IImsSystem system) throws ImstmManagerException {
        // A system has started, so connect everything up
        
        // Connect terminals that are associated with the system
        
        for(ImsTerminalImpl terminal : terminals) {
            if (terminal.getImsSystem() == system) {
                if (terminal.isConnectAtStartup()) {
                    terminal.connectToImsSystem();
                }
            }
        }
    }

	@Override
	public Map<String, IImsSystem> getTaggedImsSystems() {
		HashMap<String, IImsSystem> clonedTaggedImsSystems = new HashMap<String, IImsSystem>();
		for(Map.Entry<String, IImsSystem> entry : this.provisionedImsSystems.entrySet()) {
			clonedTaggedImsSystems.put(entry.getKey(), entry.getValue());
		}		
		return clonedTaggedImsSystems;
	}

	@Override
	public List<IImsTerminal> getImsTerminals() {	
		return new ArrayList<>(this.terminals);
	}

    @Override
    public String getNextTerminalId(IImsSystem imsSystem) {
        Integer lastTerminalIdInteger = lastTerminalId.get(imsSystem);
        int lastTerminalIdInt;
        if (lastTerminalIdInteger == null) {
            lastTerminalIdInt = 1;
        } else {
            lastTerminalIdInt = lastTerminalIdInteger.intValue() + 1;
        }

        lastTerminalId.put(imsSystem, Integer.valueOf(lastTerminalIdInt));
        return imsSystem.getApplid() + "_" + Integer.toString(lastTerminalIdInt);
    }
}
