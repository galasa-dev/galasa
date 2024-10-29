/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux.internal.shared;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.DssAdd;
import dev.galasa.framework.spi.DssSwap;
import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
import dev.galasa.framework.spi.IDssAction;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IResourcePoolingService;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.linux.LinuxManagerException;
import dev.galasa.linux.OperatingSystem;
import dev.galasa.linux.internal.LinuxManagerImpl;
import dev.galasa.linux.internal.properties.LinuxCapabilities;
import dev.galasa.linux.internal.properties.LinuxOperatingSystem;
import dev.galasa.linux.internal.properties.MaximumSlots;
import dev.galasa.linux.internal.properties.SharedLinuxImages;
import dev.galasa.linux.internal.properties.SharedLinuxPriority;
import dev.galasa.linux.internal.properties.UsernamePool;
import dev.galasa.linux.spi.ILinuxProvisionedImage;
import dev.galasa.linux.spi.ILinuxProvisioner;

public class LinuxSharedProvisioner implements ILinuxProvisioner {

    private final Log                                logger = LogFactory.getLog(getClass());

    private final LinuxManagerImpl                   manager;
    private final IDynamicStatusStoreService         dss;

    public LinuxSharedProvisioner(LinuxManagerImpl manager) {

        this.manager = manager;
        this.dss = this.manager.getDss();
    }

    @Override
    public ILinuxProvisionedImage provisionLinux(String tag, OperatingSystem operatingSystem, List<String> capabilities)
            throws LinuxManagerException, ResourceUnavailableException {
        return provisionLinux(tag, operatingSystem, capabilities, 1);
    }

    private ILinuxProvisionedImage provisionLinux(String tag, OperatingSystem operatingSystem, List<String> capabilities, int retryCount)
                throws LinuxManagerException, ResourceUnavailableException {
        
        if (retryCount > 10) {
            // tried too many times,  allow another provisioner to attempt
            throw new ResourceUnavailableException("Failed to reserve shared linux image");
        }
        

        try {
            List<String> images = SharedLinuxImages.get();
            if (images.isEmpty()) {
                return null;
            }

            List<AvailableImage>  availableImages = new ArrayList<AvailableImage>(images.size());
            for(String image : images) {
                if (image == null || image.isEmpty()) {
                    continue;
                }

                availableImages.add(new AvailableImage(image));
            }

            // Remove all those servers that do not match the operating system or doesn't have the capabilities
            String choosenOperatingSystem = null;
            if (operatingSystem != OperatingSystem.any) {
                choosenOperatingSystem = operatingSystem.name();
            }
            Iterator<AvailableImage> availableImagesIterator = availableImages.iterator();
            nextImage:
            while(availableImagesIterator.hasNext()) {
                AvailableImage image = availableImagesIterator.next();

                if (choosenOperatingSystem != null) {
                    String availableOperatingSystem = LinuxOperatingSystem.get(image.getHostId());
                    if (availableOperatingSystem == null) {
                        availableImagesIterator.remove();
                        continue;
                    }
                    if (!availableOperatingSystem.equalsIgnoreCase(choosenOperatingSystem)) {
                        availableImagesIterator.remove();
                        continue;
                    }
                }
                
                // First check to see the the tests MUST request a capability this server provides
                List<String> availableCapabilities = LinuxCapabilities.get(image.getHostId());
                if (!availableCapabilities.isEmpty()) {
                    for(String availableCapability : availableCapabilities) {
                        if (availableCapability.startsWith("+")) {
                            String actualAvailableCapability = availableCapability.substring(1);
                            boolean requestedCapability = false;
                            for(String choosenCapability : capabilities) {
                                if (choosenCapability.equalsIgnoreCase(actualAvailableCapability)) {
                                    requestedCapability = true;
                                    break;
                                }
                            }
                            if (!requestedCapability) {
                                availableImagesIterator.remove();
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
                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            availableImagesIterator.remove();
                            continue nextImage;
                        }
                    }
                }
            }

            // Are there any suitable images left?
            if (availableImages.isEmpty()) {
                logger.trace("There are no suitable shared Linux images available");
                return null;
            }

            // Find the image with the greatest capacity

            availableImagesIterator = availableImages.iterator();
            while(availableImagesIterator.hasNext()) {
                AvailableImage image = availableImagesIterator.next();
                String hostId = image.getHostId();

                int maxSlots = MaximumSlots.get(hostId);
                if (maxSlots < 1) {
                    availableImagesIterator.remove();
                    continue;
                }

                String sUsedSlots = this.dss.get(hostId + ".used.slots");
                if (sUsedSlots == null || sUsedSlots.isEmpty()) {
                    sUsedSlots = "0";
                }

                int usedSlots = Integer.parseInt(sUsedSlots);
                if (usedSlots >= maxSlots) {
                    availableImagesIterator.remove();
                    continue;
                } else {
                    float freePercentage = ((float)maxSlots - (float)usedSlots) / (float)maxSlots * 100.0f;
                    image.setSpareCapacityPercentage(Math.round(freePercentage));
                }
            }
            
            if (availableImages.isEmpty()) {
                logger.trace("There are no suitable shared Linux images with spare capacity");
                throw new ResourceUnavailableException("Failed to reserve shared linux image");
            }

            Collections.sort(availableImages);
            
            AvailableImage selectedImage = availableImages.get(0);
            
            String hostId = selectedImage.hostId;
            // Reserve a slot
            int maxSlots = MaximumSlots.get(hostId);
            if (maxSlots < 1) {
                // must have just changed,  redrive the method again
                return provisionLinux(tag, operatingSystem, capabilities, retryCount++);
            }

            int usedSlots = 0;
            String sUsedSlots = this.dss.get(hostId + ".used.slots");
            if (sUsedSlots != null) {
                usedSlots = Integer.parseInt(sUsedSlots);
            }
            
            usedSlots++;
            
            if (usedSlots > maxSlots) {
                // must have just changed,  redrive the method again
                return provisionLinux(tag, operatingSystem, capabilities, retryCount++);
            }
            
            String runName = this.manager.getFramework().getTestRunName();
            
            IDssAction slotsUpdate = null;
            if (sUsedSlots == null) {
                slotsUpdate = new DssAdd(hostId + ".used.slots", Integer.toString(usedSlots));
            } else {
                slotsUpdate = new DssSwap(hostId + ".used.slots", sUsedSlots, Integer.toString(usedSlots));
            }
            DssAdd runImage = new DssAdd("run." + runName + ".image." + hostId, "active");

            // We needs a unique userid to use on the shared linux box, so reserve that at the sametime
            
            List<String> instanceNamePool = UsernamePool.get(hostId);
            IResourcePoolingService poolingService = this.manager.getFramework().getResourcePoolingService();
            
            // *** Get a list of potential usernames
            ArrayList<String> exclude = new ArrayList<>();
            List<String> possibleNames = poolingService.obtainResources(instanceNamePool, exclude, 10, 1, this.dss,
                    "image." + hostId + ".username.");

            if (possibleNames.isEmpty()) {
                // no available usernames on this image,   there should always be more usernames than slots
                return provisionLinux(tag, operatingSystem, capabilities, retryCount++);
            }

            String possibleUsername = possibleNames.get(0);
            
            // add active and ownership
            DssAdd username = new DssAdd("image." + hostId + ".username." + possibleUsername, runName);
            DssAdd runUsername = new DssAdd("run." + runName + ".image." + hostId + ".username." + possibleUsername, "active");
            
            try {
                this.dss.performActions(slotsUpdate, runImage, username, runUsername);
            } catch(DynamicStatusStoreMatchException e) {
                //*** collision on either the slot increment or the username,  so simply retry
                Thread.sleep(200 + new SecureRandom().nextInt(200)); // *** To avoid race conditions
                return provisionLinux(tag, operatingSystem, capabilities, retryCount++);
            }
            
            logger.info("Selected shared Linux image for " + tag + " host id is " + hostId + ", with username " + possibleUsername);

            return new LinuxSharedImage(manager, tag, hostId, possibleUsername);
        } catch (Exception e) {
            throw new LinuxManagerException("Unable to provision the Linux DSE", e);
        }
    }

    @Override
    public int getLinuxPriority() {
        return SharedLinuxPriority.get();
    }


    private static class AvailableImage implements Comparable<AvailableImage> {

        private final String hostId;
        private int spareCapacityPercentage = 0;

        private AvailableImage(String hostId) {
            this.hostId = hostId;
        }

        private void setSpareCapacityPercentage(int spareCapacityPercentage) {
            this.spareCapacityPercentage = spareCapacityPercentage;
        }

        private String getHostId() {
            return hostId;
        }

        @Override
        public int compareTo(AvailableImage o) {
            return o.spareCapacityPercentage - this.spareCapacityPercentage;
        }

    }
    
    

}
