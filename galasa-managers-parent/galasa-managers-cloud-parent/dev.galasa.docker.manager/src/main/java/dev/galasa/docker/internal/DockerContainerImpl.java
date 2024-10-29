/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.DockerProvisionException;
import dev.galasa.docker.IDockerContainer;
import dev.galasa.docker.IDockerContainerConfig;
import dev.galasa.docker.IDockerExec;
import dev.galasa.docker.IDockerImage;
import dev.galasa.docker.IDockerVolume;
import dev.galasa.docker.internal.properties.DockerLeaveRunning;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;

/**
 * Docker Container implementation used to contain all container configurations.
 * 
 *   
 */
public class DockerContainerImpl implements IDockerContainer {

    private static final String CONTAINER_NAME_PREFIX = "GALASA_";

    private IFramework framework;
    private DockerManagerImpl dockerManager;
    private String tag;
    private DockerEngineImpl dockerEngine;
    private DockerImageImpl image;
    private Boolean autoStartup;
    private DockerSlotImpl dockerSlot;
    private IDynamicStatusStoreService dss;
    private String containerID;
    private String containerName;
    private JsonObject metadata;
    private IDockerContainerConfig userConfig;

    private boolean leaveRunning;
    private boolean alreadyUp;
    private boolean alreadyDefined;

    private Map<String, List<InetSocketAddress>> exposedPorts = new HashMap<>();
    private SecureRandom random = new SecureRandom();

    private static final Log logger = LogFactory.getLog(DockerContainerImpl.class);

    /**
     * Registers the docker container in the DSS within the docker namespace
     * 
     * @param framework
     * @param dockerManager
     * @param tag
     * @param dockerEngine
     * @param image
     * @param start
     * @param slot
     * @throws DockerProvisionException
     */
    public DockerContainerImpl(IFramework framework, DockerManagerImpl dockerManager, String tag,
            DockerEngineImpl dockerEngine, DockerImageImpl image, Boolean start, DockerSlotImpl slot)
            throws DockerProvisionException {
        this.framework = framework;
        this.dockerManager = dockerManager;
        this.tag = tag;
        this.dockerEngine = dockerEngine;
        this.image = image;
        this.autoStartup = start;
        this.dockerSlot = slot;

        try {
            this.dss = framework.getDynamicStatusStoreService(dockerManager.NAMESPACE);
            this.containerName = getContainerName(this.dockerSlot);
        } catch (DynamicStatusStoreException e) {
            throw new DockerProvisionException(
                    "Failed to instantiate Docker container. Could not determine the container name from the DSS: ", e);
        }

        if (autoStartup) {
            try {
                this.startDockerContainer();
            } catch (DockerManagerException e) {
                throw new DockerProvisionException("Failed to auto start container");
            }
        }

    }

    /**
     * Generates the container creation metadata with any included user definedconfig
     * 
     * @param config
     * @return JsonObject
     */
    private JsonObject generateMetadata(IDockerContainerConfig config) {
        JsonObject metadata = new JsonObject();
        metadata.addProperty("Image", this.image.getFullName());

        JsonObject hostConfig = new JsonObject();
        hostConfig.addProperty("PublishAllPorts", Boolean.TRUE);
        metadata.add("HostConfig", hostConfig);
        
        JsonObject labels = new JsonObject();
        labels.addProperty("GALASA", "GALASA");
        labels.addProperty("EngineId", dockerEngine.getEngineId());
        labels.addProperty("RunId", framework.getTestRunName());
        labels.addProperty("SlotId", dockerSlot.getSlotName());
        metadata.add("Labels", labels);

        if (config == null) {
            return metadata;
        }
        // Currently only supporting ENVS and Volumes and ports

        // Envs
        JsonArray env = new JsonArray();
        HashMap<String,String> envs = config.getEnvs();
        if (envs.keySet().size() > 0) {
            for (String envName: envs.keySet()) {
                env.add(envName+"="+envs.get(envName));
            }
            metadata.add("Env", env);
        }

        // Ports
        JsonObject exposedPorts = new JsonObject();
        List<String> ports = config.getExposedPorts();
        if (ports.size() > 0) {
            for (String port: ports) {
                exposedPorts.add(port, new JsonObject());
            }
            metadata.add("ExposedPorts", exposedPorts);
        }

        // Volumes
        JsonArray mounts = new JsonArray();
        List<IDockerVolume> volumes = config.getVolumes();
        for (IDockerVolume volume : volumes)  {
            JsonObject mount = new JsonObject();

            mount.addProperty("Target", volume.getMountPath());
            mount.addProperty("Source", volume.getVolumeName());
            mount.addProperty("Type", "volume");
            mount.addProperty("ReadOnly", volume.readOnly());

            mounts.add(mount);
        }
        if (mounts.size() > 0 ) {
            hostConfig.add("Mounts", mounts);
            metadata.remove("HostConfig");
            metadata.add("HostConfig", hostConfig);
        }

        logger.info("Container metadata:" + metadata.toString());

        return metadata;
    }

    /**
     * Check container observers the state of the container, and returns it to any
     * expected state.
     * 
     * @throws DockerProvisionException
     */
    public void checkContainer() throws DockerProvisionException {
        try {
            logger.debug("Checking if container should be left running");
            checkLeaveRunning();
            logger.debug("Checking the current state of the container");
            checkContainerState();
            this.image.locateImage();

            if (!alreadyDefined) {
                this.image.pullImage();
                try {
                    this.metadata = generateMetadata(this.userConfig);
                    logger.debug("Creating Docker container '" + tag + "'");
                    JsonObject newContainer = dockerEngine.createContainer(containerName, this.metadata);
                    alreadyDefined = true;
                    logger.debug("Created Docker container '" + tag + "'");
                    containerID = newContainer.get("Id").getAsString();
                    if (containerID == null || containerID.trim().isEmpty()) {
                        throw new DockerManagerException("Container ID is missing");
                    }
                } catch (DockerManagerException e) {
                    throw e;
                } catch (Exception e) {
                    throw new DockerManagerException("Unable to create the Docker container '" + this.tag + "'", e);
                }
            }

            logger.info("Container '" + tag + "' created under name '" + containerName + "'");
        } catch (DockerManagerException e) {
            throw new DockerProvisionException("Unable to prepare the Docker container '" + this.tag + "'", e);
        }
    }

    /**
     * If using the @DockerContainerConfig annotation a user can pass customer
     * configurations to container startup.
     * 
     * @param config
     * @throws DockerManagerException
     */
    @Override
    public void startWithConfig(IDockerContainerConfig config) throws DockerManagerException {
        // If defined we need to cleanup the container ready for new configuration
        if (alreadyDefined) {
            logger.info("Container is already defined. Cleaning old container");
            stopDockerContainer();
            deleteContainer();
            alreadyDefined = false;
        }
        this.userConfig = config;
        start();
    }

    /**
     * Used to issue the "docker start" command
     */
    @Override
    public void start() throws DockerManagerException {
        startDockerContainer();
    }

    /**
     * Passed the docker start commands to the docker engine if the container is not
     * already running.
     * 
     * @throws DockerManagerException
     */
    private void startDockerContainer() throws DockerManagerException {
        if (alreadyUp) {
            logger.info("Container already running");
            return;
        }
        try {
            logger.info("Checking container before attempting start.");
            checkContainer();
            logger.debug("Starting Docker container: " + tag);
            dockerEngine.startContainer(containerID);
            logger.info("Started Docker container: " + tag);
            Thread.sleep(2000);
            alreadyUp = true;
        } catch (Exception e) {
            throw new DockerManagerException("Failed to start Docker container: " + tag, e);
        }

        extractContainerExposedPortsFromDockerEngine();

    }

    /**
     * Issues the "docker stop" command
     */
    @Override
    public void stop() throws DockerManagerException {
        stopDockerContainer();

    }

    /**
     * Passes the docker stop command to the docker engine if the container is nor
     * already stopped
     * 
     * @throws DockerManagerException
     */
    private void stopDockerContainer() throws DockerManagerException {
        if (!isRunning()) {
            logger.info("Stop command ignored, container already stopped.");
            alreadyUp = false;
            return;
        }
        try {
            logger.info("Stopping Docker container: " + this.tag);
            killContainer();
            logger.info("Container " + this.tag + " has stopped");
            alreadyUp = false;
        } catch (DockerManagerException e) {
            throw new DockerManagerException("Unable to stop Docker container: " + this.tag, e);
        }
    }

    /**
     * Returns the image name of this container.
     */
    @Override
    public IDockerImage getDockerImage() {
        return this.image;
    }
    
    /**
     * Returns the container tag.
     */
    public String getContainerTag() {
    	return this.tag;
    }

    /**
     * Once a container is started, this method is used to query the container
     * information to determine opened ports
     * 
     * @throws DockerManagerException
     */
    private void extractContainerExposedPortsFromDockerEngine() throws DockerManagerException {
        try {
            JsonObject containerInfo = dockerEngine.getContainer(containerID);

            JsonObject ports = retrievePorts(containerInfo);
            if (ports != null) {
                for (Object port : ports.entrySet()) {
                    if (port instanceof Entry) {
                        Entry<?, ?> portEntry = (Entry<?, ?>) port;
                        exposedPorts.put((String) portEntry.getKey(), getSocketsFromPort(portEntry));
                    }
                }
            }
        } catch (Exception e) {
            throw new DockerManagerException("Unable to determine exposed ports in the Docker container: " + this.tag,
                    e);
        }
    }

    /**
     * Used by the extractContainerExposedPortsFromDockerEngine() to retrieve ports
     * from the container information.
     * 
     * @param containerInfo
     * @return Ports
     */
    private JsonObject retrievePorts(JsonObject containerInfo) {
        JsonObject networkSettings = containerInfo.get("NetworkSettings").getAsJsonObject();

        if (networkSettings != null) {
            return networkSettings.get("Ports").getAsJsonObject();
        } else
            return null;
    }

    /**
     * Returns a list of sockets from the JsonObect containing ports, used by the
     * extractContainerExposedPortsFromDockerEngine()
     * 
     * @param portEntry
     * @return Sockets
     */
    private ArrayList<InetSocketAddress> getSocketsFromPort(Entry<?, ?> portEntry) {
        ArrayList<InetSocketAddress> sockets = new ArrayList<>();

        if (portEntry.getValue() instanceof JsonArray) {
            JsonArray hostPorts = (JsonArray) portEntry.getValue();
            for (Object hostPort : hostPorts) {
                if (hostPort instanceof JsonObject) {
                    String sHostIp;
                    int iHostPort;
                    JsonElement hostIP = ((JsonObject) hostPort).get("HostIp");
                    JsonElement hPort = ((JsonObject) hostPort).get("HostPort");

                    if (hostIP == null || hostIP.getAsString().equals("0.0.0.0")) {
                        sHostIp = dockerEngine.getHost();
                        iHostPort = hPort.getAsInt();
                    } else {
                        sHostIp = hostIP.getAsString();
                        iHostPort = hPort.getAsInt();
                    }
                    InetSocketAddress socket = new InetSocketAddress(sHostIp, iHostPort);
                    sockets.add(socket);
                }
            }
        }
        return sockets;
    }

    /**
     * Returns the Map of the exposed ports
     * 
     * @throws DockerManagerException
     */
    @Override
    public Map<String, List<InetSocketAddress>> getExposedPorts() throws DockerManagerException {
        return exposedPorts;
    }

    /**
     * Retrieves the first open socket `
     * 
     * @param exposedPort
     * @return InetSocketAddress
     */
    @Override
    public InetSocketAddress getFirstSocketForExposedPort(String exposedPort) {
        List<InetSocketAddress> sockets = exposedPorts.get(exposedPort);
        if (sockets == null || sockets.isEmpty()) {
            return null;
        }
        return sockets.get(0);
    }

    /**
     * Retrieves a random open socket
     * 
     * @param exposedPort
     * @return InetSocketAddress
     */
    @Override
    public InetSocketAddress getRandomSocketForExposedPort(String exposedPort) {
        List<InetSocketAddress> sockets = exposedPorts.get(exposedPort);
        if (sockets == null || sockets.isEmpty()) {
            return null;
        }
        return sockets.get(random.nextInt(sockets.size()));
    }

    /**
     * Submit exec commands to be executed on the docker container.
     * 
     * @param command
     * @return IDockerExec
     */
    @Override
    public IDockerExec exec(String... command) throws DockerManagerException {
        return new DockerExecImpl(framework, dockerManager, this, 10000, command);
    }

    /**
     * Submit exec commands to be executed on the docker container, with a custom
     * timeout.
     * 
     * @param timeout
     * @param command
     * @return IDockerExec
     */
    @Override
    public IDockerExec exec(int timeout, String... command) throws DockerManagerException {
        return new DockerExecImpl(framework, dockerManager, this, timeout, command);
    }

    /**
     * Retrieves any stdOut from the container
     * 
     * @throws DockerManaerException
     */
    @Override
    public String retrieveStdOut() throws DockerManagerException {
        return dockerEngine.getLog("/containers/" + containerID + "/logs?stdout=true&timestamps=true");
    }

    /**
     * Retrieves any stdErr from the container
     * 
     * @throws DockerManaerException
     */
    @Override
    public String retrieveStdErr() throws DockerManagerException {
        return dockerEngine.getLog("/containers/" + containerID + "/logs?stderr=true&timestamps=true");
    }

    /**
     * Polls the docker engine for information about a container to see if running.
     * 
     * @return boolean isRunning?
     * @throws DockerManagerException
     */
    @Override
    public boolean isRunning() throws DockerManagerException {
        JsonObject response = dockerEngine.getContainer(containerName);
        if (response == null) {
            return false;
        }
        JsonObject state = response.get("State").getAsJsonObject();
        Boolean running = state.get("Running").getAsBoolean();

        return running;
    }

    /**
     * Retrieves the exitCode from container
     * 
     * @return exitCode
     * @throws DockerManagerException
     */
    @Override
    public long getExitCode() throws DockerManagerException {
        JsonObject response = dockerEngine.getContainer(containerName);
        JsonObject state = response.get("State").getAsJsonObject();
        if (state == null) {
            return -1;
        }
        Long exitCode = state.get("ExitCode").getAsLong();

        return exitCode;
    }

    /**
     * Retrieves the docker slot that this container is using.
     * 
     * @return DockerSlotImpl
     */
    public DockerSlotImpl getDockerSlot() {
        return this.dockerSlot;
    }

    /**
     * Queries the DSS for the run name to construct and standarised name for the
     * container
     * 
     * E.g 'GALASA_U12_database'
     * 
     * @param dockerSlot
     * @return String - name of container
     * @throws DynamicStatusStoreException
     */
    private String getContainerName(DockerSlotImpl dockerSlot) throws DynamicStatusStoreException {
        String slotName = dockerSlot.getSlotName();
        String runName = dss.get("engine." + dockerEngine.getEngineId() + ".slot." + slotName);
        // E.g 'GALASA_U12_ExampleContainerName'
        return CONTAINER_NAME_PREFIX + runName + "_" + this.tag;
    }

    /**
     * Check to see if a flag was set to leave the container running post test.
     * 
     * @throws DockerManagerException 
     */
    private void checkLeaveRunning() throws DockerManagerException {
        String flag = DockerLeaveRunning.get(this);
        if (flag != null) {
            logger.debug("Requested leaveRunning state: " + flag);
            leaveRunning = Boolean.parseBoolean(flag);
        } else {
            logger.debug("No state requested, setting leaveRunning to false");
            leaveRunning = false;
        }
    }

    /**
     * Used by the checkContainer() to collect the state from the docker engine.
     * 
     * If the container is not as expected then it is attempted to be resolved.
     * 
     * @throws DockerManagerException
     */
    private void checkContainerState() throws DockerManagerException {
        JsonObject response = dockerEngine.getContainer(containerName);

        if (response != null) {
            logger.debug("Docker container '" + this.tag + "' is already defined");
            alreadyDefined = true;
            containerID = response.get("Id").getAsString();

            JsonObject state = (JsonObject) response.get("State");
            alreadyUp = state.get("Running").getAsBoolean();
            if (alreadyUp) {
                logger.debug("Docker container '" + this.tag + "' is already running");
            }

            if (!leaveRunning && (alreadyUp || alreadyDefined)) {
                logger.debug("Tidying up the Docker container as leave.running is not true");
                if (alreadyUp) {
                    killContainer();
                }
                if (alreadyDefined) {
                    deleteContainer();
                }
                alreadyUp = false;
                alreadyDefined = false;
            }

            if (alreadyDefined) {
                JsonObject config = response.get("Config").getAsJsonObject();
                String imageName = config.get("Image").getAsString();
                this.image.setFullName(imageName);
            }
        } else {
            logger.debug("No response, not defined or running");
            alreadyDefined = false;
            alreadyUp = false;
        }
    }

    /**
     * Deletes the container from the docker engine
     * 
     * @throws DockerManagerException
     */
    private void deleteContainer() throws DockerManagerException {
        logger.debug("Deleting Docker container '" + tag + "'");
        dockerEngine.deleteContainer(containerID);
        logger.info("Deleted Docker container '" + tag + "'");
    }

    /**
     * Kills the running container on the docker engine
     * 
     * @throws DockerManagerException
     */
    private void killContainer() throws DockerManagerException {
        logger.debug("Killing Docker container '" + tag + "'");
        dockerEngine.killContainer(containerID);
        logger.info("Killed Docker container '" + tag + "'");
    }

    /**
     * Retrieves the dockerEngine the container is hosted on.
     * 
     * @return dockerEngine
     */
    public DockerEngineImpl getDockerEngineImpl() {
        return dockerEngine;
    }

    /**
     * @return the docker running ID
     */
    public String getContainerId() {
        return containerID;
    }

    /**
     * Checks to see if this container should be left running.
     * 
     * If not, then cleans up both the container and the slot.
     * 
     * @throws DockerManagerException
     */
    public void discard() throws DockerManagerException {
        if (leaveRunning) {
            return;
        }

        try {
            deleteContainer();
        } catch (DockerManagerException e) {
            throw new DockerManagerException("Unable to stop container: " + tag, e);
        }

        try {
            dockerSlot.free();
        } catch (Exception e) {
            logger.warn("Unable to free slot");
            throw new DockerManagerException("Unable to free Docker slot: " + tag, e);
        }
    }

    /**
     * Allows a file to be stored on a running docker container
     * 
     * @param absolutePath
     * @param file
     */
    @Override
    public void storeFile(String absolutePath, InputStream file) throws DockerManagerException {
        if(!absolutePath.startsWith("/")) {
            throw new DockerManagerException("Please specify the absolute path of the location on the container, including file name");
        }
        Path locPath = Paths.get(absolutePath);
        File tar = compressToTar(file, locPath.getFileName().toString());

        try{
        
        InputStream is = new FileInputStream(tar);
        dockerEngine.sendArchiveFile(this, is,locPath.getParent().toString()+"/");
        tar.delete();
        } catch (FileNotFoundException e) {
            logger.error("Failed to find compressed file", e);
            tar.delete();
            throw new DockerManagerException("Failed to create tar file.");
        }
    }

     /**
     * Retrieves a file from a running docker container.
     * 
     * @param path
     */
    @Override
    public InputStream retrieveFile(String path) throws DockerManagerException {
        return dockerEngine.getArchiveFile(this, path);
    }

    /**
     * Returns the contents of a file on the container as a string.
     * 
     * @param path
     * @return String
     */
    @Override
    public String retrieveFileAsString(String path) throws DockerManagerException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
		String output = "";
        InputStream in = dockerEngine.getArchiveFile(this, path);

        try {
            IOUtils.copy(in, bos);
            output = bos.toString();
            bos.close();
        } catch (IOException e) {
            throw new DockerManagerException();
        }
        
        return output;
    }

    /**
     * Takes a input stream from a file and compresses to a tar.gz, used for the docker engine API 
     *  to send archive files.
     * @param file
     * @param fileName
     * @return
     * @throws DockerManagerException
     */
    private File compressToTar(InputStream file, String fileName) throws DockerManagerException {
        File targetFile = new File("output.tar.gz");
        BufferedInputStream bIn = new BufferedInputStream(file);
        FileOutputStream out = null;
        TarArchiveOutputStream taos = null;
        try {
            out = new FileOutputStream(targetFile);

            taos = new TarArchiveOutputStream(new GZIPOutputStream(new BufferedOutputStream(out)));
            TarArchiveEntry aEntry = new TarArchiveEntry(fileName);
            aEntry.setSize(file.available());
            taos.putArchiveEntry(aEntry);
            
            int count;
            byte data[] = new byte[2048];
            while((count = bIn.read(data)) != -1) {
                taos.write(data, 0, count);
            }
            taos.closeArchiveEntry();
            
            taos.flush();
            taos.close();
            bIn.close();
            out.close();
        } catch (IOException e) {
            logger.error("IO error, failed to create tar", e);
            throw new DockerManagerException(e);
        } finally {
            try {
                out.close();
                taos.close();
            } catch (IOException e) {
                throw new DockerManagerException(e);
            }
            
        }
        return targetFile;
    }
}