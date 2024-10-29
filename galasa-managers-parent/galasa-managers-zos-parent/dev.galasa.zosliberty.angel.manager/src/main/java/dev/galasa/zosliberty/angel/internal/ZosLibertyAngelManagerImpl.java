/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty.angel.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.spi.IZosBatchSpi;
import dev.galasa.zosconsole.IZosConsole;
import dev.galasa.zosconsole.ZosConsoleManagerException;
import dev.galasa.zosconsole.spi.IZosConsoleSpi;
import dev.galasa.zosliberty.angel.IZosLibertyAngel;
import dev.galasa.zosliberty.angel.ZosLibertyAngel;
import dev.galasa.zosliberty.angel.ZosLibertyAngelManagerException;
import dev.galasa.zosliberty.angel.internal.properties.ZosLibertyAngelPropertiesSingleton;
import dev.galasa.zosliberty.angel.spi.IZosLibertyAngelSpi;

@Component(service = { IManager.class })
public class ZosLibertyAngelManagerImpl extends AbstractManager implements IZosLibertyAngelSpi {

    protected static final String NAMESPACE = "zoslibertyangel";

    private static final String LIBERTY_ANGELS = "LibertyAngels";

    private static final String PROVISIONING = "provisioning";

    private IZosManagerSpi  zosManager;
    private IZosBatchSpi zosBatchManager;
    private IZosConsoleSpi zosConsoleManager;

    private final List<ZosLibertyAngelImpl> zosLibertyAngels = new ArrayList<>();

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
            ZosLibertyAngelPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosLibertyAngelManagerException("Unable to request framework services", e);
        }

        if(galasaTest.isJava()) {
            //*** Check to see if any of our annotations are present in the test class
            //*** If there is,  we need to activate
            List<AnnotatedField> ourFields = findAnnotatedFields(ZosLibertyAngelField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers,galasaTest);
            }
        }
        this.artifactsRoot = getFramework().getResultArchiveStore().getStoredArtifactsRoot();
        this.archivePath = artifactsRoot.resolve(PROVISIONING).resolve(LIBERTY_ANGELS);
        this.currentTestMethodArchiveFolderName = "preTest";
    }


    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        this.zosManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosManagerSpi.class);
        if (zosManager == null) {
            throw new ZosLibertyAngelManagerException("The zOS Manager is not available");
        }
        this.zosBatchManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosBatchSpi.class);
        if (this.zosBatchManager == null) {
            throw new ZosLibertyAngelManagerException("The zOS Batch Manager is not available");
        }
        this.zosConsoleManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosConsoleSpi.class);
        if (this.zosConsoleManager == null) {
            throw new ZosLibertyAngelManagerException("The zOS Console Manager is not available");
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
        this.archivePath = artifactsRoot.resolve(LIBERTY_ANGELS);
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
        for (ZosLibertyAngelImpl zosLibertyAngel : this.zosLibertyAngels) {
        	zosLibertyAngel.cleanup();
        }
    }


    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(ZosLibertyAngelField.class);
    }

    @GenerateAnnotatedField(annotation=ZosLibertyAngel.class)
    public IZosLibertyAngel generateZosLiberty(Field field, List<Annotation> annotations) throws ZosLibertyAngelManagerException {
    	ZosLibertyAngel annotationZosLibertyAngel = field.getAnnotation(ZosLibertyAngel.class);
    	
    	//*** Default the tag to primary
        String tag = defaultString(annotationZosLibertyAngel.imageTag(), "PRIMARY").toUpperCase();
        
        String angelName = annotationZosLibertyAngel.name();

        IZosImage zosImage;
		try {
			zosImage = this.zosManager.getImageForTag(tag);
		} catch (ZosManagerException e) {
			throw new ZosLibertyAngelManagerException("Problem getting zOS image for tag \"" + tag + "\"", e);
		}
    	
        ZosLibertyAngelImpl zosLibertyAngel = new ZosLibertyAngelImpl(this, zosImage, angelName);
        zosLibertyAngels.add(zosLibertyAngel);
        return zosLibertyAngel;
    }

    public IZosManagerSpi getZosManager() {
        return this.zosManager;
    }

    @Override
    public @NotNull IZosLibertyAngel newZosLibertyAngel(IZosImage zosImage, String angelName) throws ZosLibertyAngelManagerException {
        ZosLibertyAngelImpl zosLibertyAngel = new ZosLibertyAngelImpl(this, zosImage, angelName);
        zosLibertyAngels.add(zosLibertyAngel);
        return zosLibertyAngel;
    }

    public IZosConsole getZosConsole(IZosImage zosImage) throws ZosLibertyAngelManagerException {
        try {
            return this.zosConsoleManager.getZosConsole(zosImage);
        } catch (ZosConsoleManagerException e) {
            throw new ZosLibertyAngelManagerException("Problem getting IZosFileHandler", e);
        }
    }

    public IZosBatch getZosBatch(IZosImage image) {
        return this.zosBatchManager.getZosBatch(image);
    }
}
