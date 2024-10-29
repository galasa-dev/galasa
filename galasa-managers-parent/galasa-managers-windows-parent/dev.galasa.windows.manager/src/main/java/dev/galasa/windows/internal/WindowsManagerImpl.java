/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.windows.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
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
import dev.galasa.windows.IWindowsImage;
import dev.galasa.windows.WindowsImage;
import dev.galasa.windows.WindowsIpHost;
import dev.galasa.windows.WindowsManagerException;
import dev.galasa.windows.WindowsManagerField;
import dev.galasa.windows.internal.properties.WindowsPropertiesSingleton;
import dev.galasa.windows.spi.IWindowsManagerSpi;
import dev.galasa.windows.spi.IWindowsProvisioner;

@Component(service = { IManager.class })
public class WindowsManagerImpl extends AbstractManager implements IWindowsManagerSpi {
    protected final static String              NAMESPACE    = "windows";

    private final static Log                   logger       = LogFactory.getLog(WindowsManagerImpl.class);

    private WindowsProperties                    windowsProperties;
    private IConfigurationPropertyStoreService cps;
    private IDynamicStatusStoreService         dss;
    private IIpNetworkManagerSpi               ipManager;

    private final ArrayList<IWindowsProvisioner> provisioners = new ArrayList<>();

    private final HashMap<String, IWindowsImage> taggedImages = new HashMap<>();

    private BundleContext                      bundleContext;

    /*
     * By default we need to load any managers that could provision windows images for
     * us, eg OpenStack
     */
    @Override
    public List<String> extraBundles(@NotNull IFramework framework) throws WindowsManagerException {
        this.windowsProperties = new WindowsProperties(framework);
        return this.windowsProperties.getExtraBundles();
    }

    @Override
    public void registerProvisioner(IWindowsProvisioner provisioner) {
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
            List<AnnotatedField> ourFields = findAnnotatedFields(WindowsManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers, galasaTest);
            }
        }

        try {
            this.dss = framework.getDynamicStatusStoreService(NAMESPACE);
            WindowsPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
            this.cps = WindowsPropertiesSingleton.cps();
        } catch (Exception e) {
            throw new WindowsManagerException("Unable to request framework services", e);
        }

        // *** Ensure our DSE Provisioner is at the top of the list
        this.provisioners.add(0, new WindowsDSEProvisioner(this));
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
            throw new WindowsManagerException("The IP Network Manager is not available");
        }

        // *** Need to find all the Managers that could provision a Windows image on behalf of us

        try {
            final ServiceReference<?>[] lpServiceReferences = bundleContext
                    .getAllServiceReferences(IWindowsProvisioner.class.getName(), null);

            if (lpServiceReferences == null || lpServiceReferences.length == 0) {
                logger.debug("No Windows provisioners have been found");
            } else {
                for(ServiceReference<?> lpServiceReference : lpServiceReferences) {
                    IWindowsProvisioner windowsProvisioner = (IWindowsProvisioner) this.bundleContext.getService(lpServiceReference);
                    if (windowsProvisioner instanceof IManager) {
                        logger.trace("Found Windows provisioner " + windowsProvisioner.getClass().getName());
                        // *** Tell the provisioner it is required,  does not necessarily mean it will register.
                        ((IManager)windowsProvisioner).youAreRequired(allManagers, activeManagers, galasaTest);
                    }
                }
            }

        } catch(Throwable t) {
            throw new WindowsManagerException("Problem looking for Windows provisioners", t);
        }



    }

    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        for (IWindowsProvisioner provisioner : provisioners) {
            if (provisioner instanceof WindowsDSEProvisioner) {
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
        // *** First add our default provisioning agent to the end
        this.provisioners.add(new WindowsDefaultProvisioner());

        // *** Get all our annotated fields
        List<AnnotatedField> annotatedFields = findAnnotatedFields(WindowsManagerField.class);

        // *** First, locate all the IWindowsImage fields
        // *** And then generate them
        Iterator<AnnotatedField> annotatedFieldIterator = annotatedFields.iterator();
        while (annotatedFieldIterator.hasNext()) {
            AnnotatedField annotatedField = annotatedFieldIterator.next();
            final Field field = annotatedField.getField();
            final List<Annotation> annotations = annotatedField.getAnnotations();

            if (field.getType() == IWindowsImage.class) {
                WindowsImage annotationWindowsImage = field.getAnnotation(WindowsImage.class);
                if (annotationWindowsImage != null) {
                    IWindowsImage windowsImage = generateWindowsImage(field, annotations);
                    registerAnnotatedField(field, windowsImage);
                }
            }
        }

        // *** Auto generate the remaining fields
        generateAnnotatedFields(WindowsManagerField.class);
    }

    private IWindowsImage generateWindowsImage(Field field, List<Annotation> annotations)
            throws ResourceUnavailableException, WindowsManagerException {
        WindowsImage annotationWindowsImage = field.getAnnotation(WindowsImage.class);

        // *** Default the tag to primary
        String tag = defaultString(annotationWindowsImage.imageTag(), "PRIMARY").toUpperCase();

        // *** Have we already generated this tag
        if (taggedImages.containsKey(tag)) {
            return taggedImages.get(tag);
        }

        String[] capabilities = annotationWindowsImage.capabilities();
        if (capabilities == null) {
            capabilities = new String[0];
        }
        List<String> capabilitiesTrimmed = AbstractManager.trim(capabilities);

        IWindowsImage image = null;
        boolean resourceUnavailable = false;
        for (IWindowsProvisioner provisioner : this.provisioners) {
            try {
                image = provisioner.provisionWindows(tag, capabilitiesTrimmed);
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
                        "There are no windows images available for provisioning the @WindowsImage tagged " + tag);
            } else {
                throw new WindowsManagerException("Unable to provision a Windows image for tag " + tag + " as no provisioners configured with suitable images");
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
        List<AnnotatedField> annotatedFields = findAnnotatedFields(WindowsManagerField.class);

        // *** First, locate all the IWindowsImage fields
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

    public IIpHost generateIpHost(Field field, List<Annotation> annotations) throws WindowsManagerException {
        WindowsIpHost annotationHost = field.getAnnotation(WindowsIpHost.class);

        // *** Default the tag to primary
        String tag = defaultString(annotationHost.imageTag(), "primary");

        // *** Ensure we have this tagged host
        IWindowsImage image = taggedImages.get(tag);
        if (image == null) {
            throw new WindowsManagerException("Unable to provision an IP Host for field " + field.getName()
            + " as no @WindowsImage for the tag '" + tag + "' was present");
        }

        return image.getIpHost();
    }

    protected IConfigurationPropertyStoreService getCps() {
        return this.cps;
    }

    protected IDynamicStatusStoreService getDss() {
        return this.dss;
    }

    protected IIpNetworkManagerSpi getIpNetworkManager() {
        return this.ipManager;
    }

    @Override
    public IWindowsImage getImageForTag(@NotNull String imageTag) throws WindowsManagerException {
        IWindowsImage image = this.taggedImages.get(imageTag);
        if (image == null) {
            throw new WindowsManagerException("Unable to locate Windows image tagged '" + imageTag + "'");
        }
        return image;
     }

}
