/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.ipnetwork.spi.IIpNetworkManagerSpi;
import dev.galasa.linux.ILinuxImage;
import dev.galasa.linux.LinuxImage;
import dev.galasa.linux.LinuxIpHost;
import dev.galasa.linux.LinuxManagerException;
import dev.galasa.linux.LinuxManagerField;
import dev.galasa.linux.OperatingSystem;
import dev.galasa.linux.internal.dse.LinuxDSEImage;
import dev.galasa.linux.internal.dse.LinuxDSEProvisioner;
import dev.galasa.linux.internal.properties.LinuxPropertiesSingleton;
import dev.galasa.linux.internal.shared.LinuxSharedImage;
import dev.galasa.linux.internal.shared.LinuxSharedProvisioner;
import dev.galasa.linux.spi.ILinuxManagerSpi;
import dev.galasa.linux.spi.ILinuxProvisioner;

@Component(service = { IManager.class })
public class LinuxManagerImpl extends AbstractManager implements ILinuxManagerSpi {
    public final static String                 NAMESPACE    = "linux";

    private final static Log                   logger       = LogFactory.getLog(LinuxManagerImpl.class);

    private LinuxProperties                    linuxProperties;
    private IConfigurationPropertyStoreService cps;
    private IDynamicStatusStoreService         dss;
    private IIpNetworkManagerSpi               ipManager;

    private final ArrayList<ILinuxProvisioner> provisioners = new ArrayList<>();

    private final HashMap<String, ILinuxImage> taggedImages = new HashMap<>();

    private BundleContext                      bundleContext;

    /*
     * By default we need to load any managers that could provision linux images for
     * us, eg OpenStack
     */
    @Override
    public List<String> extraBundles(@NotNull IFramework framework) throws LinuxManagerException {
        this.linuxProperties = new LinuxProperties(framework);
        return this.linuxProperties.getExtraBundles();
    }

    @Override
    public void registerProvisioner(ILinuxProvisioner provisioner) {
        if (!provisioners.contains(provisioner)) {
            this.provisioners.add(provisioner);
        }
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

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
            List<AnnotatedField> ourFields = findAnnotatedFields(LinuxManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers, galasaTest);
            }
        }

        try {
            this.dss = framework.getDynamicStatusStoreService(NAMESPACE);
            LinuxPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
            this.cps = LinuxPropertiesSingleton.cps();
        } catch (Exception e) {
            throw new LinuxManagerException("Unable to request framework services", e);
        }

        // *** Add our inbuilt provisioners
        this.provisioners.add(new LinuxDSEProvisioner(this));
        this.provisioners.add(new LinuxSharedProvisioner(this));
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
            throw new LinuxManagerException("The IP Network Manager is not available");
        }

        // *** Need to find all the Managers that could provision a Linux image on behalf of us

        try {
            final ServiceReference<?>[] lpServiceReferences = bundleContext
                    .getAllServiceReferences(ILinuxProvisioner.class.getName(), null);

            if (lpServiceReferences == null || lpServiceReferences.length == 0) {
                logger.debug("No Linux provisioners have been found");
            } else {
                for(ServiceReference<?> lpServiceReference : lpServiceReferences) {
                    ILinuxProvisioner linuxProvisioner = (ILinuxProvisioner) this.bundleContext.getService(lpServiceReference);
                    if (linuxProvisioner instanceof IManager) {
                        logger.trace("Found Linux provisioner " + linuxProvisioner.getClass().getName());
                        // *** Tell the provisioner it is required,  does not necessarily mean it will register.
                        ((IManager)linuxProvisioner).youAreRequired(allManagers, activeManagers, galasaTest);
                    }
                }
            }

        } catch(Throwable t) {
            throw new LinuxManagerException("Problem looking for Linux provisioners", t);
        }



    }

    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        for (ILinuxProvisioner provisioner : provisioners) {
            if (provisioner instanceof LinuxDSEProvisioner) {
                continue;
            }
            if (provisioner instanceof LinuxSharedProvisioner) {
                continue;
            }

            if (otherManager == provisioner) {
                return true;
            }
        }
        return super.areYouProvisionalDependentOn(otherManager);
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        // Sort the provisioners in descending order
        Collections.sort(this.provisioners, new ProvisionersComparator());
        
        // *** Get all our annotated fields
        List<AnnotatedField> annotatedFields = findAnnotatedFields(LinuxManagerField.class);

        // *** First, locate all the ILinuxImage fields
        // *** And then generate them
        Iterator<AnnotatedField> annotatedFieldIterator = annotatedFields.iterator();
        while (annotatedFieldIterator.hasNext()) {
            AnnotatedField annotatedField = annotatedFieldIterator.next();
            final Field field = annotatedField.getField();
            final List<Annotation> annotations = annotatedField.getAnnotations();

            if (field.getType() == ILinuxImage.class) {
                LinuxImage annotationLinuxImage = field.getAnnotation(LinuxImage.class);
                if (annotationLinuxImage != null) {
                    ILinuxImage linuxImage = generateLinuxImage(field, annotations);
                    registerAnnotatedField(field, linuxImage);
                }
            }
        }

        // *** Auto generate the remaining fields
        generateAnnotatedFields(LinuxManagerField.class);
    }

    private ILinuxImage generateLinuxImage(Field field, List<Annotation> annotations)
            throws ResourceUnavailableException, LinuxManagerException {
        LinuxImage annotationLinuxImage = field.getAnnotation(LinuxImage.class);

        // *** Default the tag to primary
        String tag = defaultString(annotationLinuxImage.imageTag(), "PRIMARY").toUpperCase();

        // *** Have we already generated this tag
        if (taggedImages.containsKey(tag)) {
            return taggedImages.get(tag);
        }

        // *** Need a new linux image, lets ask the provisioners for one
        OperatingSystem operatingSystem = annotationLinuxImage.operatingSystem();
        if (operatingSystem == null) {
            operatingSystem = OperatingSystem.any;
        }

        String[] capabilities = annotationLinuxImage.capabilities();
        if (capabilities == null) {
            capabilities = new String[0];
        }
        List<String> capabilitiesTrimmed = AbstractManager.trim(capabilities);

        ILinuxImage image = null;
        boolean resourceUnavailable = false;
        for (ILinuxProvisioner provisioner : this.provisioners) {
            try {
                image = provisioner.provisionLinux(tag, operatingSystem, capabilitiesTrimmed);
            } catch (ResourceUnavailableException e) {
                // *** one of the provisioners could have provisioned if there was enough resources
                resourceUnavailable = true;
            } catch (ManagerException e) {
                // *** There must be an error somewhere, put the run into resource wait
                throw new ResourceUnavailableException("Error during resource generate", e);
            }
            if (image != null) {
                break;
            }
        }

        if (image == null) {
            if (resourceUnavailable) {
                throw new ResourceUnavailableException(
                        "There are no linux images available for provisioning the @LinuxImage tagged " + tag);
            } else {
                throw new LinuxManagerException("Unable to provision a Linux image for tag " + tag + " as no provisioners configured with suitable images");
            }
        }

        taggedImages.put(tag, image);

        return image;
    }

    @Override
    public void provisionBuild() throws ManagerException, ResourceUnavailableException {
        super.provisionBuild();

        // *** We need to find all out IIpHosts for the images that we build and have an
        // annotation for

        // *** Get all our annotated fields
        List<AnnotatedField> annotatedFields = findAnnotatedFields(LinuxManagerField.class);

        // *** First, locate all the ILinuxImage fields
        // *** And then generate them
        Iterator<AnnotatedField> annotatedFieldIterator = annotatedFields.iterator();
        while (annotatedFieldIterator.hasNext()) {
            AnnotatedField annotatedField = annotatedFieldIterator.next();
            final Field field = annotatedField.getField();
            final List<Annotation> annotations = annotatedField.getAnnotations();

            if (field.getType() == IIpHost.class) {
                IIpHost iIpHost = generateIpHost(field, annotations);
                registerAnnotatedField(field, iIpHost);
            }
        }

    }
    
    @Override
    public void provisionDiscard() {
        for(ILinuxImage image : this.taggedImages.values()) {
            if (image instanceof LinuxDSEImage) { // dont discard provisioned images from other managers,  let them do it
                ((LinuxDSEImage)image).discard();
            }
            if (image instanceof LinuxSharedImage) { // dont discard provisioned images from other managers,  let them do it
                ((LinuxSharedImage)image).discard();
            }
        }
    }

    public IIpHost generateIpHost(Field field, List<Annotation> annotations) throws LinuxManagerException {
        LinuxIpHost annotationHost = field.getAnnotation(LinuxIpHost.class);

        // *** Default the tag to primary
        String tag = defaultString(annotationHost.imageTag(), "primary");

        // *** Ensure we have this tagged host
        ILinuxImage image = taggedImages.get(tag);
        if (image == null) {
            throw new LinuxManagerException("Unable to provision an IP Host for field " + field.getName()
            + " as no @LinuxImage for the tag '" + tag + "' was present");
        }

        return image.getIpHost();
    }

    public IConfigurationPropertyStoreService getCps() {
        return this.cps;
    }

    public IDynamicStatusStoreService getDss() {
        return this.dss;
    }

    public IIpNetworkManagerSpi getIpNetworkManager() {
        return this.ipManager;
    }

    @Override
    public ILinuxImage getImageForTag(@NotNull String imageTag) throws LinuxManagerException {
        ILinuxImage image = this.taggedImages.get(imageTag);
        if (image == null) {
            throw new LinuxManagerException("Unable to locate Linux image tagged '" + imageTag + "'");
        }
        return image;
    }
    
    private static class ProvisionersComparator implements Comparator<ILinuxProvisioner> {

        @Override
        public int compare(ILinuxProvisioner o1, ILinuxProvisioner o2) {
            return o2.getLinuxPriority() - o1.getLinuxPriority();
        }
        
    }

}
