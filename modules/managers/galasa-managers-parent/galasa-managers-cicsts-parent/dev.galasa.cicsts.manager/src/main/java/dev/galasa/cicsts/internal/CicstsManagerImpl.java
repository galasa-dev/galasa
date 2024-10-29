/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.ProductVersion;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicsTerminal;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.CicstsManagerField;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.internal.dse.DseProvisioningImpl;
import dev.galasa.cicsts.internal.properties.CicstsPropertiesSingleton;
import dev.galasa.cicsts.internal.properties.DefaultVersion;
import dev.galasa.cicsts.internal.properties.ExtraBundles;
import dev.galasa.cicsts.internal.properties.ProvisionType;
import dev.galasa.cicsts.spi.CicsTerminalImpl;
import dev.galasa.cicsts.spi.ICeciProvider;
import dev.galasa.cicsts.spi.ICedaProvider;
import dev.galasa.cicsts.spi.ICemtProvider;
import dev.galasa.cicsts.spi.ICicsRegionLogonProvider;
import dev.galasa.cicsts.spi.ICicsRegionProvisioned;
import dev.galasa.cicsts.spi.ICicsRegionProvisioner;
import dev.galasa.cicsts.spi.ICicsResourceProvider;
import dev.galasa.cicsts.spi.ICicstsManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.textscan.spi.ITextScannerManagerSpi;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.spi.IZosBatchSpi;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.spi.IZosFileSpi;

@Component(service = { IManager.class })
public class CicstsManagerImpl extends AbstractManager implements ICicstsManagerSpi {
    protected static final String NAMESPACE = "cicsts";

    private static final Log logger = LogFactory.getLog(CicstsManagerImpl.class);
    private boolean required;

    private IZosManagerSpi zosManager;
    private IZosBatchSpi zosBatchManager;
    private IZosFileSpi zosFileManager;
    private ITextScannerManagerSpi textScanner;

    private final HashMap<String, ICicsRegionProvisioned> provisionedCicsRegions = new HashMap<>();

    private final ArrayList<ICicsRegionProvisioner> provisioners = new ArrayList<>();
    private final ArrayList<CicsTerminalImpl> terminals = new ArrayList<>();
    private final ArrayList<ICicsRegionLogonProvider> logonProviders = new ArrayList<>();

    private String provisionType;
    
    private ICeciProvider ceciProvider;
    private ICedaProvider cedaProvider;
    private ICemtProvider cemtProvider;
    private ICicsResourceProvider cicsResourceProvider;

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        // *** Check to see if any of our annotations are present in the test class
        // *** If there is, we need to activate
        if(galasaTest.isJava()) {
            List<AnnotatedField> ourFields = findAnnotatedFields(CicstsManagerField.class);
            if (ourFields.isEmpty() && !required) {
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

        this.required = true;
        activeManagers.add(this);

        this.zosManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosManagerSpi.class);
        if (this.zosManager == null) {
            throw new CicstsManagerException("Unable to locate the zOS Manager, required for the CICS TS Manager");
        }
        this.zosBatchManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosBatchSpi.class);
        if (this.zosBatchManager == null) {
            throw new CicstsManagerException("The zOS Batch Manager is not available");
        }
        this.zosFileManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosFileSpi.class);
        if (this.zosFileManager == null) {
            throw new CicstsManagerException("The zOS File Manager is not available");
        }
        this.textScanner = addDependentManager(allManagers, activeManagers, galasaTest, ITextScannerManagerSpi.class);
        if (this.textScanner == null) {
            throw new CicstsManagerException("The Text Scanner Manager is not available");
        }

        this.provisionType = ProvisionType.get();
        this.provisioners.add(new DseProvisioningImpl(this));
    }

    @Override
    public List<String> extraBundles(@NotNull IFramework framework) throws ManagerException {
        try {
            CicstsPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new CicstsManagerException("Unable to request framework services", e);
        }

        return ExtraBundles.get();
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
        // First, give the provisioners the opportunity to provision CICS regions
        for (ICicsRegionProvisioner provisioner : provisioners) {
            provisioner.cicsProvisionGenerate();
        }

        // Now provision all the individual annotations 

        List<AnnotatedField> annotatedFields = findAnnotatedFields(CicstsManagerField.class);

        for (AnnotatedField annotatedField : annotatedFields) {
            final Field field = annotatedField.getField();

            if (field.getType() == ICicsRegion.class) {
                CicsRegion annotation = field.getAnnotation(CicsRegion.class);
                if (annotation != null) {
                    ICicsRegion cicsRegion = generateCicsRegion(field, annotatedField.getAnnotations());
                    registerAnnotatedField(field, cicsRegion);
                }
            }
        }

        // *** Auto generate the fields
        generateAnnotatedFields(CicstsManagerField.class);
    }

    /**
     * Not using the auto generate as we need all the CICS Regions generated before
     * any other annotated field
     */
    public ICicsRegion generateCicsRegion(Field field, List<Annotation> annotations) throws ManagerException {
        CicsRegion annotationCics = field.getAnnotation(CicsRegion.class);

        String tag = defaultString(annotationCics.cicsTag(), "PRIMARY").toUpperCase();

        // Have we already got it
        ICicsRegionProvisioned region = this.provisionedCicsRegions.get(tag);
        if (region != null) {
            return region;
        }

        for (ICicsRegionProvisioner provisioner : provisioners) {
            ICicsRegionProvisioned newRegion = provisioner.provision(tag, annotationCics.imageTag(), annotations);
            if (newRegion != null) {
                this.provisionedCicsRegions.put(tag, newRegion);
                return newRegion;
            }
        }

        throw new CicstsManagerException("Unable to provision CICS Region tagged " + tag);
    }

    @GenerateAnnotatedField(annotation = CicsTerminal.class)
    public ICicsTerminal generateCicsTerminal(Field field, List<Annotation> annotations) throws ManagerException {
        CicsTerminal annotation = field.getAnnotation(CicsTerminal.class);

        String tag = defaultString(annotation.cicsTag(), "PRIMARY").toUpperCase();
        String loginCredentialsTag = defaultString(annotation.loginCredentialsTag(), "").toUpperCase();
        
        ICicsRegionProvisioned region = this.provisionedCicsRegions.get(tag);
        if (region == null) {
            throw new CicstsManagerException("Unable to setup CICS Terminal for field '" + field.getName() + "', for region with tag '"
            + tag + "' as a region with a matching 'cicsTag' tag was not found, or the region was not provisioned.");
        }

        try {
            CicsTerminalImpl newTerminal = new CicsTerminalImpl(this, getFramework(), region, annotation.connectAtStartup(), this.textScanner, loginCredentialsTag);
            this.terminals.add(newTerminal);
            return newTerminal;
        } catch (TerminalInterruptedException e) {
            throw new CicstsManagerException(
                    "Unable to setup CICS Terminal for field " + field.getName() + ", tagged region " + tag, e);
        }
       
    }
    
    @Override
    public ICicsTerminal generateCicsTerminal(String tag) throws CicstsManagerException{
    	ICicsRegionProvisioned region = this.provisionedCicsRegions.get(tag);
        if (region == null) {
            throw new CicstsManagerException("Unable to setup CICS Terminal for tag " + tag + ", no region was provisioned");
        }

        try {
            CicsTerminalImpl newTerminal = new CicsTerminalImpl(this, getFramework(), region, true, this.textScanner);
            this.terminals.add(newTerminal);
            return newTerminal;
        } catch (TerminalInterruptedException | ManagerException e) {
            throw new CicstsManagerException(
                    "Unable to setup CICS Terminal for tagged region " + tag, e);
        }
    }
    
    @Override
    public ICicsRegion locateCicsRegion(String tag) throws CicstsManagerException {
    	ICicsRegionProvisioned region = this.provisionedCicsRegions.get(tag);
        if (region == null) {
            throw new CicstsManagerException("Unable to setup CICS Terminal for tag " + tag + ", no region was provisioned");
        }
        return region;
    }
    
    @Override
    public void provisionBuild() throws ManagerException, ResourceUnavailableException {
        // First, give the provisioners the opportunity to build CICS regions
        for (ICicsRegionProvisioner provisioner : provisioners) {
            provisioner.cicsProvisionBuild();
        }

    }

    @Override
    public void provisionStart() throws ManagerException, ResourceUnavailableException {
        // Add the default Logon Provider incase one isn't supplied
        this.logonProviders.add(new CicstsDefaultLogonProvider(getFramework()));

        // First, give the provisioners the opportunity to start CICS regions
        for (ICicsRegionProvisioner provisioner : provisioners) {
            provisioner.cicsProvisionStart();
        }

        // Start the CICS Regions

        // Start the autoconnect terminals - in case they were not started during the above provisioner code
        logger.info("Connecting CICS Terminals");
        for (CicsTerminalImpl terminal : this.terminals) {
            if (terminal.isConnected()) {
                continue;
            }
            
            if (!terminal.isConnectAtStartup()) {
                continue;
            }
            
            if (!terminal.getCicsRegion().isProvisionStart()) {
                continue;
            }
            
            try {
                terminal.connectToCicsRegion();
            } catch (CicstsManagerException e) {
                throw new CicstsManagerException("Failed to connect to the " + terminal.getCicsRegion(), e);
            }
        }
    }

    @Override
    public void provisionStop() {
        for (CicsTerminalImpl terminal : this.terminals) {
            try {
                terminal.writeRasOutput();
            	terminal.flushTerminalCache();
                terminal.disconnect();
            } catch (TerminalInterruptedException e) { // NOSONAR - wish to hide disconnect errors
            }
        }
        
        // Give the provisioners the opportunity to stop CICS regions
        for (ICicsRegionProvisioner provisioner : provisioners) {
            provisioner.cicsProvisionStop();
        }

    }
    
    @Override
    public void provisionDiscard() {
        // Give the provisioners the opportunity to discard CICS regions
        for (ICicsRegionProvisioner provisioner : provisioners) {
            provisioner.cicsProvisionDiscard();
        }
    }

    @Override
    public void registerProvisioner(ICicsRegionProvisioner provisioner) {
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
    public List<ICicsRegionLogonProvider> getLogonProviders() {
        return new ArrayList<>(this.logonProviders);
    }

    @Override
    public @NotNull ProductVersion getDefaultVersion() {
        return DefaultVersion.get();
    }

    @Override
    public void registerCeciProvider(@NotNull ICeciProvider ceciProvider) {
        this.ceciProvider = ceciProvider;
    }

    @Override
    public void registerCedaProvider(@NotNull ICedaProvider cedaProvider) {
        this.cedaProvider = cedaProvider;
    }

    @Override
    public void registerCemtProvider(@NotNull ICemtProvider cemtProvider) {
        this.cemtProvider = cemtProvider;
    }
    
    @Override
    public void registerCicsResourceProvider(@NotNull ICicsResourceProvider cicsResourceProvider) {
        this.cicsResourceProvider = cicsResourceProvider;
    }

    @Override
    @NotNull
    public ICeciProvider getCeciProvider() throws CicstsManagerException {
        if (this.ceciProvider == null) {
            throw new CicstsManagerException("No CECI provider has been registered");
        }
        
        return this.ceciProvider;
    }

    @Override
    @NotNull
    public ICedaProvider getCedaProvider() throws CicstsManagerException {
        if (this.cedaProvider == null) {
            throw new CicstsManagerException("No CEDA provider has been registered");
        }
        
        return this.cedaProvider;
    }

    @Override
    @NotNull
    public ICemtProvider getCemtProvider() throws CicstsManagerException {
        if (this.cemtProvider == null) {
            throw new CicstsManagerException("No CEMT provider has been registered");
        }
        
        return this.cemtProvider;
    }

    @Override
	public @NotNull ICicsResourceProvider getCicsResourceProvider() throws CicstsManagerException {
    	if (this.cicsResourceProvider == null) {
            throw new CicstsManagerException("No CICS Resource provider has been registered");
        }
        
        return this.cicsResourceProvider;
	}

	@Override
    public void cicstsRegionStarted(ICicsRegion region) throws CicstsManagerException {
        // A region has started, so connect everything up
        
        // Connect terminals that are associated with the region
        
        for(CicsTerminalImpl terminal : terminals) {
            if (terminal.getCicsRegion() == region) {
                if (terminal.isConnectAtStartup()) {
                    if (!terminal.connectToCicsRegion()) {
                        throw new CicstsManagerException("Failed to connect terminal to CICS TS region");
                    }
                }
            }
        }
    }

	@Override
	public IZosBatch getZosBatch(ICicsRegion region) throws CicstsManagerException {
		return this.zosBatchManager.getZosBatch(region.getZosImage());
	}

	@Override
	public IZosFileHandler getZosFileHandler() throws CicstsManagerException {
		try {
			return this.zosFileManager.getZosFileHandler();
		} catch (ZosFileManagerException e) {
			throw new CicstsManagerException("Unable to get zOS File Handler", e);
		}
	}

	@Override
	public Map<String, ICicsRegionProvisioned> getTaggedCicsRegions() {
		HashMap<String, ICicsRegionProvisioned> clonedTaggedCicsRegions = new HashMap<>();
		for(Map.Entry<String, ICicsRegionProvisioned> entry : this.provisionedCicsRegions.entrySet()) {
			clonedTaggedCicsRegions.put(entry.getKey(), entry.getValue());
		}		
		return clonedTaggedCicsRegions;
	}

	@Override
	public List<ICicsTerminal> getCicsTerminals() {	
		return new ArrayList<>(this.terminals);
	}
}
