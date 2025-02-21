/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal;

import java.nio.charset.Charset;
import java.text.MessageFormat;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.properties.ImageCodePage;
import dev.galasa.zos.internal.properties.ImageLogonInitialText;
import dev.galasa.zos.internal.properties.ImageSysname;
import dev.galasa.zos.internal.properties.ImageVtamLogon;

public abstract class ZosBaseImageImpl implements IZosImage {

    private final ZosManagerImpl zosManager;
    private final IConfigurationPropertyStoreService cps;

    private final String        imageId;
    private final String        sysname;
    private final String        vtamLogonString;
    private final String        logonInitialText;
    private final String        clusterId;
    private final String        sysplexID;
    private final String        defaultCredentialsId;
    private final ZosIpHostImpl ipHost;
    private final Charset       codePage;

    private final String        runTemporaryUNIXPath;
    private final String         javaHome;
    private final String         libertyInstallDir;
    private final String         zosConnectInstallDir;

    private ICredentials defaultCedentials;

    private static final String SLASH_SYBMOL = "/";

    public ZosBaseImageImpl(ZosManagerImpl zosManager, String imageId, String clusterId) throws ZosManagerException {
        this.zosManager = zosManager;
        this.cps = zosManager.getCPS();
        this.imageId    = imageId;
        this.clusterId  = clusterId;

        try {
            this.codePage = ImageCodePage.get(this.imageId);
            this.sysname = ImageSysname.get(this.imageId);
            this.vtamLogonString = ImageVtamLogon.get(this.imageId);
            this.logonInitialText = ImageLogonInitialText.get(this.imageId);
            this.sysplexID = AbstractManager.nulled(this.cps.getProperty("image." + this.imageId, "sysplex"));
            this.defaultCredentialsId = AbstractManager.defaultString(this.cps.getProperty("image", "credentials", this.imageId), "ZOS");
        } catch(Exception e) {
            throw new ZosManagerException("Problem populating Image " + this.imageId + " properties", e);
        }

        try {
            this.ipHost = new ZosIpHostImpl(zosManager, imageId);
        } catch(Exception e) {
            throw new ZosManagerException("Unable to create the IP Host for the image " + this.imageId, e);
        }
        
        this.runTemporaryUNIXPath = this.zosManager.getRunUNIXPathPrefix(this) + SLASH_SYBMOL + this.zosManager.getRunId() + SLASH_SYBMOL;
        this.javaHome = this.zosManager.getJavaHome(this);
        this.libertyInstallDir = this.zosManager.getLibertyInstallDir(this);
        this.zosConnectInstallDir = this.zosManager.getZosConnectInstallDir(this);
    }

    protected IConfigurationPropertyStoreService getCPS() {
        return this.cps;
    }

    protected ZosManagerImpl getZosManager() {
        return this.zosManager;
    }

    @Override
    public @NotNull String getImageID() {
        return this.imageId;
    }

    @Override
    public @NotNull String getSysname() {
        return this.sysname;
    }

    @Override
    public @NotNull String getVtamLogonString(String applid) {
        return MessageFormat.format(this.vtamLogonString, applid);
    }

    public @NotNull String getLogonInitialText() {
        return this.logonInitialText;
    }

    @Override
    public String getSysplexID() {
        if (this.sysplexID == null) {
            return this.getImageID();
        }
        return this.sysplexID;
    }

    @Override
    public String getClusterID() {
        return this.clusterId;
    }

    @Override
    public @NotNull Charset getCodePage() {
        return this.codePage;
    }

    @Override
    public @NotNull String getDefaultHostname() throws ZosManagerException {
        return this.ipHost.getHostname();
    }

    @Override
    public ICredentials getDefaultCredentials() throws ZosManagerException {
        if (this.defaultCedentials != null) {
            return this.defaultCedentials;
        }

        try {
            ICredentialsService credsService = zosManager.getFramework().getCredentialsService();

            this.defaultCedentials = credsService.getCredentials(this.defaultCredentialsId);
            if (this.defaultCedentials == null) {
                this.defaultCedentials = credsService.getCredentials(this.defaultCredentialsId.toUpperCase());
            }
        } catch (CredentialsException e) {
            throw new ZosManagerException("Unable to acquire the credentials for id " + this.defaultCredentialsId, e);
        }

        if (this.defaultCedentials == null) {
            throw new ZosManagerException("zOS Credentials missing for image " + this.imageId + " id " + this.defaultCredentialsId);
        }

        return defaultCedentials;
    }

    @Override
    public ZosIpHostImpl getIpHost() {
        return this.ipHost;
    }

    @Override
    public String getHome() {
        return getRunTemporaryUNIXPath();
    }

    @Override
    public String getRunTemporaryUNIXPath() {
        return this.runTemporaryUNIXPath;
    }

    @Override
    public String getJavaHome() {
        return this.javaHome;
    }

    @Override
    public String getLibertyInstallDir() {
        return this.libertyInstallDir;
    }

    @Override
    public String getZosConnectInstallDir() {
        return this.zosConnectInstallDir;
    }

    @Override
    public String toString() {
        return this.imageId;
    }
}
