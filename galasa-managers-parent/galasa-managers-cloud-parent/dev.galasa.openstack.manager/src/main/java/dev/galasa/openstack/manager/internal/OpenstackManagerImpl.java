/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.osgi.service.component.annotations.Component;

import com.google.gson.Gson;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DssAdd;
import dev.galasa.framework.spi.DssSwap;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
import dev.galasa.framework.spi.IDssAction;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IResourcePoolingService;
import dev.galasa.framework.spi.InsufficientResourcesAvailableException;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.framework.spi.utils.GalasaGson;
import dev.galasa.ipnetwork.spi.IIpNetworkManagerSpi;
import dev.galasa.linux.LinuxManagerException;
import dev.galasa.linux.OperatingSystem;
import dev.galasa.linux.spi.ILinuxManagerSpi;
import dev.galasa.linux.spi.ILinuxProvisionedImage;
import dev.galasa.linux.spi.ILinuxProvisioner;
import dev.galasa.openstack.manager.OpenstackManagerException;
import dev.galasa.openstack.manager.internal.properties.LinuxImageCapabilities;
import dev.galasa.openstack.manager.internal.properties.LinuxImages;
import dev.galasa.openstack.manager.internal.properties.MaximumInstances;
import dev.galasa.openstack.manager.internal.properties.NamePool;
import dev.galasa.openstack.manager.internal.properties.OpenStackEnabled;
import dev.galasa.openstack.manager.internal.properties.OpenStackLinuxPriority;
import dev.galasa.openstack.manager.internal.properties.OpenstackPropertiesSingleton;
import dev.galasa.openstack.manager.internal.properties.WindowsImageCapabilities;
import dev.galasa.openstack.manager.internal.properties.WindowsImages;
import dev.galasa.windows.spi.IWindowsManagerSpi;
import dev.galasa.windows.spi.IWindowsProvisionedImage;
import dev.galasa.windows.spi.IWindowsProvisioner;

@Component(service = { IManager.class, ILinuxProvisioner.class, IWindowsProvisioner.class })
public class OpenstackManagerImpl extends AbstractManager implements ILinuxProvisioner, IWindowsProvisioner {
    protected final static String                    NAMESPACE = "openstack";

    private final static Log                         logger    = LogFactory.getLog(OpenstackManagerImpl.class);

    private IDynamicStatusStoreService               dss;
    private IIpNetworkManagerSpi                     ipManager;
    private ILinuxManagerSpi                         linuxManager;
    private IWindowsManagerSpi                       windowsManager;

    private final ArrayList<OpenstackServerImpl> instances = new ArrayList<>();

    private CloseableHttpClient                      httpClient;
    private OpenstackHttpClient                      openstackHttpClient;

    private GalasaGson                               gson      = new GalasaGson();

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

        try {
            this.dss = framework.getDynamicStatusStoreService(NAMESPACE);
            OpenstackPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
            this.openstackHttpClient = new OpenstackHttpClient(framework);
        } catch (Exception e) {
            throw new LinuxManagerException("Unable to request framework services", e);
        }

        this.httpClient = HttpClients.createDefault();
    }

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);

        // *** Absolutely need the IP Network manager
        this.ipManager = addDependentManager(allManagers, activeManagers, galasaTest, IIpNetworkManagerSpi.class);
        if (this.ipManager == null) {
            throw new LinuxManagerException("The IP Network Manager is not available");
        }

        // *** Check if Linux is loaded
        this.linuxManager = addDependentManager(allManagers, activeManagers, galasaTest, ILinuxManagerSpi.class);
        if (this.linuxManager != null) {
            this.linuxManager.registerProvisioner(this);
        }

        // *** Check if Windows is loaded
        this.windowsManager = addDependentManager(allManagers, activeManagers, galasaTest, IWindowsManagerSpi.class);
        if (this.windowsManager != null) {
            this.windowsManager.registerProvisioner(this);
        }

    }

    @Override
    public void provisionBuild() throws ManagerException, ResourceUnavailableException {
        for (OpenstackServerImpl instance : instances) {
            try {
                instance.build();
            } catch (ConfigurationPropertyStoreException e) {
                throw new OpenstackManagerException("Problem building OpenStack servers", e);
            }
        }
    }

    @Override
    public void provisionDiscard() {

        for (OpenstackServerImpl instance : instances) {
            instance.discard();
        }

        if (this.httpClient != null) {
            try {
                this.httpClient.close();
            } catch (IOException e) { // Ignore error, not much we can do
            }
        }
    }

    @Override
    public ILinuxProvisionedImage provisionLinux(String tag, OperatingSystem operatingSystem, List<String> capabilities)
            throws OpenstackManagerException, ResourceUnavailableException {

        // check we are enabled
        if (!OpenStackEnabled.get()) {
            logger.trace("OpenStack not enabled");
            return null;
        }

        // *** Check that we can connect to openstack before we attempt to provision, if
        // we can't end gracefully and give someone else a chance
        if (!openstackHttpClient.connectToOpenstack()) {
            logger.trace("Unable to connect to OpenStack");
            return null;
        }

        logger.trace("Locating possible images that are available for selection");
        try {
            List<String> possibleImages = LinuxImages.get(operatingSystem, null);

            Iterator<String> possibleImagesIterator = possibleImages.iterator();
            nextImage:
            while(possibleImagesIterator.hasNext()) {
                String image = possibleImagesIterator.next();
                logger.trace("Checking if image " + image + " is correct for this test");

                // First check to see the the tests MUST request a capability this server provides
                List<String> availableCapabilities = LinuxImageCapabilities.get(image);
                if (!availableCapabilities.isEmpty()) {
                    for(String availableCapability : availableCapabilities) {
                        logger.trace(availableCapability + " is an available capability of this image");
                        if (availableCapability.startsWith("+")) {
                            String actualAvailableCapability = availableCapability.substring(1);
                            boolean requestedCapability = false;
                            for(String choosenCapability : capabilities) {
                                if (choosenCapability.equalsIgnoreCase(actualAvailableCapability)) {
                                    logger.trace("This image has an available capability " + actualAvailableCapability + " that matches a chosen capability " + choosenCapability);
                                    requestedCapability = true;
                                    break;
                                } else {
                                    logger.trace("This image's available capability " + availableCapability + " is not required");
                                }
                            }
                            if (!requestedCapability) {
                                logger.trace("This image had no availabilie capabilities that were chosen for this test");
                                possibleImagesIterator.remove();
                                continue nextImage;
                            }
                        }
                    }
                }

                // Now check the server provides the capabilities requested
                if (!capabilities.isEmpty()) {
                    for(String choosenCapability : capabilities) {
                        if (choosenCapability == null || choosenCapability.isEmpty()) {
                            continue;
                        }

                        boolean found = false;
                        for (String availableCapability : availableCapabilities) {
                            if (availableCapability.startsWith("+")) {
                                availableCapability = availableCapability.substring(1);
                            }
                            if (availableCapability.equalsIgnoreCase(choosenCapability)) {
                                logger.trace("This image has an available capability " + availableCapability + " that matches a required capability " + choosenCapability);
                                found = true;
                                break;
                            } else {
                                logger.trace("This image's available capability " + availableCapability + " is not required");
                            }
                        }

                        if (!found) {
                            logger.trace("This image had no available capabilities that we need so it is not possible to use");
                            possibleImagesIterator.remove();
                            continue nextImage;
                        }
                    }
                }
            }


            // *** Are there any images left? if not return gracefully as some other
            // provisioner may be able to support it
            if (possibleImages.isEmpty()) {
                return null;
            }

            // *** Select the first image as they will be listed in preference order
            String selectedImage = possibleImages.get(0);
            logger.trace("The selected image for this test is " + selectedImage);

            // *** See if we have capacity for a new Instance on Openstack
            String instanceName = reserveInstance();

            // *** We have one, return it
            OpenstackLinuxImageImpl instance = new OpenstackLinuxImageImpl(this, this.openstackHttpClient, instanceName,
                    selectedImage, tag);
            this.instances.add(instance);

            logger.info("Reserved OpenStack Linux instance " + instanceName + " with image " + selectedImage
                    + " for tag " + tag);

            return instance;
        } catch (ConfigurationPropertyStoreException e) {
            throw new OpenstackManagerException("Problem accessing the CPS", e);
        } catch (DynamicStatusStoreException e) {
            throw new OpenstackManagerException("Problem accessing the DSS", e);
        } catch (InsufficientResourcesAvailableException e) {
            throw new ResourceUnavailableException("Ran out of slot names", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OpenstackManagerException("Processing interrupted", e);
        }
    }

    @Override
    public IWindowsProvisionedImage provisionWindows(String tag, List<String> capabilities)
            throws OpenstackManagerException, ResourceUnavailableException {

        // check we are enabled
        if (!OpenStackEnabled.get()) {
            return null;
        }

        // *** Check that we can connect to openstack before we attempt to provision, if
        // we can't end gracefully and give someone else a chance
        if (!openstackHttpClient.connectToOpenstack()) {
            return null;
        }

        // *** Locate the possible images that are available for selection
        try {
            List<String> possibleImages = WindowsImages.get(null);

            // *** Filter out those that don't have the necessary capabilities
            if (!capabilities.isEmpty()) {
                Iterator<String> imageIterator = possibleImages.iterator();
                imageSearch: while (imageIterator.hasNext()) {
                    String image = imageIterator.next();
                    List<String> imageCapabilities = WindowsImageCapabilities.get(image);
                    for (String requestedCapability : capabilities) {
                        if (!imageCapabilities.contains(requestedCapability)) {
                            imageIterator.remove();
                            continue imageSearch;
                        }
                    }
                }
            }

            // *** Are there any images left? if not return gracefully as some other
            // provisioner may be able to support it
            if (possibleImages.isEmpty()) {
                return null;
            }

            // *** Select the first image as they will be listed in preference order
            String selectedImage = possibleImages.get(0);

            // *** See if we have capacity for a new Instance on Openstack
            String instanceName = reserveInstance();

            // *** We have one, return it
            OpenstackWindowsImageImpl instance = new OpenstackWindowsImageImpl(this, this.openstackHttpClient, instanceName,
                    selectedImage, tag);
            this.instances.add(instance);

            logger.info("Reserved OpenStack Windows instance " + instanceName + " with image " + selectedImage
                    + " for tag " + tag);

            return instance;
        } catch (ConfigurationPropertyStoreException e) {
            throw new OpenstackManagerException("Problem accessing the CPS", e);
        } catch (DynamicStatusStoreException e) {
            throw new OpenstackManagerException("Problem accessing the DSS", e);
        } catch (InsufficientResourcesAvailableException e) {
            throw new ResourceUnavailableException("Ran out of slot names", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OpenstackManagerException("Processing interrupted", e);
        }
    }

    @NotNull
    private String reserveInstance() throws DynamicStatusStoreException, InterruptedException,
    InsufficientResourcesAvailableException, ConfigurationPropertyStoreException, OpenstackManagerException {

        // *** Get the runname for reserving slot names
        String runName = this.getFramework().getTestRunName();

        List<String> instanceNamePool = NamePool.get();
        IResourcePoolingService poolingService = this.getFramework().getResourcePoolingService();

        // *** Get the current and maximum instances
        int maxInstances = MaximumInstances.get();

        int currentInstances = 0;

        String sCurrentInstances = this.dss.get("server.current.compute.instances");
        if (sCurrentInstances != null) {
            currentInstances = Integer.parseInt(sCurrentInstances);
        }

        // *** Is there room?
        if (maxInstances <= currentInstances) {
            throw new InsufficientResourcesAvailableException("At max slots");
        }


        // *** reserve a slot and allocate a new name
        currentInstances++;
        IDssAction slotNumber = null;
        if (sCurrentInstances != null) {
            slotNumber = new DssSwap("server.current.compute.instances", sCurrentInstances, Integer.toString(currentInstances));
        } else {
            slotNumber = new DssAdd("server.current.compute.instances", Integer.toString(currentInstances));
        }

        // *** Get a list of potential compute IDs
        ArrayList<String> exclude = new ArrayList<>();
        List<String> possibleNames = poolingService.obtainResources(instanceNamePool, exclude, 10, 1, this.dss,
                "compute.");

        if (possibleNames.isEmpty()) {
            throw new InsufficientResourcesAvailableException("Insufficient Compute names available");
        }

        // take the first one
        String instanceName = "compute." + possibleNames.remove(0);

        // add active and ownership
        DssAdd computeId = new DssAdd(instanceName, runName);
        DssAdd runInstance = new DssAdd("run." + runName + "." + instanceName, "active");




        try {
            this.dss.performActions(slotNumber, computeId, runInstance);
        } catch(DynamicStatusStoreMatchException e) {
            //*** collision on either the slot increment or the instance name,  so simply retry
            Thread.sleep(200 + new SecureRandom().nextInt(200)); // *** To avoid race conditions
            return reserveInstance();
        }

        return instanceName;
    }

    public IDynamicStatusStoreService getDSS() {
        return this.dss;
    }

    protected Gson getGson() {
        return this.gson.getGson();
    }

    protected IIpNetworkManagerSpi getIpNetworkManager() {
        return this.ipManager;
    }

    @Override
    public int getLinuxPriority() {
        return OpenStackLinuxPriority.get();
    }

}
