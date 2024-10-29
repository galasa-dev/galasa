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
import dev.galasa.openstack.manager.OpenstackManagerException;
import dev.galasa.openstack.manager.OpenstackWindowsManagerException;
import dev.galasa.openstack.manager.internal.json.GalasaMetadata;
import dev.galasa.openstack.manager.internal.json.SecurityGroup;
import dev.galasa.openstack.manager.internal.json.Server;
import dev.galasa.openstack.manager.internal.json.ServerRequest;
import dev.galasa.openstack.manager.internal.properties.WindowsAvailablityZone;
import dev.galasa.openstack.manager.internal.properties.WindowsCredentials;
import dev.galasa.openstack.manager.internal.properties.WindowsFlavor;
import dev.galasa.openstack.manager.internal.properties.WindowsKeyPair;
import dev.galasa.openstack.manager.internal.properties.WindowsName;
import dev.galasa.openstack.manager.internal.properties.WindowsSecurityGroups;
import dev.galasa.windows.WindowsManagerException;
import dev.galasa.windows.spi.IWindowsProvisionedImage;

public class OpenstackWindowsImageImpl extends OpenstackServerImpl implements IWindowsProvisionedImage {

    private static final Log          logger = LogFactory.getLog(OpenstackWindowsImageImpl.class);

    private FileSystem                fileSystem;

    private Path                      pathRoot;
    private Path                      pathTemp;
    private Path                      pathHome;
    private Path                      pathRunDirectory;

    public OpenstackWindowsImageImpl(@NotNull OpenstackManagerImpl manager,
            @NotNull OpenstackHttpClient openstackHttpClient, @NotNull String instanceName, @NotNull String image,
            @NotNull String tag) {
        super("Windows", manager, openstackHttpClient, instanceName, image, tag);
    }

    @Override
    public @NotNull String getImageID() {
        return this.instanceName;
    }

    @Override
    public @NotNull ICredentials getDefaultCredentials() throws WindowsManagerException {
        if (this.getIpHost() == null) {
            throw new OpenstackWindowsManagerException("Openstack instance has not been built yet");
        }
        try {
            return this.getIpHost().getDefaultCredentials();
        } catch (IpNetworkManagerException e) {
            throw new OpenstackWindowsManagerException("Unable to retrieve credentials", e);
        }
    }

    protected @NotNull ICredentials getServerCredentials() throws OpenstackManagerException {
        try {
            return this.manager.getFramework().getCredentialsService().getCredentials(WindowsCredentials.get(this.image));
        } catch (Exception e) {
            throw new OpenstackManagerException("Unable to create credentials", e);
        }
    }

    public void build() throws OpenstackManagerException, ConfigurationPropertyStoreException {
        logger.info("Building OpenStack Windows instance " + this.instanceName + " with image " + this.image + " for tag "
                + this.tag);

        String flavor = WindowsFlavor.get(this.image);

        Server server = new Server();
        server.name = this.instanceName;
        server.imageRef = getOpenstackHttpClient().getImageId(WindowsName.get(this.image));
        server.flavorRef = getOpenstackHttpClient().getFlavourId(flavor);
        server.availability_zone = WindowsAvailablityZone.get(this.image);
        server.metadata = new GalasaMetadata();
        server.metadata.galasa_run = this.manager.getFramework().getTestRunName();
        server.key_name = WindowsKeyPair.get(this.image);

        List<String> groups = WindowsSecurityGroups.get(this.image);
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
            logger.info("Home directory for Windows image tagged " + tag + " is " + homeDir);
        } catch (IpNetworkManagerException e) {
            throw new OpenstackManagerException("Unable to determine home directory", e);
        }

    }

    @Override
    public @NotNull ICommandShell getCommandShell() throws WindowsManagerException {
        return getServerCommandShell();
    }

    @Override
    public @NotNull Path getRoot() throws WindowsManagerException {
        return this.pathRoot;
    }

    @Override
    public @NotNull Path getHome() throws WindowsManagerException {
        return this.pathHome;
    }

    @Override
    public @NotNull Path getTmp() throws WindowsManagerException {
        return this.pathTemp;
    }

    @Override
    public @NotNull Path getRunDirectory() throws WindowsManagerException {
        if (this.pathRunDirectory != null) {
            return this.pathRunDirectory;
        }

        this.pathRunDirectory = this.pathHome.resolve(this.manager.getFramework().getTestRunName());

        try {
            Files.createDirectories(pathRunDirectory);
        } catch(Exception e) {
            throw new WindowsManagerException("Unable to create the run directory on server", e);
        }

        return this.pathRunDirectory;
    }
}
