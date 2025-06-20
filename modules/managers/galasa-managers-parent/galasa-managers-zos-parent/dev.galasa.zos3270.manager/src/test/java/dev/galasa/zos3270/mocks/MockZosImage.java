/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.mocks;

import java.nio.charset.Charset;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;

public class MockZosImage implements IZosImage {

    private String imageId;

    public MockZosImage(String imageId) {
        this.imageId = imageId;
    }

    @Override
    public @NotNull String getImageID() {
        return this.imageId;
    }

    @Override
    public @NotNull String getSysname() {
        throw new UnsupportedOperationException("Unimplemented method 'getSysname'");
    }

    @Override
    public @NotNull String getVtamLogonString(String applid) {
        throw new UnsupportedOperationException("Unimplemented method 'getVtamLogonString'");
    }

    @Override
    public String getLogonInitialText() {
        throw new UnsupportedOperationException("Unimplemented method 'getLogonInitialText'");
    }

    @Override
    public @NotNull String getSysplexID() {
        throw new UnsupportedOperationException("Unimplemented method 'getSysplexID'");
    }

    @Override
    public String getClusterID() {
        throw new UnsupportedOperationException("Unimplemented method 'getClusterID'");
    }

    @Override
    public @NotNull Charset getCodePage() {
        throw new UnsupportedOperationException("Unimplemented method 'getCodePage'");
    }

    @Override
    public @NotNull String getDefaultHostname() throws ZosManagerException {
        throw new UnsupportedOperationException("Unimplemented method 'getDefaultHostname'");
    }

    @Override
    public @NotNull ICredentials getDefaultCredentials() throws ZosManagerException {
        throw new UnsupportedOperationException("Unimplemented method 'getDefaultCredentials'");
    }

    @Override
    public @NotNull IIpHost getIpHost() {
        throw new UnsupportedOperationException("Unimplemented method 'getIpHost'");
    }

    @Override
    public @NotNull String getHome() throws ZosManagerException {
        throw new UnsupportedOperationException("Unimplemented method 'getHome'");
    }

    @Override
    public @NotNull String getRunTemporaryUNIXPath() throws ZosManagerException {
        throw new UnsupportedOperationException("Unimplemented method 'getRunTemporaryUNIXPath'");
    }

    @Override
    public @NotNull String getJavaHome() throws ZosManagerException {
        throw new UnsupportedOperationException("Unimplemented method 'getJavaHome'");
    }

    @Override
    public @NotNull String getLibertyInstallDir() throws ZosManagerException {
        throw new UnsupportedOperationException("Unimplemented method 'getLibertyInstallDir'");
    }

    @Override
    public @NotNull String getZosConnectInstallDir() throws ZosManagerException {
        throw new UnsupportedOperationException("Unimplemented method 'getZosConnectInstallDir'");
    }
    
}
