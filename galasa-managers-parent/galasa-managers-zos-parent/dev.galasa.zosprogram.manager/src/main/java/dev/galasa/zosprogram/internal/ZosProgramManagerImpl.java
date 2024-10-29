/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosprogram.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.artifact.IArtifactManager;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.spi.IZosBatchSpi;
import dev.galasa.zosfile.IZosDataset;
import dev.galasa.zosfile.IZosDataset.DSType;
import dev.galasa.zosfile.IZosDataset.DatasetOrganization;
import dev.galasa.zosfile.IZosDataset.RecordFormat;
import dev.galasa.zosfile.IZosDataset.SpaceUnit;
import dev.galasa.zosfile.spi.IZosFileSpi;
import dev.galasa.zosprogram.IZosProgram;
import dev.galasa.zosprogram.ZosProgram;
import dev.galasa.zosprogram.ZosProgram.Language;
import dev.galasa.zosprogram.ZosProgramManagerException;
import dev.galasa.zosprogram.internal.properties.ZosProgramPropertiesSingleton;
import dev.galasa.zosprogram.spi.IZosProgramManagerSpi;

@Component(service = { IManager.class })
public class ZosProgramManagerImpl extends AbstractManager implements IZosProgramManagerSpi {
    
    private static final Log logger = LogFactory.getLog(ZosProgramManagerImpl.class);
    
    private static final String NAMESPACE = "zosprogram";

    private static final String ZOSBATCH_JOBS = "zosBatchJobs";

    private static final String PROVISIONING = "provisioning";

	private static final String PRE_TEST = "preTest";
    
    private IZosManagerSpi zosManager;
    protected IZosManagerSpi getZosManager() {
        return this.zosManager;
    }

    private IZosBatchSpi zosBatch;
    protected IZosBatchSpi getZosBatch() {
    	return this.zosBatch;
    }

    private IZosFileSpi zosFile;
    protected IZosFileSpi getZosFile() {
        return this.zosFile;
    }

    private IArtifactManager artifactManager;
    protected IArtifactManager getArtifactManager() {
        return this.artifactManager;
    }

    private IBundleResources testBundleResources;
    protected IBundleResources getTestBundleResources() {
        return this.testBundleResources;
    }

    protected IBundleResources managerBundleResources;
    protected IBundleResources getManagerBundleResources() {
        return this.managerBundleResources;
    }

	private Path archivePath;
	protected Path getArchivePath() {
		return archivePath;
	}

    private String runId;    
    public String getRunId() {
        return this.runId;
    }

    protected IZosDataset runLoadlib;

    private final LinkedHashMap<String, ZosProgramImpl> zosPrograms = new LinkedHashMap<>();
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        try {
            ZosProgramPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosProgramManagerException("Unable to request framework services", e);
        }

        if(galasaTest.isJava()) {
            //*** Check to see if any of our annotations are present in the test class
            //*** If there is,  we need to activate
            List<AnnotatedField> ourFields = findAnnotatedFields(ZosProgramManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers, galasaTest);
            }
        }
        this.runId = getFramework().getTestRunName();
    }
    

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        
        this.zosManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosManagerSpi.class);
        if (this.zosManager == null) {
            throw new ZosProgramManagerException("The zOS Manager is not available");
        }
        this.zosBatch = addDependentManager(allManagers, activeManagers, galasaTest, IZosBatchSpi.class);
        if (this.zosBatch == null) {
            throw new ZosProgramManagerException("The zOS Batch Manager is not available");
        }
        this.zosFile = addDependentManager(allManagers, activeManagers, galasaTest, IZosFileSpi.class);
        if (this.zosFile == null) {
            throw new ZosProgramManagerException("The zOS File Manager is not available");
        }
        this.artifactManager = addDependentManager(allManagers, activeManagers, galasaTest, IArtifactManager.class);
        if (this.artifactManager == null) {
            throw new ZosProgramManagerException("The Artifact Manager is not available");
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
    
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(ZosProgramManagerField.class);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.IManager#startOfTestClass()
     */
    @Override
    public void startOfTestClass() throws ManagerException {
    	this.archivePath = getFramework().getResultArchiveStore().getStoredArtifactsRoot().resolve(PROVISIONING).resolve(ZOSBATCH_JOBS).resolve(PRE_TEST);
        this.managerBundleResources = artifactManager.getBundleResources(this.getClass());
        this.testBundleResources = artifactManager.getBundleResources(getTestClass());
        for (Entry<String, ZosProgramImpl> entry : zosPrograms.entrySet()) {
            if (entry.getValue().getCompile()) {
                entry.getValue().compile();
            } else {
                logger.warn("WARNING: " + entry.getValue().getLanguage() + " program \"" + entry.getValue().getName() + "\"" + ((ZosProgramImpl) entry.getValue()).logForField() + " is set to \"compile = false\" and has not been compiled");
            }
        }
    }
    
    @GenerateAnnotatedField(annotation=ZosProgram.class)
    public IZosProgram generateZosProgram(Field field, List<Annotation> annotations) throws ZosProgramManagerException {
        ZosProgram annotationZosProgram = field.getAnnotation(ZosProgram.class);

        String name = nulled(annotationZosProgram.name());
        String location = nulled(annotationZosProgram.location());
        String tag = defaultString(annotationZosProgram.imageTag(), "PRIMARY").toUpperCase();;
        Language language = annotationZosProgram.language();
        boolean cics = annotationZosProgram.cics();
        String loadlib = nulled(annotationZosProgram.loadlib());
        boolean compile = annotationZosProgram.compile();
        
        ZosProgramImpl zosProgram = new ZosProgramImpl(this, field, tag, name, location, language, cics, loadlib, compile);
        zosPrograms.put(field.getName(), zosProgram);
        
        return zosProgram;
    }


    @Override
    public IZosProgram newZosProgram(IZosImage image, String name, String programSource, Language language, boolean cics, String loadlib) throws ZosProgramManagerException {
        return new ZosProgramImpl(this, image, name, programSource, language, cics, loadlib);
    }


    public IZosBatch getZosBatchForImage(IZosImage image) {
        return zosBatch.getZosBatch(image);
    }
    
    public IZosDataset getRunLoadlib(IZosImage image) throws ZosProgramManagerException {
        if (runLoadlib == null) {
            try {
                runLoadlib = zosFile.getZosFileHandler().newDataset(zosManager.getRunDatasetHLQ(image) + "." + runId + ".LOAD", image);
                if (!runLoadlib.exists()) {
                    runLoadlib.setSpace(SpaceUnit.CYLINDERS, 1, 5);
                    runLoadlib.setRecordFormat(RecordFormat.UNDEFINED);
                    runLoadlib.setRecordlength(0);
                    runLoadlib.setBlockSize(32720);
                    runLoadlib.setDatasetOrganization(DatasetOrganization.PARTITIONED);
                    runLoadlib.setDatasetType(DSType.LIBRARY);
                    runLoadlib.create();
                    runLoadlib.setShouldArchive(false);
                }
            } catch (ZosManagerException e) {
                throw new ZosProgramManagerException(e);
            }
        }
        return runLoadlib;
    }
}
