/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ipnetwork.spi;

import java.nio.file.FileSystem;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.ipnetwork.IpNetworkManagerException;

public interface IIpNetworkManagerSpi {

    @NotNull
    ICommandShell getCommandShell(IIpHost host, ICredentials credentials) throws IpNetworkManagerException;

    @NotNull
    FileSystem getFileSystem(IIpHost host) throws IpNetworkManagerException;

    @NotNull
    FileSystem getFileSystem(IIpHost host, ICredentials credentials) throws IpNetworkManagerException;

}
