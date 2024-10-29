/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.rseapi.manager.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatch;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchField;
import dev.galasa.zosbatch.ZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.spi.IZosBatchSpi;
import dev.galasa.zosrseapi.spi.IRseapiManagerSpi;

/**
 * zOS Batch Manager implemented using zOS/MF
 *
 */
@Component(service = { IManager.class })
public class RseapiZosBatchManagerImpl extends AbstractManager implements IZosBatchSpi {
    
    private static final Log logger = LogFactory.getLog(RseapiZosBatchManagerImpl.class);

    private static final String ZOSBATCH_JOBS = "zosBatchJobs";

    private static final String PROVISIONING = "provisioning";

    private IZosManagerSpi zosManager;
    public IZosManagerSpi getZosManager() {
        return this.zosManager;
    }
    
    private IRseapiManagerSpi rseapiManager;
    public IRseapiManagerSpi getRseapiManager() {
        return this.rseapiManager;
    }

    private final HashMap<String, RseapiZosBatchImpl> taggedZosBatches = new HashMap<>();
    private final HashMap<String, RseapiZosBatchImpl> zosBatches = new HashMap<>();

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
        return this.archivePath.resolve(this.currentTestMethodArchiveFolderName);
    }
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);

        if(galasaTest.isJava()) {
            //*** Check to see if any of our annotations are present in the test class
            //*** If there is,  we need to activate
            List<AnnotatedField> ourFields = findAnnotatedFields(ZosBatchField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers, galasaTest);
            }
        }
        
        this.artifactsRoot = getFramework().getResultArchiveStore().getStoredArtifactsRoot();
        this.archivePath = artifactsRoot.resolve(PROVISIONING).resolve(ZOSBATCH_JOBS);
        this.currentTestMethodArchiveFolderName = "preTest";
    }
    

    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(ZosBatchField.class);
    }


    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#youAreRequired()
     */
    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        this.zosManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosManagerSpi.class);
        if (zosManager == null) {
            throw new ZosBatchManagerException("The zOS Manager is not available");
        }
        this.rseapiManager = addDependentManager(allManagers, activeManagers, galasaTest, IRseapiManagerSpi.class);
        if (rseapiManager == null) {
            throw new ZosBatchManagerException("The RSE API Manager is not available");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#areYouProvisionalDependentOn(dev.galasa.framework.spi.IManager)
     */
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        return otherManager instanceof IZosManagerSpi ||
               otherManager instanceof IRseapiManagerSpi;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#startOfTestMethod()
     */
    @Override
    public void startOfTestMethod(@NotNull GalasaMethod galasaMethod) throws ManagerException {
        cleanup(false);
        this.archivePath = artifactsRoot.resolve(ZOSBATCH_JOBS);
        if (galasaMethod.getJavaTestMethod() != null) {
            this.currentTestMethodArchiveFolderName = galasaMethod.getJavaTestMethod().getName() + "." + galasaMethod.getJavaExecutionMethod().getName();
        } else {
            this.currentTestMethodArchiveFolderName = galasaMethod.getJavaExecutionMethod().getName();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#endOfTestMethod(java.lang.String,java.lang.Throwable)
     */
    @Override
    public Result endOfTestMethod(@NotNull GalasaMethod galasaMethod, @NotNull Result currentResult, Throwable currentException) throws ManagerException {
        cleanup(false);
        
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#endOfTestClass(java.lang.String,
     * java.lang.Throwable)
     */
    @Override
    public Result endOfTestClass(@NotNull Result currentResult, Throwable currentException) throws ManagerException {
        this.archivePath = artifactsRoot.resolve(PROVISIONING).resolve(ZOSBATCH_JOBS);
        this.currentTestMethodArchiveFolderName = "postTest";
        cleanup(false);
        
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#endOfTestRun()
     */
    @Override
    public void endOfTestRun() {
        try {
            cleanup(true);
        } catch (ZosBatchException e) {
            logger.error("Problem in endOfTestRun()", e);
        }
    }
    
    protected void cleanup(boolean endOfTest) throws ZosBatchException {
        for (Entry<String, RseapiZosBatchImpl> entry : this.taggedZosBatches.entrySet()) {
            entry.getValue().cleanup(endOfTest);
        }
        for (Entry<String, RseapiZosBatchImpl> entry : this.zosBatches.entrySet()) {
            entry.getValue().cleanup(endOfTest);
        }
    }
    
    @GenerateAnnotatedField(annotation=ZosBatch.class)
    public IZosBatch generateZosBatch(Field field, List<Annotation> annotations) throws ZosManagerException {
        ZosBatch annotationZosBatch = field.getAnnotation(ZosBatch.class);

        //*** Default the tag to primary
        String tag = defaultString(annotationZosBatch.imageTag(), "PRIMARY").toUpperCase();

        //*** Have we already generated this tag
        if (this.taggedZosBatches.containsKey(tag)) {
            return this.taggedZosBatches.get(tag);
        }

        IZosImage image = zosManager.getImageForTag(tag);
        IZosBatch zosBatch = new RseapiZosBatchImpl(this, image);
        this.taggedZosBatches.put(tag, (RseapiZosBatchImpl) zosBatch);
        
        return zosBatch;
    }
    
    @GenerateAnnotatedField(annotation=ZosBatchJobname.class)
    public IZosBatchJobname generateZosBatchJobname(Field field, List<Annotation> annotations) throws ZosBatchManagerException {
        ZosBatchJobname annotationZosBatchJobname = field.getAnnotation(ZosBatchJobname.class);

        //*** Default the tag to primary
        String tag = defaultString(annotationZosBatchJobname.imageTag(), "PRIMARY").toUpperCase();
        IZosImage image;
        try {
            image = zosManager.getImageForTag(tag);
        } catch (ZosManagerException e) {
            throw new ZosBatchManagerException("Unable to get image for tag \"" + tag + "\"", e);
        }
        return newZosBatchJobname(image);
    }

    protected IZosBatchJobname newZosBatchJobname(IZosImage image) throws ZosBatchException {
        return zosManager.newZosBatchJobname(image);
    }

    protected IZosBatchJobname newZosBatchJobname(String name) throws ZosBatchException {
        return zosManager.newZosBatchJobname(name);
    }

    @Override
    public @NotNull IZosBatch getZosBatch(IZosImage image) {
        if (zosBatches.containsKey(image.getImageID())) {
            return zosBatches.get(image.getImageID());
        } else {
            RseapiZosBatchImpl zosBatch = new RseapiZosBatchImpl(this, image);
            zosBatches.put(image.getImageID(), zosBatch);
            return zosBatch;
        }
    }
}
