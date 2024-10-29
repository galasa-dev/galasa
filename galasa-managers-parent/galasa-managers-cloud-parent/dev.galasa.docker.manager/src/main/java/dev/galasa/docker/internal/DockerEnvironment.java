/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.docker.DockerContainer;
import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.DockerProvisionException;
import dev.galasa.docker.internal.properties.DockerSlots;
import dev.galasa.framework.spi.DssAdd;
import dev.galasa.framework.spi.DssDelete;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicResource;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;

/**
 * Docker Environment. Manages the flow of both docker containers and slots to a
 * specified docker engine
 * 
 *   
 */
public class DockerEnvironment implements IDockerEnvironment {
    private IFramework framework;
    private DockerManagerImpl dockerManager;
    private IDynamicStatusStoreService dss;
    private IDynamicResource dynamicResource;
    private Map<String, DockerContainerImpl> containersByTag = new HashMap<>();
    private Map<String, DockerEngineImpl> enginesByTag = new HashMap<>();
    private boolean dockerEnginesChecked;
    private List<DockerVolumeImpl> volumes = new ArrayList<>();

    private final static Log logger = LogFactory.getLog(DockerEnvironment.class);

    /**
     * Sets the environment up to use the dss in the docker namespace.
     * 
     * @param framework
     * @param dockerManager
     * @throws DockerManagerException
     */
    public DockerEnvironment(IFramework framework, DockerManagerImpl dockerManager) throws DockerManagerException {
        this.framework = framework;
        this.dockerManager = dockerManager;

        try {
            this.dss = framework.getDynamicStatusStoreService(dockerManager.NAMESPACE);
        } catch (DynamicStatusStoreException e) {
            throw new DockerManagerException("Failed to create Docker environment", e);
        }

    }

    /**
     * Provisions the specified docker containers and stored the tag.
     * 
     * @param tag
     * @param imageName
     * @param start     (boolean)
     * @return DockerContainerImpl
     * @throws DockerProvisionException
     */
    @Override
    public DockerContainerImpl provisionDockerContainer(String tag, String imageName, boolean start,
            String dockerEngineTag) throws DockerProvisionException {
        DockerContainerImpl container = containersByTag.get(tag);
        DockerEngineImpl engine = enginesByTag.get(dockerEngineTag);
        if (container != null) {
            logger.info("Container already provisioned: " + tag);
            return container;
        }

        if (engine == null) {
            engine = buildDockerEngine(dockerEngineTag);
            enginesByTag.put(dockerEngineTag, engine);
        }

        if (dockerEnginesChecked != true) {
            engine.checkEngine();
            dockerEnginesChecked = true;
        }

        try {
            DockerSlotImpl slot = provisionDockerSlot(engine);

            DockerImageImpl image = new DockerImageImpl(framework, dockerManager, engine, imageName);

            container = new DockerContainerImpl(framework, dockerManager, tag, engine, image, start, slot);
            containersByTag.put(tag, container);

            logger.debug("Docker Container '" + tag + "' was provisioned as slot '"
                    + container.getDockerSlot().getSlotName());

            return container;
        } catch (DockerManagerException e) {
            throw new DockerProvisionException("Failed to gather resources.", e);
        }
    }

    /**
     * Builds all the recorded docker containers
     * 
     * @param testClasses
     * @throws DockerProvisionException
     */
    @Override
    public void build(List<Class<?>> testClasses) throws DockerProvisionException {
        if (!enginesByTag.isEmpty()) {
            checkDockerEngines();
            dockerEnginesChecked = true;
        }
        // if (this.dockerEngine != null) {
        // this.dockerEngine.checkEngine();
        // dockerEnginesChecked = true;
        // }

        for (DockerContainerImpl container : getContainers()) {
            container.checkContainer();
        }
    }

    private void checkDockerEngines() throws DockerProvisionException {
        for (String id : enginesByTag.keySet()) {
            enginesByTag.get(id).checkEngine();
        }
    }

    /**
     * Discrads the entire docker environment, stopping and deleting all docker
     * containers in instance and volumes.
     * 
     * @throws DockerManagerException
     */
    @Override
    public void discard() throws DockerManagerException {
        for (DockerContainerImpl container : containersByTag.values()) {
            container.discard();
        }
        for (DockerVolumeImpl volume : volumes) {
            removeDockerVolume(volume);
        }
    }

    /**
     * Returns the docker engine.
     */
    @Override
    public DockerEngineImpl getDockerEngineImpl(String dockerEngineTag) throws DockerManagerException {
        if (enginesByTag.containsKey(dockerEngineTag)) {
            return enginesByTag.get(dockerEngineTag);
        }
        throw new DockerManagerException("Unable to find Docker engine with the tag: " + dockerEngineTag);
    }

    /**
     * Returns the container implementation for a given tag
     * 
     * @param dockerContainerTag
     * @return DockerContainerImpl
     * @throws DockerManagerException
     */
    @Override
    public DockerContainerImpl getDockerContainerImpl(String dockerContainerTag) throws DockerManagerException {
        if (containersByTag.containsKey(dockerContainerTag)) {
            return containersByTag.get(dockerContainerTag);
        }
        throw new DockerManagerException("Unable to find Docker container with the tag: " + dockerContainerTag);
    }

    /**
     * Returns the collection of docker container tags
     * 
     * @return Collections<DockerContainerImpl>
     */
    @Override
    public Collection<DockerContainerImpl> getContainers() {
        return containersByTag.values();
    }

    /**
     * Creates the docker engine.
     * 
     * @return DockerEngineImpl
     * @throws DockerProvisionException
     */
    private DockerEngineImpl buildDockerEngine(String dockerEngineTag) throws DockerProvisionException {
        if (enginesByTag.containsKey(dockerEngineTag)) {
            logger.info("DockerEngine already built, returning that.");
            return enginesByTag.get(dockerEngineTag);
        }
        DockerEngineImpl dockerEngine = new DockerEngineImpl(framework, dockerManager, dockerEngineTag, dss);

        enginesByTag.put(dockerEngineTag, dockerEngine);

        return dockerEngine;
    }

    /**
     * Provisions the docker containers from the annotations in the test classs
     * 
     * @param annotation
     * @return DockerContainerImpl
     * @throws DockerProvisionException
     */
    private DockerContainerImpl provisionDockerContainer(DockerContainer annotation) throws DockerProvisionException {
        return provisionDockerContainer("GALASA_" + annotation.dockerContainerTag().trim().toUpperCase(),
                annotation.image(), annotation.start(), annotation.dockerEngineTag());
    }

    /**
     * Provisions a docker slot for a docker container.
     * @return
     * @throws DockerProvisionException
     * @throws DockerManagerException
     */
    private DockerSlotImpl provisionDockerSlot(DockerEngineImpl engine)
            throws DockerProvisionException, DockerManagerException {
        String runName = framework.getTestRunName();
        String dockerEngineId = engine.getEngineId();

        this.dynamicResource = this.dss.getDynamicResource("engine." + dockerEngineId);

        return allocateAndCreateDssSlot(dockerEngineId, runName, engine);
    }

    /**
     * Used during the provisioning of a docker slot to allocate a slot within the
     * DSS to lock the resource.
     * 
     * @param dockerHost
     * @return boolean (failed/passed)
     * @throws DockerProvisionException
     */
    private DockerSlotImpl allocateAndCreateDssSlot(String dockerEngineId, String runName, DockerEngineImpl engine)
            throws DockerProvisionException {
        String slotKey = "engine." + dockerEngineId + ".current.slots";
        String slotNamePrefix = "SLOT_" + runName + "_";
        String allocatedSlotName;
        String slotPropertyKey;
        String allocatedTime = Instant.now().toString();

        HashMap<String, String> slotProps = new HashMap<>();

        try {
            int maxSlots = Integer.parseInt(DockerSlots.get(engine));
            int usedSlots = 0;
            String currentSlots = dss.get(slotKey);

            if (currentSlots != null) {
                usedSlots = Integer.parseInt(currentSlots);
            }
            if (usedSlots >= maxSlots) {
                throw new DockerProvisionException("Not enough available slots");
            }
            usedSlots++;
            String slotIncrease = Integer.toString(usedSlots);

            for (int i = 0;; i++) {
                slotProps.clear();
                allocatedSlotName = slotNamePrefix + i;
                slotPropertyKey = "engine." + dockerEngineId + ".slot." + allocatedSlotName;
                slotProps.put("slot." + dockerEngineId + ".run." + runName + "." + allocatedSlotName, "active");
                if (dss.putSwap(slotPropertyKey, null, runName)) {
                    break;
                }
            }

            if (dss.putSwap(slotKey, currentSlots, slotIncrease, slotProps)) {
                String resourcePropertyPrefix = "slot." + allocatedSlotName;

                HashMap<String, String> resProps = new HashMap<>();
                resProps.put(resourcePropertyPrefix, runName);
                resProps.put(resourcePropertyPrefix + ".allocated", allocatedTime);

                dynamicResource.put(resProps);

                return new DockerSlotImpl(dockerManager, engine, allocatedSlotName, resProps);
            } else {
                // Retry as another slot was allocated during the allocation of this slot
                dss.delete(slotPropertyKey);
                return allocateAndCreateDssSlot(dockerEngineId, runName, engine);
            }
        } catch (DockerManagerException e) {
            logger.error("Could not find number of Docker slots in CPS");
        } catch (DynamicStatusStoreException e) {
            logger.warn("Could not perform putswap on dss");
        }
        throw new DockerProvisionException("Failed to provision Docker slot");
    }

    /**
     * Free a specifed docker slot in terms of the container and environment
     * 
     * @param dockerSlot
     * @throws DockerProvisionException
     */
    @Override
    public void freeDockerSlot(DockerSlotImpl dockerSlot) throws DockerProvisionException {
        DockerEngineImpl dockerEngine = dockerSlot.getDockerEngine();
        String dockerEngineId = dockerEngine.getEngineId();

        try {
            String currentSlot = dss.get("engine." + dockerEngineId + ".current.slots");
            if (currentSlot == null) {
                return;
            }

            int usedSlots = Integer.parseInt(currentSlot);
            usedSlots--;
            if (usedSlots < 0) {
                usedSlots = 0;
            }
            dynamicResource.delete(dockerSlot.getResourcePropertyKeys());

            String prefix = "engine." + dockerEngineId + ".slot." + dockerSlot.getSlotName();
            String slotKey = "slot." + dockerEngineId + ".run." + framework.getTestRunName() + "."
                    + dockerSlot.getSlotName();
            HashMap<String, String> otherProps = new HashMap<>();
            otherProps.put(slotKey, "free");
            if (!dss.putSwap("engine." + dockerEngineId + ".current.slots", currentSlot, Integer.toString(usedSlots),
                    otherProps)) {
                Thread.sleep(200);
                freeDockerSlot(dockerSlot);
                return;
            }

            HashSet<String> delProps = new HashSet<>();
            delProps.add(prefix);
            delProps.add(prefix + ".allocated");
            delProps.add(slotKey);
            dss.delete(delProps);
            logger.info("Discarding slot: " + dockerSlot.getSlotName() + ". on the Docker engine: " + dockerEngineId);
        } catch (Exception e) {
            logger.warn("Failed to free slot on engine " + dockerEngineId + ", slot " + dockerSlot.getSlotName()
                    + ", leaving for manager clean up routines", e);
        }
    }

    /**
     * Used by resource management to clean up stale properties
     * 
     * @param runName
     * @param dockerEngineId
     * @param slotName
     * @param dss
     */
    public static void deleteStaleDssSlot(String runName, String dockerEngineId, String slotName,
            IDynamicStatusStoreService dss) {
        try {
            String numberOfSlotKey = "engine." + dockerEngineId + ".current.slots";
            String currentSlot = dss.get(numberOfSlotKey);
            if (currentSlot == null) {
                return;
            }

            int usedSlots = Integer.parseInt(currentSlot);
            usedSlots--;
            if (usedSlots < 0) {
                usedSlots = 0;
            }

            IDynamicResource dynamicResource = dss.getDynamicResource("engine." + dockerEngineId);
            String resPrefix = "slot." + slotName;

            HashSet<String> resProps = new HashSet<>();
            resProps.add(resPrefix);
            resProps.add(resPrefix + ".allocated");
            dynamicResource.delete(resProps);

            String slotStatusKey = "slot." + dockerEngineId + ".run." + runName + "." + slotName;
            String runIdKey = "engine." + dockerEngineId + ".slot." + slotName;

            HashMap<String, String> dockerDssProps = new HashMap<>();
            dockerDssProps.put(slotStatusKey, "free");

            if ("active".equals(dss.get(slotStatusKey))) {
                if (!dss.putSwap(numberOfSlotKey, currentSlot, Integer.toString(usedSlots), dockerDssProps)) {
                    Thread.sleep(200);
                    deleteStaleDssSlot(runName, dockerEngineId, slotName, dss);
                    return;
                }
                HashSet<String> props = new HashSet<>();
                props.add(slotStatusKey);
                props.add(runIdKey);
                dss.delete(props);
            }

        } catch (Exception e) {
            logger.error("Failed to discard slot " + slotName + " on Docker engine " + dockerEngineId, e);
        }
    }

    @Override
    public DockerVolumeImpl allocateDockerVolume(String volumeName, String tag, String mountPath, String dockerEngineTag, boolean readOnly) throws DockerProvisionException {

        DockerEngineImpl engine = enginesByTag.get(dockerEngineTag);
        if (engine == null) {
            engine = buildDockerEngine(dockerEngineTag);
            enginesByTag.put(dockerEngineTag, engine);
        }
        
        // String enginePropertyPrefix = "engine." + dockerEngineTag + ".volume."; 
        String volumePropertyPrefix = "volume.";
        String preProvisionVolumeName = "GALASA_VOLUME_" + framework.getTestRunName() + "_";
        int volumeNumber = 1;
        String fullVolumeName;
        boolean provision = true;

        try {
            if (!"".equals(volumeName)) {
                fullVolumeName = volumeName;
                provision = false;
            } else {
                while (framework.getTestRunName().equals(dss.get(volumePropertyPrefix + preProvisionVolumeName + volumeNumber+ ".run"))){
                        volumeNumber++;
                }
                fullVolumeName = preProvisionVolumeName + volumeNumber;
                dss.performActions(new DssAdd(volumePropertyPrefix + fullVolumeName + ".engine", dockerEngineTag),
                                    new DssAdd(volumePropertyPrefix + fullVolumeName + ".run", framework.getTestRunName()));
            }
            
        } catch (DynamicStatusStoreException e) {
            throw new DockerProvisionException("Failed to form a volume name", e);
        }


        try {
            DockerVolumeImpl volume = new DockerVolumeImpl(dockerManager, fullVolumeName, tag, mountPath, engine, readOnly, provision);
            if (provision) {
                volumes.add(volume);
            }

            
            return volume;
        } catch (DockerManagerException e) {
            throw new DockerProvisionException("Failed to allocate Docker volume.", e);
        }
    }

    @Override
    public void removeDockerVolume(DockerVolumeImpl volume) throws DockerManagerException {
        String volumeRun = "volume." + volume.getVolumeName() + ".run";
        String volumeEngine = "volume." + volume.getVolumeName() + ".engine";

        try{
            dss.performActions(
                new DssDelete(volumeRun, framework.getTestRunName()),
                new DssDelete(volumeEngine, volume.getEngineTag())
            );
        } catch (DynamicStatusStoreException e) {
            throw new DockerManagerException("Failed to clean dss Volume properties for: " + volume.getVolumeName() ,e);
        }

        volume.discard();
    }
}