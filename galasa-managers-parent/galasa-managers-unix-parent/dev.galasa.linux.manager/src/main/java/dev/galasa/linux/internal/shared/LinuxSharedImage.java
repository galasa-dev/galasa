/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux.internal.shared;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.security.SecureRandom;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DssDelete;
import dev.galasa.framework.spi.DssSwap;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.linux.LinuxManagerException;
import dev.galasa.linux.internal.LinuxManagerImpl;
import dev.galasa.linux.internal.properties.LinuxArchivesDirectory;
import dev.galasa.linux.spi.ILinuxProvisionedImage;

public class LinuxSharedImage implements ILinuxProvisionedImage {

    private static final Log                         logger = LogFactory.getLog(LinuxSharedImage.class);

    private final LinuxManagerImpl                   linuxManager;
    private final String                             tag;
    private final ICommandShell                      sudoCommandShell;
    private final ICommandShell                      userCommandShell;
    private final FileSystem                         fileSystem;
    private final LinuxSharedIpHost                  ipHost;
    private final String                             hostId;
    private final String                             username;
    private final ICredentialsUsernamePassword       usernameCredentials;

    private final Path                               pathHome;
    private final Path                               pathTemp;
    private final Path                               pathRoot;

    private final SecureRandom                       random = new SecureRandom();

    public LinuxSharedImage(LinuxManagerImpl manager, String tag, String hostid, String username)
            throws LinuxManagerException, ConfigurationPropertyStoreException {
        this.linuxManager = manager;
        this.tag = tag;
        this.hostId = hostid;
        this.username = username;

        try {
            this.ipHost = new LinuxSharedIpHost(this.linuxManager, hostid);
        } catch (Exception e) {
            throw new LinuxManagerException("Unable to create the IP Host for host " + this.hostId, e);
        }

        logger.debug("Linux shared image " + this.hostId + " has hostname of " + this.ipHost.getHostname());

        try {
            this.sudoCommandShell = createCommandShell(this.ipHost.getDefaultCredentials());

            logger.trace("Creating username " + this.username + " on host " + this.hostId);

            String response = this.sudoCommandShell.issueCommand("sudo useradd -s /bin/bash -m " + this.username + " && echo galasaresponse=$?");
            if (!response.contains("galasaresponse=0")) {
                throw new LinuxManagerException("useradd of username " + this.username + " failed:-\n" + response);
            }

            String tempPassword = "Galasa_Future_0f_Test1ng!"; //Not a secret but this raises a vulnerability on server side runs. Issue has been raised to correct this //pragma: allowlist secret
            for(int i = 0; i < 4; i++) {
                tempPassword = tempPassword + Integer.toString(this.random.nextInt(10));
            }

            this.linuxManager.getFramework().getConfidentialTextService().registerText(tempPassword, "Temporary password for username " + this.username);

            response = this.sudoCommandShell.issueCommand("echo -e \"" +tempPassword + "\n" + tempPassword + "\" | sudo passwd " + this.username + " && echo galasaresponse=$?");
            if (!response.contains("galasaresponse=0")) {
                throw new LinuxManagerException("passwd of username " + this.username + " failed:-\n" + response);
            }

            this.usernameCredentials = new CredentialsUsernamePassword(null, this.username, tempPassword);
            this.userCommandShell = createCommandShell(this.usernameCredentials);


            this.fileSystem = createFileSystem(this.usernameCredentials);

            this.pathRoot = this.fileSystem.getPath("/");
            this.pathTemp = this.fileSystem.getPath("/tmp");

            String homeDir = this.userCommandShell.issueCommand("pwd");
            if (homeDir == null) {
                throw new LinuxManagerException("Unable to determine home directory, response null");
            }
            homeDir = homeDir.replaceAll("\\r\\n?|\\n", "");
            this.pathHome = this.fileSystem.getPath(homeDir);
            logger.info("Home directory for linux image tagged " + tag + " is " + homeDir);
        } catch (IpNetworkManagerException | CredentialsException e) {
            throw new LinuxManagerException("Unable to create username " + this.username, e);
        }

    }

    private FileSystem createFileSystem(ICredentialsUsernamePassword credentials) throws LinuxManagerException {
        try {
            return this.linuxManager.getIpNetworkManager().getFileSystem(this.ipHost, credentials);
        } catch (Exception e) {
            throw new LinuxManagerException("Unable to initialise the File System", e);
        }
    }

    private ICommandShell createCommandShell(@NotNull ICredentials iCredentials) throws LinuxManagerException {
        try {
            return this.linuxManager.getIpNetworkManager().getCommandShell(this.ipHost,
                    iCredentials);
        } catch (Exception e) {
            throw new LinuxManagerException("Unable to initialise the command shell", e);
        }
    }

    @Override
    public @NotNull String getImageID() {
        return this.hostId;
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
        return this.userCommandShell;
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
        // As this is a unique username,   the run directory can be the home directory

        return this.pathHome;
    }

    public void discard() {

        for(int tries = 3; tries > 0; tries--) {
            try {
                discardDssSlot(this.linuxManager.getDss(), this.hostId, this.linuxManager.getFramework().getTestRunName());
                logger.info("Discarded Linux shared image " + this.hostId + " with username " + this.username);
                break;
            } catch(DynamicStatusStoreMatchException e) {
                // Try again
                try {
                    Thread.sleep(200 + this.random.nextInt(200));
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                    logger.error("Wait interrupted",e);
                    return;
                } // *** To avoid race conditions
            } catch(DynamicStatusStoreException e) {
                logger.error("Failed to release dss slot resources",e);
                return;
            }
        }

        try {
            if (this.userCommandShell != null) {
                this.userCommandShell.disconnect();
            }
            if (this.fileSystem != null) {
                this.fileSystem.close();
            }

            discardDssUsername(this.linuxManager.getDss(), this.sudoCommandShell, this.hostId, this.username, this.linuxManager.getFramework().getTestRunName());
        } catch(DynamicStatusStoreException | IpNetworkManagerException | IOException e) {
            logger.error("Failed to release dss slot resources",e);
            return;
        }
    }

    public static void discardDssSlot(IDynamicStatusStoreService dss, String hostId, String runName) throws DynamicStatusStoreException {

        int usedSlots = 0;
        String sUsedSlots = dss.get(hostId + ".used.slots");
        if (sUsedSlots != null) {
            usedSlots = Integer.parseInt(sUsedSlots);
        }

        usedSlots--;
        if (usedSlots < 0) {
            usedSlots = 0;
        }

        DssSwap slotCount = new DssSwap(hostId + ".used.slots", sUsedSlots, Integer.toString(usedSlots));
        DssDelete runImage = new DssDelete("run." + runName + ".image." + hostId, null);

        dss.performActions(slotCount, runImage);
    }

    public static void discardDssUsername(IDynamicStatusStoreService dss, ICommandShell commandShell, String hostId, String username, String runName) throws DynamicStatusStoreException {

        try {
            // First attempt to kill all processes for the username
            boolean killed = false;
            boolean noexist = false;
            for (int tries = 4; tries > 0; tries ++) {
                String response = commandShell.issueCommand("sudo pkill -U " + username);
                if (response.contains("invalid user name")) {
                    noexist = true;
                    break;
                }
                response = commandShell.issueCommand("sudo ps -U " + username);
                if (!response.contains(username)) {
                    killed = true;
                    break;
                }
            }

            if (!noexist) {
                if (!killed) {
                    logger.error("Failed to kill " + username +" proceses");
                    return;
                }
                // now delete the username
                String response = commandShell.issueCommand("sudo userdel -r " + username + " && echo galasaresponse=$?");
                if (!response.contains("galasaresponse=0")) {
                    throw new LinuxManagerException("useradd of username " + username + " failed:-\n" + response);
                }
            }
        } catch(Exception e) {
            logger.error("Failed to delete username " + username + " on Linux shared image " + hostId,e);
            return;
        }

        DssDelete usernameAllocate = new DssDelete("image." + hostId + ".username." + username, null);
        DssDelete runUsername = new DssDelete("run." + runName + ".image." + hostId + ".username." + username, null);

        dss.performActions(usernameAllocate, runUsername);
    }

    @Override
    public @NotNull Path getArchivesDirectory() throws LinuxManagerException {
        try {
            return this.fileSystem.getPath(LinuxArchivesDirectory.get(this.hostId));
        } catch (Exception e) {
            throw new LinuxManagerException("Problem determining archives directory", e);
        }
    }


}
