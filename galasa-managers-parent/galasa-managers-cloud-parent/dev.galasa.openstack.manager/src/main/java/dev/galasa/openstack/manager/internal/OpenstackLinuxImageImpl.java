/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ICredentials;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.linux.LinuxManagerException;
import dev.galasa.linux.spi.ILinuxProvisionedImage;
import dev.galasa.openstack.manager.OpenstackLinuxManagerException;
import dev.galasa.openstack.manager.OpenstackManagerException;
import dev.galasa.openstack.manager.internal.json.GalasaMetadata;
import dev.galasa.openstack.manager.internal.json.SecurityGroup;
import dev.galasa.openstack.manager.internal.json.Server;
import dev.galasa.openstack.manager.internal.json.ServerRequest;
import dev.galasa.openstack.manager.internal.properties.LinuxArchivesDirectory;
import dev.galasa.openstack.manager.internal.properties.LinuxAvailablityZone;
import dev.galasa.openstack.manager.internal.properties.LinuxCredentials;
import dev.galasa.openstack.manager.internal.properties.LinuxFlavor;
import dev.galasa.openstack.manager.internal.properties.LinuxKeyPair;
import dev.galasa.openstack.manager.internal.properties.LinuxName;
import dev.galasa.openstack.manager.internal.properties.LinuxSecurityGroups;

public class OpenstackLinuxImageImpl extends OpenstackServerImpl implements ILinuxProvisionedImage {

    private static final Log          logger = LogFactory.getLog(OpenstackLinuxImageImpl.class);

    private FileSystem                fileSystem;

    private Path                      pathRoot;
    private Path                      pathTemp;
    private Path                      pathHome;
    private Path                      pathRunDirectory;

    public OpenstackLinuxImageImpl(@NotNull OpenstackManagerImpl manager,
            @NotNull OpenstackHttpClient openstackHttpClient, @NotNull String instanceName, @NotNull String image,
            @NotNull String tag) {
        super("Linux", manager, openstackHttpClient, instanceName, image, tag);
    }

    @Override
    public @NotNull String getImageID() {
        return this.instanceName;
    }

    @Override
    public @NotNull ICredentials getDefaultCredentials() throws LinuxManagerException {
        if (this.getIpHost() == null) {
            throw new OpenstackLinuxManagerException("Openstack instance has not been built yet");
        }
        try {
            return this.getIpHost().getDefaultCredentials();
        } catch (IpNetworkManagerException e) {
            throw new OpenstackLinuxManagerException("Unable to retrieve credentials", e);
        }
    }

    protected @NotNull ICredentials getServerCredentials() throws OpenstackManagerException {
        try {
            return this.manager.getFramework().getCredentialsService().getCredentials(LinuxCredentials.get(this.image));
        } catch (Exception e) {
            throw new OpenstackManagerException("Unable to create credentials", e);
        }
    }

    public void build() throws OpenstackManagerException, ConfigurationPropertyStoreException {
        logger.info("Building OpenStack Linux instance " + this.instanceName + " with image " + this.image + " for tag "
                + this.tag);

        String flavor = LinuxFlavor.get(this.image);
        logger.trace("The Linux flavor is " + flavor);

        String imageName = LinuxName.get(this.image);
        logger.trace("The image name is " + imageName);

        Server server = new Server();
        server.name = this.instanceName;
        server.imageRef = getOpenstackHttpClient().getImageId(imageName);
        server.flavorRef = getOpenstackHttpClient().getFlavourId(flavor);
        server.availability_zone = LinuxAvailablityZone.get(this.image);
        server.metadata = new GalasaMetadata();
        server.metadata.galasa_run = this.manager.getFramework().getTestRunName();
        server.key_name = LinuxKeyPair.get(this.image);

        List<String> groups = LinuxSecurityGroups.get(this.image);
        List<SecurityGroup> securityGroups = new ArrayList<>();
        for (String group : groups) {
            SecurityGroup sGroup = new SecurityGroup();
            sGroup.name = group;
            securityGroups.add(sGroup);
        }
        server.security_groups = securityGroups;

        if (server.imageRef == null) {
            throw new OpenstackManagerException("Image " + this.image + " is missing in OpenStack");
        }

        if (server.flavorRef == null) {
            throw new OpenstackManagerException("Flavor " + flavor + " is missing in OpenStack");
        }

        ServerRequest serverRequest = new ServerRequest();
        serverRequest.server = server;

        createServer(serverRequest);

        // *** Create the filesystem
        try {
            this.fileSystem = this.manager.getIpNetworkManager().getFileSystem(this.getIpHost());
        } catch (IpNetworkManagerException e) {
            throw new OpenstackManagerException("Unable to create server FileSystem",e);
        }

        this.pathRoot = this.fileSystem.getPath("/");
        this.pathTemp = this.fileSystem.getPath("/tmp");

        try {
            String homeDir = getServerCommandShell().issueCommand("pwd");
            if (homeDir == null) {
                throw new OpenstackManagerException("Unable to determine home directory, response null");
            }
            homeDir = homeDir.replaceAll("\\r\\n?|\\n", "");
            this.pathHome = this.fileSystem.getPath(homeDir);
            logger.info("Home directory for linux image tagged " + tag + " is " + homeDir);
        } catch (IpNetworkManagerException e) {
            throw new OpenstackManagerException("Unable to determine home directory", e);
        }

    }

    @Override
    public @NotNull ICommandShell getCommandShell() throws LinuxManagerException {
        return getServerCommandShell();
    }

    @Override
    public @NotNull Path getRoot() throws LinuxManagerException {
        return this.pathRoot;
    }

    @Override
    public @NotNull Path getHome() throws LinuxManagerException {
        return this.pathHome;
    }

    @Override
    public @NotNull Path getTmp() throws LinuxManagerException {
        return this.pathTemp;
    }
    
    @Override
    public @NotNull Path getRunDirectory() throws LinuxManagerException {
        if (this.pathRunDirectory != null) {
            return this.pathRunDirectory;
        }

        this.pathRunDirectory = this.pathHome.resolve(this.manager.getFramework().getTestRunName());

        try {
            Files.createDirectories(pathRunDirectory);
        } catch(Exception e) {
            throw new LinuxManagerException("Unable to create the run directory on server", e);
        }

        return this.pathRunDirectory;
    }
    
    @Override
    public @NotNull Path getArchivesDirectory() throws LinuxManagerException {
        try {
            return this.fileSystem.getPath(LinuxArchivesDirectory.get(getImage()));
        } catch (Exception e) {
            throw new LinuxManagerException("Problem determining archives directory", e);
        }
    }

}
