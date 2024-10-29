/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux.internal.dse;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ICredentials;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.linux.LinuxManagerException;
import dev.galasa.linux.internal.LinuxManagerImpl;
import dev.galasa.linux.internal.properties.LinuxArchivesDirectory;
import dev.galasa.linux.internal.properties.RetainRunDirectory;
import dev.galasa.linux.spi.ILinuxProvisionedImage;

public class LinuxDSEImage implements ILinuxProvisionedImage {

    private final Log                                logger = LogFactory.getLog(LinuxDSEImage.class);

    private final LinuxManagerImpl                   linuxManager;
    private final IConfigurationPropertyStoreService cps;
    private final String                             tag;
    private final ICommandShell                      commandShell;
    private final FileSystem                         fileSystem;
    private final LinuxDSEIpHost                     ipHost;
    private final String                             hostid;

    private final Path                               pathHome;
    private final Path                               pathTemp;
    private final Path                               pathRoot;
    private Path                                     pathRunDirectory;

    public LinuxDSEImage(LinuxManagerImpl manager, IConfigurationPropertyStoreService cps, String tag, String hostid)
            throws LinuxManagerException, ConfigurationPropertyStoreException {
        this.linuxManager = manager;
        this.cps = cps;
        this.tag = tag;
        this.hostid = hostid;

        try {
            this.ipHost = new LinuxDSEIpHost(this.linuxManager, hostid);
        } catch (Exception e) {
            throw new LinuxManagerException("Unable to create the IP Host for host " + this.hostid, e);
        }

        logger.debug("Linux DSE host " + this.hostid + " has hostname of " + this.ipHost.getHostname());

        this.commandShell = createCommandShell();
        this.fileSystem = createFileSystem();

        this.pathRoot = this.fileSystem.getPath("/");
        this.pathTemp = this.fileSystem.getPath("/tmp");

        try {
            logger.trace("Checking access to Linux DSE host " + this.hostid);
            String homeDir = this.commandShell.issueCommand("pwd");
            if (homeDir == null) {
                throw new LinuxManagerException("Unable to determine home directory, response null");
            }
            homeDir = homeDir.replaceAll("\\r\\n?|\\n", "");
            this.pathHome = this.fileSystem.getPath(homeDir);
            logger.info("Home directory for linux image tagged " + tag + " is " + homeDir);
        } catch (IpNetworkManagerException e) {
            throw new LinuxManagerException("Unable to determine home directory", e);
        }

    }

    private FileSystem createFileSystem() throws LinuxManagerException {
        try {
            return this.linuxManager.getIpNetworkManager().getFileSystem(this.ipHost);
        } catch (Exception e) {
            throw new LinuxManagerException("Unable to initialise the File System", e);
        }
    }

    private ICommandShell createCommandShell() throws LinuxManagerException {
        try {
            return this.linuxManager.getIpNetworkManager().getCommandShell(this.ipHost,
                    this.ipHost.getDefaultCredentials());
        } catch (Exception e) {
            throw new LinuxManagerException("Unable to initialise the command shell", e);
        }
    }

    @Override
    public @NotNull String getImageID() {
        return "dse " + tag;
    }

    @Override
    public @NotNull IIpHost getIpHost() {
        return this.ipHost;
    }

    @Override
    public @NotNull ICredentials getDefaultCredentials() throws LinuxManagerException {
        try {
            return this.ipHost.getDefaultCredentials();
        } catch (IpNetworkManagerException e) {
            throw new LinuxManagerException("Unable to obtain default credentials for linux host tagged " + this.tag,
                    e);
        }
    }

    @Override
    public @NotNull ICommandShell getCommandShell() throws LinuxManagerException {
        return this.commandShell;
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

        this.pathRunDirectory = this.pathHome.resolve(this.linuxManager.getFramework().getTestRunName());

        try {
            Files.createDirectories(pathRunDirectory);
        } catch(Exception e) {
            throw new LinuxManagerException("Unable to create the run directory on server", e);
        }

        return this.pathRunDirectory;
    }

    public void discard() {
        if (this.pathRunDirectory != null) {
            try {
                if (RetainRunDirectory.get(this)) {
                    logger.warn("Retaining the run directory instead of discarding as requested");
                    return;
                }

                commandShell.issueCommand("rm -rf " + this.pathRunDirectory);
            } catch(Exception e) {
                logger.trace("Problem discarding the run directory");
            }
        }

    }

    @Override
    public @NotNull Path getArchivesDirectory() throws LinuxManagerException {
        try {
            return this.fileSystem.getPath(LinuxArchivesDirectory.get(this.hostid));
        } catch (Exception e) {
            throw new LinuxManagerException("Problem determining archives directory", e);
        }
    }

}
