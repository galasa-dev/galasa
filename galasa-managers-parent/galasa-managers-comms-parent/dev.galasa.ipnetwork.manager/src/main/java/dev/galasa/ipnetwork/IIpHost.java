/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ipnetwork;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;

/**
 * <p>
 * Represents a IP Host or Stack.
 * </p>
 * 
 * <p>
 * Use the appropriate host manager annotation to obtain an object
 * </p>
 * 
 *  
 *
 */
public interface IIpHost {

    /**
     * Get the default Hostname of the Host
     */
    @NotNull
    String getHostname();

    /**
     * Get the IPV4 Hostname of the Host
     */
    String getIpv4Hostname();

    /**
     * Get the IPV6 Hostname of the Host
     */
    String getIpv6Hostname();

    /**
     * Get the Telnet port, defaults to 23
     * 
     * @return Telnet port
     * @throws IpNetworkManagerException if there is a problem accessing the CPS
     */
    int getTelnetPort() throws IpNetworkManagerException;

    /**
     * Is the Telnet port secured by TLS, default false
     * 
     * @return secured?
     * @throws IpNetworkManagerException if there is a problem accessing the CPS
     */
    boolean isTelnetPortTls() throws IpNetworkManagerException;

    /**
     * Get the FTP port, defaults to 21
     * 
     * @return FTP port
     * @throws IpNetworkManagerException if there is a problem accessing the CPS
     */
    int getFtpPort() throws IpNetworkManagerException;

    /**
     * Is the FTP port secured by TLS, default false
     * 
     * @return secured?
     * @throws IpNetworkManagerException if there is a problem accessing the CPS
     */
    boolean isFtpPortTls() throws IpNetworkManagerException;

    /**
     * Get the SSH port, defaults to 22
     * 
     * @return SSH Port
     * @throws IpNetworkManagerException if there is a problem accessing the CPS
     */
    int getSshPort() throws IpNetworkManagerException;

    /**
     * @return
     * @throws IpNetworkManagerException
     */
    @NotNull
    ICredentials getDefaultCredentials() throws IpNetworkManagerException;

}
