/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ipnetwork.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.ipnetwork.IpNetworkManagerException;

public abstract class AbstractGenericIpHost implements IIpHostSpi {

    private final IConfigurationPropertyStoreService cps;
    private final IDynamicStatusStoreService         dss;
    private final ICredentialsService                creds;
    private final String                             prefix;
    private final String                             hostid;

    private final String                             hostnameIPV4;
    private final String                             hostnameIPV6;

    public AbstractGenericIpHost(IConfigurationPropertyStoreService cps, IDynamicStatusStoreService dss,
            ICredentialsService creds, String prefix, String hostid) throws IpNetworkManagerException {
        this.cps = cps;
        this.dss = dss;
        this.creds = creds;
        this.prefix = prefix;
        this.hostid = hostid;

        try {
            hostnameIPV4 = AbstractManager.nulled(this.cps.getProperty(prefix, hostid + ".ipv4.hostname"));
            hostnameIPV6 = AbstractManager.nulled(this.cps.getProperty(prefix, hostid + ".ipv6.hostname"));
            if (hostnameIPV4 == null && hostnameIPV6 == null) {
                throw new IpNetworkManagerException("Unable to locate a hostname for hostid " + hostid); // *** TODO
                                                                                                         // give example
                                                                                                         // property ids
            }
        } catch (IpNetworkManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new IpNetworkManagerException("Problem populating the hostname properties", e);
        }
    }

    @Override
    public String getPrefixHost() {
        return this.prefix + "." + this.hostid;
    }

    @Override
    public String getHostname() {
        if (hostnameIPV4 != null) {
            return this.hostnameIPV4;
        }
        return this.hostnameIPV6;
    }

    @Override
    public String getIpv4Hostname() {
        return this.hostnameIPV4;
    }

    @Override
    public String getIpv6Hostname() {
        return this.hostnameIPV6;
    }

    @Override
    public int getTelnetPort() throws IpNetworkManagerException {
        try {
            String temp = AbstractManager.nulled(this.cps.getProperty(this.prefix, "telnet.port", this.hostid));
            if (temp == null) {
                return 23;
            }
            return Integer.parseInt(temp);
        } catch (Exception e) {
            throw new IpNetworkManagerException("Unable to retrieve telnet port property for host " + this.hostid, e);
        }
    }

    @Override
    public boolean isTelnetPortTls() throws IpNetworkManagerException {
        try {
            return Boolean
                    .parseBoolean(AbstractManager.nulled(this.cps.getProperty(this.prefix, "telnet.tls", this.hostid)));
        } catch (Exception e) {
            throw new IpNetworkManagerException("Unable to retrieve telnet tls property for host " + this.hostid, e);
        }
    }

    @Override
    public int getFtpPort() throws IpNetworkManagerException {
        try {
            String temp = AbstractManager.nulled(this.cps.getProperty(this.prefix, "ftp.port", this.hostid));
            if (temp == null) {
                return 21;
            }
            return Integer.parseInt(temp);
        } catch (Exception e) {
            throw new IpNetworkManagerException("Unable to retrieve ftp port property for host " + this.hostid, e);
        }
    }

    @Override
    public boolean isFtpPortTls() throws IpNetworkManagerException {
        try {
            return Boolean
                    .parseBoolean(AbstractManager.nulled(this.cps.getProperty(this.prefix, "ftp.tls", this.hostid)));
        } catch (Exception e) {
            throw new IpNetworkManagerException("Unable to retrieve ftp tls property for host " + this.hostid, e);
        }
    }

    @Override
    public int getSshPort() throws IpNetworkManagerException {
        try {
            String temp = AbstractManager.nulled(this.cps.getProperty(this.prefix, "ssh.port", this.hostid));
            if (temp == null) {
                return 22;
            }
            return Integer.parseInt(temp);
        } catch (Exception e) {
            throw new IpNetworkManagerException("Unable to retrieve ssh port property for host " + this.hostid, e);
        }
    }

    @Override
    public @NotNull ICredentials getDefaultCredentials() throws IpNetworkManagerException {
        try {
            String temp = AbstractManager.nulled(this.cps.getProperty(this.prefix, "credentials", this.hostid));
            if (temp == null) {
                throw new IpNetworkManagerException("Unable to obtain credentials id for host " + this.hostid);
            }

            ICredentials creds = this.creds.getCredentials(temp);
            if (creds == null) {
                throw new IpNetworkManagerException(
                        "Unable to obtain default credentials, the credentials id for host " + temp + " is missing");
            }
            return creds;
        } catch (IpNetworkManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new IpNetworkManagerException("Unable to retrieve ssh port property for host " + this.hostid, e);
        }
    }
}
