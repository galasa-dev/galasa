/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IDynamicResource;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.properties.ImageMaxSlots;

public class ZosProvisionedImageImpl extends ZosBaseImageImpl {

    private final static Log logger = LogFactory.getLog(ZosProvisionedImageImpl.class);

    private final IDynamicStatusStoreService dss;
    private final IDynamicResource dynamicResource;

    private String allocatedSlotName;

    public ZosProvisionedImageImpl(ZosManagerImpl zosManager, String imageId, String clusterId) throws ZosManagerException {
        super(zosManager, imageId, clusterId);
        this.dss = zosManager.getDSS();
        this.dynamicResource = this.dss.getDynamicResource("image." + getImageID());
    }

    public boolean hasCapacity() throws ZosManagerException {
        return getCurrentUsage() < 1.0f;
    }

    public Float getCurrentUsage() throws ZosManagerException {
        IDynamicStatusStoreService dss = getZosManager().getDSS();

        float maxSlots = ImageMaxSlots.get(getImageID());
        if (maxSlots <= 0.0f) {
            return 1.0f;
        }

        float usedSlots = 0.0f;
        try {
            String currentSlots = dss.get("image." + getImageID() + ".current.slots");
            if (currentSlots != null) {
                usedSlots = Integer.parseInt(currentSlots);
            }
        } catch (Exception e) {
            throw new ZosManagerException("Problem finding used slots for zOS Image " + getImageID(), e);
        }

        return usedSlots / maxSlots;
    }

    public boolean allocateImage() throws ZosManagerException {
        String runName = getZosManager().getFramework().getTestRunName();

        int maxSlots = ImageMaxSlots.get(getImageID());
        try {
            int usedSlots = 0;
            String currentSlots = dss.get("image." + getImageID() + ".current.slots");
            if (currentSlots != null) {
                usedSlots = Integer.parseInt(currentSlots);
            }

            if (usedSlots >= maxSlots) {
                return false;
            }

            //*** allocate a slot
            usedSlots++;        
            if (!dss.putSwap("image." + getImageID() + ".current.slots", currentSlots, Integer.toString(usedSlots))) {
                //*** The value of the current slots changed whilst this was running,  so we need to try again with the updated value
                Thread.sleep(200); //*** To avoid race conditions
                return allocateImage();
            }

            //*** Now generate a slot name so that we can track who is using all the slots
            //*** As we dont actually care what the slotname is, to make it relevant add the runname as the slotname
            //*** Need to becareful with immediate reruns of the same run name so generate a unique slotname for each runname instance
            String slotName = "SLOT_" + runName;
            for(int i = 0; ;i++) {
                String actualSlotname = slotName;
                if (i > 0) {
                    actualSlotname += "_" + i;
                }

                //*** Try setting the control properties
                String allocated = Instant.now().toString();
                String prefix = "image." + getImageID() + ".slot." + actualSlotname;
                HashMap<String, String> otherProps = new HashMap<>();
                otherProps.put("slot.run." + runName + "." + prefix, "active");
                if (dss.putSwap(prefix, null, runName, otherProps)) {
                    allocatedSlotName = actualSlotname;

                    String resPrefix = "slot." + this.allocatedSlotName;
                    //*** Set the user view properties
                    HashMap<String, String> resProps = new HashMap<>();
                    resProps.put(resPrefix + ".run", runName);
                    resProps.put(resPrefix + ".allocated", allocated);
                    dynamicResource.put(resProps);
                    break;
                }
            }
        } catch (Exception e) {
            throw new ZosManagerException("Problem finding used slots for zOS Image " + getImageID(), e);
        }

        return true;
    }

    public String getSlotName() {
        return this.allocatedSlotName;
    }

    public void freeImage() {
        try {
            String currentSlots = dss.get("image." + getImageID() + ".current.slots");

            if (currentSlots == null) {
                return; // Missing value, no need to update
            }

            int usedSlots = Integer.parseInt(currentSlots);
            usedSlots--;
            if (usedSlots < 0) {
                usedSlots = 0;
            }

            String runName = getZosManager().getFramework().getTestRunName();

            //*** Remove the userview set
            String resPrefix = "slot." + this.allocatedSlotName;
            //*** delete the user view properties
            HashSet<String> resProps = new HashSet<>();
            resProps.add(resPrefix + ".run");
            resProps.add(resPrefix + ".allocated");
            dynamicResource.delete(resProps);

            //*** Remove the control set
            String prefix = "image." + getImageID() + ".slot." + this.allocatedSlotName;
            HashMap<String, String> otherProps = new HashMap<>();
            otherProps.put("slot.run." + getZosManager().getFramework().getTestRunName() + "." + prefix, "free");
            if (!dss.putSwap("image." + getImageID() + ".current.slots", currentSlots, Integer.toString(usedSlots), otherProps)) {
                //*** The value of the current slots changed whilst this was running,  so we need to try again with the updated value
                Thread.sleep(200); //*** To avoid race conditions
                freeImage();
                return;
            }

            HashSet<String> delProps = new HashSet<>();
            delProps.add("slot.run." + runName + "." + prefix);
            delProps.add(prefix);
            dss.delete(delProps);

            dss.delete("image." + getImageID() + ".current.slot." + this.allocatedSlotName);

            logger.info("Discard slot name " + this.allocatedSlotName + " for zOS Image " + getImageID());
        } catch (Exception e) {
            logger.warn("Failed to free slot on image " + getImageID() + ", slot " + this.allocatedSlotName + ", leaving for manager clean up routines", e);
        }
    }

    public static void deleteDss(String runName, String imageId, String slot, IDynamicStatusStoreService dss) {
        //*** Have to be careful with the delete as we need to keep the current.slots count correct

        try {
            //*** First clear the User view properties
            IDynamicResource dynamicResource = dss.getDynamicResource("image." + imageId);

            String resPrefix = "slot." + slot;

            HashSet<String> resProps = new HashSet<>();
            resProps.add(resPrefix + ".run");
            resProps.add(resPrefix + ".allocated");
            dynamicResource.delete(resProps);

            //*** Now get the run slot to see if it is still active,   if so,  try to switch to free
            String prefix = "image." + imageId + ".slot." + slot;
            String runSlot = dss.get("slot.run." + runName + "." + prefix);
            if ("active".equals(runSlot)) {  //*** The slot is still active so try and free it
                if (dss.putSwap("slot.run." + runName + "." + prefix, "active", "free")) {
                    //*** Managed it,   decrement the current slots by 1,  may need a couple attempts as someone else may be updating
                    while(true) {
                        String sCurrentSlots = dss.get("image." + imageId + ".current.slots");
                        int currentSlots = Integer.parseInt(sCurrentSlots);
                        currentSlots--;
                        if (currentSlots < 0) {
                            currentSlots = 0;
                        }

                        if (dss.putSwap("image." + imageId + ".current.slots", sCurrentSlots, Integer.toString(currentSlots))) {
                            break;
                        }

                        Thread.sleep(100);
                    }
                }
            }

            //*** Delete the slot records
            HashSet<String> props = new HashSet<>();
            props.add(prefix);
            props.add("slot.run." + runName + "." + prefix);
            dss.delete(props);
        } catch(Exception e) {
            logger.error("Failed to discard slot " + slot + " on image " + imageId,e);
        }

    }
}
