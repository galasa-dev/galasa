/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.windows.internal;

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
import dev.galasa.windows.WindowsManagerException;
import dev.galasa.windows.internal.properties.RetainRunDirectory;
import dev.galasa.windows.spi.IWindowsProvisionedImage;


public class WindowsDSEImage implements IWindowsProvisionedImage {

    private final Log                                logger = LogFactory.getLog(WindowsDSEImage.class);

    private final WindowsManagerImpl                   windowsManager;
    private final IConfigurationPropertyStoreService cps;
    private final String                             tag;
    private final ICommandShell                      commandShell;
    private final FileSystem                         fileSystem;
    private final WindowsDSEIpHost                     ipHost;
    private final String                             hostid;

    private final Path                               pathHome;
    private final Path                               pathTemp;
    private final Path                               pathRoot;
    private Path                                     pathRunDirectory;


    public WindowsDSEImage(WindowsManagerImpl manager, IConfigurationPropertyStoreService cps, String tag, String hostid)
            throws WindowsManagerException, ConfigurationPropertyStoreException {
        this.windowsManager = manager;
        this.cps = cps;
        this.tag = tag;
        this.hostid = hostid;

        try {
            this.ipHost = new WindowsDSEIpHost(this.windowsManager, hostid);
        } catch (Exception e) {
            throw new WindowsManagerException("Unable to create the IP Host for host " + this.hostid, e);
        }

        this.commandShell = createCommandShell();
        this.fileSystem = createFileSystem();

        this.pathRoot = this.fileSystem.getPath("C:/");
        this.pathTemp = this.fileSystem.getPath("C:/tmp");

        try {
            String homeDir = this.commandShell.issueCommand("echo %cd%");
            if (homeDir == null) {
                throw new WindowsManagerException("Unable to determine home directory, response null");
            }
            
            homeDir = homeDir.replaceAll("\\r\\n?|\\n", "");
            this.pathHome = this.fileSystem.getPath(homeDir);
            logger.info("Home directory for windows image tagged " + tag + " is " + homeDir);
        } catch (IpNetworkManagerException e) {
            throw new WindowsManagerException("Unable to determine home directory", e);
        }

    }

    private FileSystem createFileSystem() throws WindowsManagerException {
        try {
            return this.windowsManager.getIpNetworkManager().getFileSystem(this.ipHost);
        } catch (Exception e) {
            throw new WindowsManagerException("Unable to initialise the File System", e);
        }
    }

    private ICommandShell createCommandShell() throws WindowsManagerException {
        try {
            ICommandShell commandShell =  this.windowsManager.getIpNetworkManager().getCommandShell(this.ipHost,
                    this.ipHost.getDefaultCredentials());
            commandShell.setRemoveAnsiEscapeCodes(true);
            return commandShell;
        } catch (Exception e) {
            throw new WindowsManagerException("Unable to initialise the command shell", e);
        }
    }

    @Override
    public @NotNull String getImageID() {
        return "dse" + tag;
    }

    @Override
    public @NotNull IIpHost getIpHost() {
        return this.ipHost;
    }

    @Override
    public @NotNull ICredentials getDefaultCredentials() throws WindowsManagerException {
        try {
            return this.ipHost.getDefaultCredentials();
        } catch (IpNetworkManagerException e) {
            throw new WindowsManagerException("Unable to obtain default credentials for windows host tagged " + this.tag,
                    e);
        }
    }

    @Override
    public @NotNull ICommandShell getCommandShell() throws WindowsManagerException {
        return this.commandShell;
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

        this.pathRunDirectory = this.pathHome.resolve(this.windowsManager.getFramework().getTestRunName());

        try {
            Files.createDirectories(pathRunDirectory);
        } catch(Exception e) {
            throw new WindowsManagerException("Unable to create the run directory on server", e);
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
}
