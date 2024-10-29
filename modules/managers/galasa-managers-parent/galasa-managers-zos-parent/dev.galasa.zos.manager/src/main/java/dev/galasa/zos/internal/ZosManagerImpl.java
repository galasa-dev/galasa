/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ICredentials;
import dev.galasa.ManagerException;
import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.framework.spi.utils.DssUtils;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.ipnetwork.IIpPort;
import dev.galasa.ipnetwork.spi.IIpNetworkManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosImage;
import dev.galasa.zos.ZosIpHost;
import dev.galasa.zos.ZosIpPort;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.ZosManagerField;
import dev.galasa.zos.internal.properties.BatchExtraBundle;
import dev.galasa.zos.internal.properties.ClusterIdForTag;
import dev.galasa.zos.internal.properties.ClusterImages;
import dev.galasa.zos.internal.properties.ConsoleExtraBundle;
import dev.galasa.zos.internal.properties.DseClusterIdForTag;
import dev.galasa.zos.internal.properties.DseImageIdForTag;
import dev.galasa.zos.internal.properties.FileExtraBundle;
import dev.galasa.zos.internal.properties.ImageIdForTag;
import dev.galasa.zos.internal.properties.JavaHome;
import dev.galasa.zos.internal.properties.LibertyInstallDir;
import dev.galasa.zos.internal.properties.RunDatasetHLQ;
import dev.galasa.zos.internal.properties.RunUNIXPathPrefix;
import dev.galasa.zos.internal.properties.TSOCommandExtraBundle;
import dev.galasa.zos.internal.properties.UNIXCommandExtraBundle;
import dev.galasa.zos.internal.properties.ZosConnectInstallDir;
import dev.galasa.zos.internal.properties.ZosPropertiesSingleton;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zos.spi.ZosImageDependencyField;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosbatch.internal.ZosBatchJobOutputImpl;
import dev.galasa.zosbatch.internal.ZosBatchJobOutputSpoolFileImpl;
import dev.galasa.zosbatch.internal.ZosBatchJobnameImpl;
import dev.galasa.zosbatch.internal.properties.BatchRestrictToImage;
import dev.galasa.zosbatch.internal.properties.JobWaitTimeout;
import dev.galasa.zosbatch.internal.properties.TruncateJCLRecords;
import dev.galasa.zosbatch.internal.properties.UseSysaff;
import dev.galasa.zosbatch.internal.properties.ZosBatchPropertiesSingleton;
import dev.galasa.zosbatch.spi.IZosBatchJobOutputSpi;
import dev.galasa.zosconsole.ZosConsoleManagerException;
import dev.galasa.zosconsole.internal.properties.ConsoleRestrictToImage;
import dev.galasa.zosconsole.internal.properties.ZosConsolePropertiesSingleton;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.internal.properties.DirectoryListMaxItems;
import dev.galasa.zosfile.internal.properties.FileRestrictToImage;
import dev.galasa.zosfile.internal.properties.UnixFilePermissions;
import dev.galasa.zosfile.internal.properties.ZosFilePropertiesSingleton;

@Component(service = { IManager.class })
public class ZosManagerImpl extends AbstractManager implements IZosManagerSpi {
    protected static final String NAMESPACE = "zos";
    protected static final String ZOSBATCH_NAMESPACE = "zosbatch";
    protected static final String ZOSFILE_NAMESPACE = "zosfile";
    protected static final String ZOSCONSOLE_NAMESPACE = "zosconsole";
    
    private static final Log logger = LogFactory.getLog(ZosManagerImpl.class);

    private static final String PRIMARY_TAG = "PRIMARY";

    private static final String LOG_SELECTED_FOR_ZOS_TAG = " selected for zosTag '";
    private static final String LOG_ZOS_IMAGE = "zOS Image ";

    private IConfigurationPropertyStoreService cps;
    private IDynamicStatusStoreService dss;
    private IIpNetworkManagerSpi ipManager;
    private ZosPoolPorts zosPoolPorts;

    private final ArrayList<ImageUsage> definedImages = new ArrayList<>();

    private final HashMap<String, ZosBaseImageImpl> taggedImages = new HashMap<>();
    private final HashMap<String, String> taggedPorts = new HashMap<>();
    private final HashMap<String, ZosBaseImageImpl> images = new HashMap<>();
    
    private String runid;
    private String javaHome;
    private String zosLibertyInstallDir;
    private String zosConnectInstallDir;

    /* 
     * We need to load the default implementations, but provide the ability for them to be overridden
     */
    @Override
    public List<String> extraBundles(@NotNull IFramework framework) throws ZosManagerException {
        try {
            ZosPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
            ZosBatchPropertiesSingleton.setCps(framework.getConfigurationPropertyService(ZOSBATCH_NAMESPACE));
            ZosFilePropertiesSingleton.setCps(framework.getConfigurationPropertyService(ZOSFILE_NAMESPACE));
            ZosConsolePropertiesSingleton.setCps(framework.getConfigurationPropertyService(ZOSCONSOLE_NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosManagerException("Unable to request framework services", e);
        }
        
        ArrayList<String> bundles = new ArrayList<>();
        
        bundles.add(BatchExtraBundle.get());
        bundles.add(ConsoleExtraBundle.get());
        bundles.add(FileExtraBundle.get());
        bundles.add(TSOCommandExtraBundle.get());
        bundles.add(UNIXCommandExtraBundle.get());

        return bundles;
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
            List<AnnotatedField> ourFields = findAnnotatedFields(ZosManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers, galasaTest);
            }
        }

        try {
            this.dss = framework.getDynamicStatusStoreService(NAMESPACE);
            this.cps = framework.getConfigurationPropertyService(NAMESPACE);
        } catch (Exception e) {
            throw new ZosManagerException("Unable to request framework services", e);
        }
    }


    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        ipManager = addDependentManager(allManagers, activeManagers, galasaTest, IIpNetworkManagerSpi.class);
        if (ipManager == null) {
            throw new ZosManagerException("The IP Network Manager is not available");
        }        
    }

    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        //*** Get all our annotated fields
        List<AnnotatedField> annotatedFields = findAnnotatedFields(ZosManagerField.class);
        Set<String> dependencyTags = findProvisionDependentAnnotatedFieldTags(ZosImageDependencyField.class, "imageTag");

        //*** First, locate the IZosImpl field that has a tag of primary
        //*** And then generate it
        boolean primaryFound = false;
        Iterator<AnnotatedField> annotatedFieldIterator = annotatedFields.iterator();
        while(annotatedFieldIterator.hasNext()) {
            AnnotatedField annotatedField = annotatedFieldIterator.next();
            final Field field = annotatedField.getField();

            if (field.getType() == IZosImage.class) {
                ZosImage annotationZosImage = field.getAnnotation(ZosImage.class);
                String tag = annotationZosImage.imageTag();
                if (tag == null || PRIMARY_TAG.equalsIgnoreCase(tag.toUpperCase())) {
                    IZosImage zosImage = generateZosImage("PRIMARY");
                    registerAnnotatedField(field, zosImage);
                    annotatedFieldIterator.remove(); //*** Dont need it for the second pass
                    primaryFound = true;
                    break;
                }
            }
        }

        // Check if there are any dependencies that require the PRIMARY tag
        if (!primaryFound && dependencyTags.contains("PRIMARY")) {
            generateZosImage("PRIMARY");
            dependencyTags.remove("PRIMARY");
        }
        
        //*** Second pass, generate all the remaining zosimages now the primary is allocated
        for(AnnotatedField annotatedField : annotatedFields) {
            final Field field = annotatedField.getField();

            //*** Check this field has not been annotated already
            if (getAnnotatedField(field) != null) {
                continue;
            }

            if (field.getType() == IZosImage.class) {
                IZosImage zosImage = generateZosImage(field);
                registerAnnotatedField(field, zosImage);
            }
        }


        // Check for any remaining dependencies
        for(String tag : dependencyTags) {
            generateZosImage(tag);
        }
        
        
		zosPoolPorts = new ZosPoolPorts(this, this.getDSS(), getFramework().getResourcePoolingService());


        //*** Auto generate the remaining fields
        generateAnnotatedFields(ZosManagerField.class);
    }
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#provisionDiscard()
     */
    @Override
    public void provisionDiscard() {
        //*** Free up any slots we have allocated for this run

        for(ZosBaseImageImpl image : images.values()) {
            if (image instanceof ZosProvisionedImageImpl) {
                ((ZosProvisionedImageImpl)image).freeImage();
            }
        }
    }

    //*** We do not allow auto generate of the zos image fields as they need
    //*** to be done first AND the primary image needs to be the first one
    protected IZosImage generateZosImage(Field field) throws ZosManagerException {
        ZosImage annotationZosImage = field.getAnnotation(ZosImage.class);

        //*** Default the tag to primary
        String tag = defaultString(annotationZosImage.imageTag(), PRIMARY_TAG).toUpperCase();
        return generateZosImage(tag);
    }

    //*** We do not allow auto generate of the zos image fields as they need
    //*** to be done first AND the primary image needs to be the first one
    protected IZosImage generateZosImage(String tag) throws ZosManagerException {
        //*** Have we already generated this tag
        if (taggedImages.containsKey(tag)) {
            return taggedImages.get(tag);
        }

        //*** Check to see if we have a DSE for this tag
        logger.info("Searching for a zos DSE Image configured for tag " + tag);
        String imageID = DseImageIdForTag.get(tag);
        if (imageID != null) {
            logger.info("zOS DSE Image " + imageID + LOG_SELECTED_FOR_ZOS_TAG + tag + "'");
            
            //*** Check to see if the image has already been allocated
            if (images.containsKey(imageID)) {
                ZosBaseImageImpl selectedImage = images.get(imageID);
                taggedImages.put(tag, selectedImage);
                return selectedImage;
            }

            logger.info("Searching for a zos DSE Cluster configured for tag " + tag);
            String clusterId = DseClusterIdForTag.get(tag);
            ZosDseImageImpl image = new ZosDseImageImpl(this, imageID, clusterId);
            images.put(image.getImageID(), image);
            taggedImages.put(tag, image);
            return image;
        }

        logger.info("No DSE Image found, searching for specific zos image for tag " + tag);
        //*** See if the we need to run on a specific image,  not DSE
        imageID = ImageIdForTag.get(tag);
        if (imageID != null) {
            //*** Do we already have it
            if (images.containsKey(imageID)) {
                ZosBaseImageImpl selectedImage = images.get(imageID);
                taggedImages.put(tag, selectedImage);
                return selectedImage;
            }

            ZosProvisionedImageImpl image = new ZosProvisionedImageImpl(this, imageID, null);
            if (image.allocateImage()) {
                logger.info(LOG_ZOS_IMAGE + image.getImageID() + LOG_SELECTED_FOR_ZOS_TAG + tag + "'");
                images.put(image.getImageID(), image);
                taggedImages.put(tag, image);
                return image;
            } else {
                DssUtils.incrementMetric(dss, "metrics.slots.insufficent");
                throw new ZosManagerException("Unable to provision zOS Image tagged " + tag + " on " + imageID + " as there is insufficient capacity");
            }
        }
        logger.info("No specific image found for tag" + tag + " selecting image");
        return selectNewImage(tag);
    }

    @GenerateAnnotatedField(annotation=ZosIpHost.class)
    public IIpHost generateIpHost(Field field, List<Annotation> annotations) throws ZosManagerException {
        ZosIpHost annotationHost = field.getAnnotation(ZosIpHost.class);

        //*** Default the tag to primary
        String tag = defaultString(annotationHost.imageTag(), PRIMARY_TAG).toUpperCase();

        //*** Ensure we have this tagged host
        ZosBaseImageImpl image = taggedImages.get(tag);
        if (image == null) { 
            throw new ZosManagerException("Unable to provision an IP Host for field " + field.getName() + " as no @ZosImage for the tag '" + tag + "' was present");
        }

        return image.getIpHost();
    }

    @GenerateAnnotatedField(annotation=ZosIpPort.class)
    public IIpPort generateIpPort(Field field, List<Annotation> annotations) throws ZosManagerException {
        ZosIpPort annotationPort = field.getAnnotation(ZosIpPort.class);

        //*** Default the tag to primary
        String imageTag = defaultString(annotationPort.imageTag(), PRIMARY_TAG).toUpperCase();
        String type = defaultString(annotationPort.type(), "standard");
        String tag = annotationPort.tag();
;
        //*** Ensure we have this tagged host
        ZosBaseImageImpl image = taggedImages.get(imageTag);
        if (image == null) { 
            throw new ZosManagerException("Unable to provision an IP Host for field " + field.getName() + " as no @ZosImage for the tag '" + imageTag + "' was present");
        }
        try {
        	IIpPort provisionedPort = image.getIpHost().provisionPort(type);        	
        	if (!tag.isEmpty()) {
        		taggedPorts.put(tag, "" + provisionedPort.getPortNumber());
        	}
            return provisionedPort;
        } catch(Exception e) {
            throw new ZosManagerException("Unable to provision a port for zOS Image " + image.getImageID() + ", type=" + type, e);
        }
    }

    protected ZosProvisionedImageImpl selectNewImage(String tag) throws ZosManagerException {
        //***  Need the cluster we can allocate an image from
        logger.info("Searching for cluster ID for tag " + tag);
        String clusterId = ClusterIdForTag.get(tag);
        if (clusterId == null) {
            logger.info("No cluster ID found for tag " + tag + " assuming DEFAULT");
            clusterId = "DEFAULT";
        }
        clusterId = clusterId.toUpperCase();

        //*** Find a list of images
        logger.info("Searching for list of images for cluster " + clusterId);
        for(String definedImage : ClusterImages.get(clusterId)) {
            ZosProvisionedImageImpl image = new ZosProvisionedImageImpl(this, definedImage, clusterId);
            definedImages.add(new ImageUsage(image));
        }

        Collections.sort(definedImages);

        //*** First attempt to use an image that has not been selected for this test yet
        for(ImageUsage image : definedImages) {
            if (this.images.containsKey(image.image.getImageID())) {
                continue;
            }

            if (image.image.allocateImage()) {
                logger.info(LOG_ZOS_IMAGE + image.image.getImageID() + LOG_SELECTED_FOR_ZOS_TAG + tag + "' with slot name " + image.image.getSlotName());
                taggedImages.put(tag, image.image);
                images.put(image.image.getImageID(), image.image);

                return image.image;
            }
        }

        //*** Can do some other stuff in the future to reuse already allocated lpars,  but not for now
        DssUtils.incrementMetric(dss, "metrics.slots.insufficent");
        throw new ZosManagerException("Insufficent capacity for images in cluster " + clusterId);
    }


    protected IDynamicStatusStoreService getDSS() {
        return this.dss;
    }

    public IConfigurationPropertyStoreService getCPS() {
        return this.cps;
    }


    protected static class ImageUsage implements Comparable<ImageUsage> {
        private final ZosProvisionedImageImpl image;
        private       Float        usage;

        public ImageUsage(ZosProvisionedImageImpl image) throws ZosManagerException {
            this.image = image;
            usage = image.getCurrentUsage();
        }

        @Override
        public int compareTo(ImageUsage o) {
            return usage.compareTo(o.usage);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            
            return this.compareTo((ImageUsage) obj) == 0;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(image);
         }

        @Override
        public String toString()  {
            return image.getImageID();            
        }

    }


    protected IIpNetworkManagerSpi getIpManager() {
        return this.ipManager;
    }

    @Override
    public @NotNull IZosImage provisionImageForTag(String tag) throws ZosManagerException {
        Objects.nonNull(tag);
        tag = tag.toUpperCase();
        
        IZosImage image = this.taggedImages.get(tag);
        if (image == null) {
            return generateZosImage(tag);
        }
        return image;
    }

    @Override
    public @NotNull IZosImage getImageForTag(String tag) throws ZosManagerException {
        Objects.nonNull(tag);
        tag = tag.toUpperCase();
        
        IZosImage image = this.taggedImages.get(tag);
        if (image == null) {
            throw new ZosManagerException("Unable to locate zOS Image for tag " + tag);
        }
        return image;
    }

    @Override
    public IZosImage getImage(String imageId) throws ZosManagerException {
        Objects.nonNull(imageId);
        
        IZosImage zosImage = this.images.get(imageId);
        if (zosImage == null) {
            for(ImageUsage imageUsage : definedImages) {
                if (this.images.containsKey(imageUsage.image.getImageID()) || !imageUsage.image.getImageID().equals(imageId)) {
                    continue;
                }
    
                if (imageUsage.image.allocateImage()) {
                    logger.info(LOG_ZOS_IMAGE + imageUsage.image.getImageID() + " selected with slot name " + imageUsage.image.getSlotName());
                    images.put(imageUsage.image.getImageID(), imageUsage.image);
    
                    return imageUsage.image;
                }
            }
        } else {
            if (zosImage instanceof ZosProvisionedImageImpl) {
                logger.info(LOG_ZOS_IMAGE + zosImage.getImageID() + " selected with slot name " + ((ZosProvisionedImageImpl) zosImage).getSlotName());            
            } else {
                logger.info(LOG_ZOS_IMAGE + zosImage.getImageID() + " selected");                
            }
        }
        if (zosImage == null) {
            throw new ZosManagerException("zOS image \"" + imageId + "\" not defined");
        }
        return zosImage;
    }

    @Override
    public ICredentials getCredentials(String credentialsId, String imageId) throws ZosManagerException {
        ICredentials credentials;
        try {
            ICredentialsService credsService = getFramework().getCredentialsService();

            credentials = credsService.getCredentials(credentialsId);
            if (credentials == null) {
                credentials = credsService.getCredentials(credentialsId.toUpperCase());
            }
        } catch (CredentialsException e) {
            throw new ZosManagerException("Unable to acquire the credentials for id " + credentialsId, e);
        }

        if (credentials == null) {
            throw new ZosManagerException("zOS Credentials missing for image " + imageId + " id " + credentialsId);
        }

        return credentials;
    }

    @Override
    public IZosImage getUnmanagedImage(String imageId) throws ZosManagerException {
        Objects.nonNull(imageId);
        IZosImage zosImage = this.images.get(imageId);
        if (zosImage != null) {
            return zosImage;
        }

        String clusterId = DseClusterIdForTag.get(imageId);
        ZosBaseImageImpl image =  new ZosDseImageImpl(this, imageId, clusterId);
        this.images.put(image.getImageID(), image);
        return image;
    }

    @Override
    public String getRunDatasetHLQ(IZosImage image) throws ZosManagerException {
        Objects.nonNull(image);
        return RunDatasetHLQ.get(image);
    }

    @Override
    public String getRunUNIXPathPrefix(IZosImage image) throws ZosManagerException {
        Objects.nonNull(image);
        return RunUNIXPathPrefix.get(image);
    }
    
    @Override
    public boolean getZosBatchPropertyBatchRestrictToImage(String imageId) throws ZosBatchManagerException {
        return BatchRestrictToImage.get(imageId);
    }

    @Override
    public boolean getZosBatchPropertyUseSysaff(String imageId) throws ZosBatchManagerException {
        return UseSysaff.get(imageId);
    }

    @Override
    public int getZosBatchPropertyJobWaitTimeout(String imageId) throws ZosBatchManagerException {
        return JobWaitTimeout.get(imageId);
    }

    @Override
    public boolean getZosBatchPropertyTruncateJCLRecords(String imageId) throws ZosBatchManagerException {
        return TruncateJCLRecords.get(imageId);
    }

    @Override
    public IZosBatchJobname newZosBatchJobname(IZosImage image) throws ZosBatchException {
        return new ZosBatchJobnameImpl(image);
    }

    @Override
    public IZosBatchJobname newZosBatchJobname(String name) {
        return new ZosBatchJobnameImpl(name);
    }

    @Override
    public IZosBatchJobOutputSpi newZosBatchJobOutput(IZosBatchJob batchJob, String jobname, String jobid) {
        return new ZosBatchJobOutputImpl(batchJob, jobname, jobid);
    }

    @Override
    public IZosBatchJobOutputSpoolFile newZosBatchJobOutputSpoolFile(IZosBatchJob batchJob, String jobname, String jobid, String stepname, String procstep, String ddname, String id, String records) throws ZosBatchException {
        return new ZosBatchJobOutputSpoolFileImpl(batchJob, jobname, jobid, stepname, procstep, ddname, id, records);
    }

    @Override
    public int getZosFilePropertyDirectoryListMaxItems(String imageId) throws ZosFileManagerException {
        return DirectoryListMaxItems.get(imageId);
    }

    @Override
    public boolean getZosFilePropertyFileRestrictToImage(String imageId) throws ZosFileManagerException {
        return FileRestrictToImage.get(imageId);
    }

    @Override
    public String getZosFilePropertyUnixFilePermissions(String imageId) throws ZosFileManagerException {
        return UnixFilePermissions.get(imageId);
    }

    @Override
    public boolean getZosConsolePropertyConsoleRestrictToImage(String imageId) throws ZosConsoleManagerException {
        return ConsoleRestrictToImage.get(imageId);
    }

    @Override
    public String buildUniquePathName(Path artifactPath, String name) {
        int uniqueId = 1;
        while (Files.exists(artifactPath.resolve(name))) {
            Pattern pattern = Pattern.compile("[_][\\d]+$");
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                name = matcher.replaceFirst("_" + Integer.toString(uniqueId));
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(name);
                stringBuilder.append("_");
                stringBuilder.append(uniqueId);
                name = stringBuilder.toString();
            }
            uniqueId++;
        }
        return name;
    }

    @Override
    public void storeArtifact(Path artifactPath, String content, ResultArchiveStoreContentType type) throws ZosManagerException {
        try {
        	if (content == null) {
        		content = "";
        	}
            Files.createFile(artifactPath, type);
            Files.write(artifactPath, content.getBytes());
        } catch (IOException e) {
            throw new ZosManagerException("Unable to store artifact", e);
        }
    }

    @Override
    public void createArtifactDirectory(Path artifactPath) throws ZosManagerException {
        try {
            Files.createDirectories(artifactPath);
        } catch (IOException e) {
            throw new ZosManagerException("Unable to create artifact directory", e);
        }
    }

    public String getRunId() {
        if (this.runid == null) {
            this.runid = getFramework().getTestRunName();
        }
        return this.runid;
    }

    public String getJavaHome(ZosBaseImageImpl image) throws ZosManagerException {
        if (this.javaHome == null) {
            this.javaHome = JavaHome.get(image);
        }
        return this.javaHome;
    }

    public String getLibertyInstallDir(ZosBaseImageImpl image) throws ZosManagerException {
        if (this.zosLibertyInstallDir == null) {
            this.zosLibertyInstallDir = LibertyInstallDir.get(image);
        }
        return this.zosLibertyInstallDir;
    }

    public String getZosConnectInstallDir(ZosBaseImageImpl image) throws ZosManagerException {
        if (this.zosConnectInstallDir == null) {
            this.zosConnectInstallDir = ZosConnectInstallDir.get(image);
        }
        return this.zosConnectInstallDir;
    }
    
    public ZosPoolPorts getZosPortController() {
    	return this.zosPoolPorts;
    }

	@Override
	public HashMap<String, String> getTaggedPorts() {
		return this.taggedPorts;
	}
}
