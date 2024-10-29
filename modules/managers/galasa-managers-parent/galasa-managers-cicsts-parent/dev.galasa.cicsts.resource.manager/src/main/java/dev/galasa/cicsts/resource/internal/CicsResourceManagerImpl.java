/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.resource.internal;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.artifact.IArtifactManager;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.cicsresource.CicsResourceManagerException;
import dev.galasa.cicsts.cicsresource.ICicsResource;
import dev.galasa.cicsts.resource.internal.properties.CicstsResourcePropertiesSingleton;
import dev.galasa.cicsts.spi.ICicsResourceProvider;
import dev.galasa.cicsts.spi.ICicstsManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.textscan.ILogScanner;
import dev.galasa.textscan.TextScanManagerException;
import dev.galasa.textscan.spi.ITextScannerManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.spi.IZosFileSpi;
import dev.galasa.zosliberty.IZosLiberty;
import dev.galasa.zosliberty.ZosLibertyManagerException;
import dev.galasa.zosliberty.spi.IZosLibertySpi;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.spi.IZosUNIXCommandSpi;

@Component(service = { IManager.class })
public class CicsResourceManagerImpl extends AbstractManager implements ICicsResourceProvider {
    
    protected static final String NAMESPACE = "cicsresource";
    private ICicstsManagerSpi cicstsManager;
    private IZosFileSpi zosFileManager;
    private IZosLibertySpi zosLibertyManager;
    protected IZosUNIXCommandSpi zosUnixCommandManager;
    private ITextScannerManagerSpi textScannerManager;
	private IArtifactManager artifactManager;

    private static final String JVMSERVERS = "JVM_servers";

    private static final String PROVISIONING = "provisioning";
    
    private HashMap<ICicsRegion, ICicsResource> regionCicsResources = new HashMap<>();
	private List<CicsBundleImpl>  cicsBundles = new ArrayList<>();
    private List<JvmprofileImpl> jvmprofiles = new ArrayList<>();
    private List<JvmserverImpl> jvmServers = new ArrayList<>();
    

    private Path artifactsRoot;
    public Path getArtifactsRoot() {
        return artifactsRoot;
    }
    
    private Path archivePath;
    public Path getArchivePath() {
        return this.archivePath;
    }
    
    private String currentTestMethodArchiveFolderName;

    public Path getCurrentTestMethodArchiveFolder() {
        return archivePath.resolve(currentTestMethodArchiveFolderName);
    }
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);

        if(galasaTest.isJava()) {
            youAreRequired(allManagers, activeManagers, galasaTest);
        }
        try {
            CicstsResourcePropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new CicsResourceManagerException("Unable to request framework services", e);
        }
        
        this.artifactsRoot = getFramework().getResultArchiveStore().getStoredArtifactsRoot();
        this.archivePath = artifactsRoot.resolve(PROVISIONING).resolve(JVMSERVERS);
        this.currentTestMethodArchiveFolderName = "preTest";
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#areYouProvisionalDependentOn(dev.galasa.framework.spi.IManager)
     */
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        return otherManager instanceof ICicstsManagerSpi ||
               otherManager instanceof IZosFileSpi ||
               otherManager instanceof IZosLibertySpi ||
               otherManager instanceof IZosUNIXCommandSpi ||
               otherManager instanceof ITextScannerManagerSpi ||
               otherManager instanceof IArtifactManager;
    }


    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#youAreRequired()
     */
    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        
        this.cicstsManager = addDependentManager(allManagers, activeManagers, galasaTest, ICicstsManagerSpi.class);
        if(cicstsManager == null) {
           throw new CicstsManagerException("The CICS Manager is not available");
        }
        this.zosFileManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosFileSpi.class);
        if (this.zosFileManager == null) {
            throw new CicstsManagerException("The zOS File Manager is not available");
        }
        this.zosLibertyManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosLibertySpi.class);
        if (this.zosLibertyManager == null) {
            throw new CicstsManagerException("The zOS Liberty Manager is not available");
        }
        this.zosUnixCommandManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosUNIXCommandSpi.class);
        if (this.zosUnixCommandManager == null) {
            throw new CicstsManagerException("The zOS UNIX Command Manager is not available");
        }
        this.textScannerManager = addDependentManager(allManagers, activeManagers, galasaTest, ITextScannerManagerSpi.class);
        if (this.textScannerManager == null) {
            throw new CicstsManagerException("The Text Scan Manager is not available");
        }
        this.artifactManager = addDependentManager(allManagers, activeManagers, galasaTest, IArtifactManager.class);
        if (this.artifactManager == null) {
            throw new ZosLibertyManagerException("The Artifact Manager is not available");
        }
        cicstsManager.registerCicsResourceProvider(this);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#startOfTestMethod()
     */
    @Override
    public void startOfTestMethod(@NotNull GalasaMethod galasaMethod) throws ManagerException {
        this.archivePath = artifactsRoot.resolve(JVMSERVERS);
        if (galasaMethod.getJavaTestMethod() != null) {
            this.currentTestMethodArchiveFolderName = galasaMethod.getJavaTestMethod().getName() + "." + galasaMethod.getJavaExecutionMethod().getName();
        } else {
            this.currentTestMethodArchiveFolderName = galasaMethod.getJavaExecutionMethod().getName();
        }
    }

    /* (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#endOfTestClass(java.lang.String,
     * java.lang.Throwable)
     */
    @Override
    public Result endOfTestClass(@NotNull Result currentResult, Throwable currentException) throws ManagerException {
        this.archivePath = artifactsRoot.resolve(PROVISIONING).resolve(JVMSERVERS);
        this.currentTestMethodArchiveFolderName = "postTest";
        
        
        for (CicsBundleImpl cicsBundle : this.cicsBundles) {
            cicsBundle.cleanup();
        }
        for (JvmserverImpl jvmServer : this.jvmServers) {
            jvmServer.cleanup();
        }
        for (JvmprofileImpl jvmProfile : this.jvmprofiles) {
            jvmProfile.cleanup();
        }
        
        return null;
    }
    
    @Override
    public @NotNull ICicsResource getCicsResource(ICicsRegion cicsRegion) throws CicsResourceManagerException {
        ICicsResource cicsResources = this.regionCicsResources.get(cicsRegion);
        if (cicsResources == null) {
            cicsResources = new CicsResourceImpl(this, cicsRegion);
            this.regionCicsResources.put(cicsRegion, cicsResources);
        }
        return cicsResources;
    }

    protected IArtifactManager getArtifactManager() {
		return this.artifactManager;
	}

	protected ICicstsManagerSpi getCicsManager() {
        return this.cicstsManager;
    }
    
    protected IZosFileHandler getZosFileHandler() throws CicsResourceManagerException {
        try {
            return this.zosFileManager.getZosFileHandler();
        } catch (ZosFileManagerException e) {
            throw new CicsResourceManagerException("Problem getting IZosFileHandler", e);
        }
    }
    
    protected IZosLiberty getZosLiberty() throws CicsResourceManagerException {
        try {
            return this.zosLibertyManager.getZosLiberty();
        } catch (ZosLibertyManagerException e) {
            throw new CicsResourceManagerException("Problem getting IZosLiberty", e);
        }
    }
    
    protected @NotNull IZosUNIXCommand getZosUnixCommand(IZosImage image) {
        return this.zosUnixCommandManager.getZosUNIXCommand(image);
    }

    protected ILogScanner getLogScanner() throws CicsResourceManagerException {
        try {
            return this.textScannerManager.getLogScanner();
        } catch (TextScanManagerException e) {
            throw new CicsResourceManagerException("Problem getting ILogScanner", e);
        }
    }

    protected void registerCicsBundle(CicsBundleImpl cicsBundle) {
		this.cicsBundles.add(cicsBundle);
	}

	protected void registerJvmserver(JvmserverImpl jvmserver) {
        this.jvmServers.add(jvmserver);
    }

	protected void registerJvmprofile(JvmprofileImpl jvmprofile) {
        this.jvmprofiles.add(jvmprofile);
    }
}
