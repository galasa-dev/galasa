/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.artifact.IArtifactManager;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.textscan.ILogScanner;
import dev.galasa.textscan.TextScanManagerException;
import dev.galasa.textscan.spi.ITextScannerManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.spi.IZosBatchSpi;
import dev.galasa.zosconsole.IZosConsole;
import dev.galasa.zosconsole.ZosConsoleManagerException;
import dev.galasa.zosconsole.spi.IZosConsoleSpi;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.spi.IZosFileSpi;
import dev.galasa.zosliberty.IZosLiberty;
import dev.galasa.zosliberty.ZosLiberty;
import dev.galasa.zosliberty.ZosLibertyManagerException;
import dev.galasa.zosliberty.internal.properties.ZosLibertyPropertiesSingleton;
import dev.galasa.zosliberty.spi.IZosLibertySpi;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.spi.IZosUNIXCommandSpi;

@Component(service = { IManager.class })
public class ZosLibertyManagerImpl extends AbstractManager implements IZosLibertySpi {

    private static final Log logger = LogFactory.getLog(ZosLibertyManagerImpl.class);

    protected static final String NAMESPACE = "zosliberty";

    private static final String LIBERTY_SERVERS = "LibertyServers";

    private static final String PROVISIONING = "provisioning";

    private IZosManagerSpi  zosManager;
    private IZosFileSpi zosFileManager;
    private IZosBatchSpi zosBatchManager;
    private IZosConsoleSpi zosConsoleManager;
    private IZosUNIXCommandSpi zosUNIXCommand;
    private ITextScannerManagerSpi textScannerManager;
    private IArtifactManager artifactManager;

    private final List<ZosLibertyImpl> zosLibertys = new ArrayList<>();

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
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        try {
            ZosLibertyPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosLibertyManagerException("Unable to request framework services", e);
        }

        if(galasaTest.isJava()) {
            //*** Check to see if any of our annotations are present in the test class
            //*** If there is,  we need to activate
            List<AnnotatedField> ourFields = findAnnotatedFields(ZosLibertyField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers,galasaTest);
            }
        }
        this.artifactsRoot = getFramework().getResultArchiveStore().getStoredArtifactsRoot();
        this.archivePath = artifactsRoot.resolve(PROVISIONING).resolve(LIBERTY_SERVERS);
        this.currentTestMethodArchiveFolderName = "preTest";
    }


    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        this.zosManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosManagerSpi.class);
        if (zosManager == null) {
            throw new ZosLibertyManagerException("The zOS Manager is not available");
        }
        this.zosFileManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosFileSpi.class);
        if (this.zosFileManager == null) {
            throw new ZosLibertyManagerException("The zOS File Manager is not available");
        }
        this.zosBatchManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosBatchSpi.class);
        if (this.zosBatchManager == null) {
            throw new ZosLibertyManagerException("The zOS Batch Manager is not available");
        }
        this.zosConsoleManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosConsoleSpi.class);
        if (this.zosConsoleManager == null) {
            throw new ZosLibertyManagerException("The zOS Console Manager is not available");
        }
        this.zosUNIXCommand = addDependentManager(allManagers, activeManagers, galasaTest, IZosUNIXCommandSpi.class);
        if (this.zosUNIXCommand == null) {
            throw new ZosLibertyManagerException("The zOS UNIX Command Manager is not available");
        }
        this.textScannerManager = addDependentManager(allManagers, activeManagers, galasaTest, ITextScannerManagerSpi.class);
        if (this.textScannerManager == null) {
            throw new ZosLibertyManagerException("The Text Scanner Manager is not available");
        }
        this.artifactManager = addDependentManager(allManagers, activeManagers, galasaTest, IArtifactManager.class);
        if (this.artifactManager == null) {
            throw new ZosLibertyManagerException("The Artifact Manager is not available");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * dev.galasa.framework.spi.IManager#areYouProvisionalDependentOn(dev.galasa.framework.spi.IManager)
     */
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        return otherManager instanceof IZosManagerSpi;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#startOfTestMethod()
     */
    @Override
    public void startOfTestMethod(@NotNull GalasaMethod galasaMethod) throws ManagerException {
        cleanup(false);
        this.archivePath = artifactsRoot.resolve(LIBERTY_SERVERS);
        if (galasaMethod.getJavaTestMethod() != null) {
            this.currentTestMethodArchiveFolderName = galasaMethod.getJavaTestMethod().getName() + "." + galasaMethod.getJavaExecutionMethod().getName();
        } else {
            this.currentTestMethodArchiveFolderName = galasaMethod.getJavaExecutionMethod().getName();
        }
    }

    /* (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#endOfTestRun()
     */
    @Override
    public void endOfTestRun() {
        try {
            cleanup(true);
        } catch (ZosLibertyManagerException e) {
            logger.error("Problem in endOfTestRun()", e);
        }
    }
    
    protected void cleanup(boolean endOfTest) throws ZosLibertyManagerException {
        for (ZosLibertyImpl zosLiberty : this.zosLibertys) {
            zosLiberty.cleanup(endOfTest);
        }
    }


    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(ZosLibertyField.class);
    }

    @GenerateAnnotatedField(annotation=ZosLiberty.class)
    public IZosLiberty generateZosLiberty(Field field, List<Annotation> annotations) throws ZosLibertyManagerException {
        ZosLibertyImpl zosLiberty = new ZosLibertyImpl(this);
        zosLibertys.add(zosLiberty);
        return zosLiberty;
    }

    public IZosManagerSpi getZosManager() {
        return this.zosManager;
    }

    @Override
    public @NotNull IZosLiberty getZosLiberty() throws ZosLibertyManagerException {
        ZosLibertyImpl zosLiberty = new ZosLibertyImpl(this);
        zosLibertys.add(zosLiberty);
        return zosLiberty;
    }

    public IZosFileHandler getZosFileHandler() throws ZosLibertyManagerException {
        try {
            return this.zosFileManager.getZosFileHandler();
        } catch (ZosFileManagerException e) {
            throw new ZosLibertyManagerException("Problem getting IZosFileHandler", e);
        }
    }

    public IZosConsole getZosConsole(IZosImage zosImage) throws ZosLibertyManagerException {
        try {
            return this.zosConsoleManager.getZosConsole(zosImage);
        } catch (ZosConsoleManagerException e) {
            throw new ZosLibertyManagerException("Problem getting IZosFileHandler", e);
        }
    }

    public IZosUNIXCommand getZosUNIXCommand(IZosImage image) {
        return this.zosUNIXCommand.getZosUNIXCommand(image);
    }

    public IZosBatch getZosBatch(IZosImage image) {
        return this.zosBatchManager.getZosBatch(image);
    }

    protected ILogScanner getLogScanner() throws ZosLibertyManagerException {
        try {
            return this.textScannerManager.getLogScanner();
        } catch (TextScanManagerException e) {
            throw new ZosLibertyManagerException("Problem getting ILogScanner", e);
        }
    }

    public IArtifactManager getArtifactManager() {
        return this.artifactManager;
    }
}
