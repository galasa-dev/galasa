/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ipnetwork.internal;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicResource;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.ipnetwork.IIpPort;
import dev.galasa.ipnetwork.spi.IIpHostSpi;

public class IpPortImpl implements IIpPort {

    private final IIpHostSpi host;
    private final int        port;
    private final String     type;

    private IpPortImpl(@NotNull IIpHostSpi host, int port, @NotNull String type) {
        this.host = host;
        this.port = port;
        this.type = type;
    }

    @Override
    public int getPortNumber() {
        return this.port;
    }

    @Override
    public IIpHost getHost() {
        return this.host;
    }

    @Override
    public String getType() {
        return this.type;
    }

    protected static IpPortImpl allocateDss(String runName, IIpHostSpi host, int port, String type,
            IDynamicStatusStoreService dss) throws DynamicStatusStoreException {
        String sPort = Integer.toString(port);

        String dssPrefix = host.getPrefixHost() + ".port." + sPort;

        // *** First try and assign a control set, if this fails to swap, then the port
        // has been allocated by someone else
        HashMap<String, String> otherProps = new HashMap<>();
        otherProps.put("port.run." + runName + "." + dssPrefix, "active");
        otherProps.put(dssPrefix + ".type", type);

        if (!dss.putSwap(dssPrefix, null, runName, otherProps)) {
            return null;
        }

        IDynamicResource dssResource = dss.getDynamicResource(host.getPrefixHost() + ".port");
        otherProps = new HashMap<>();
        otherProps.put(sPort, runName);
        otherProps.put(sPort + ".type", type);
        otherProps.put(sPort + ".allocated", Instant.now().toString());
        dssResource.put(otherProps);

        return new IpPortImpl(host, port, type);
    }

    protected static void deleteDss(String runName, String prefixHost, String port, IDynamicStatusStoreService dss)
            throws DynamicStatusStoreException {
        IDynamicResource dssResource = dss.getDynamicResource(prefixHost + ".port");

        // *** Delete the user view set first before the control set
        HashSet<String> dssProperties = new HashSet<>();
        dssProperties.add(port);
        dssProperties.add(port + ".allocated");
        dssProperties.add(port + ".type");
        dssResource.delete(dssProperties);

        // *** Delete the control set
        String dssKey = prefixHost + ".port." + port;
        dssProperties = new HashSet<>();
        dssProperties.add(dssKey);
        dssProperties.add(dssKey + ".type");
        dssProperties.add("port.run." + runName + "." + dssKey);
        dss.delete(dssProperties);

    }

    protected void discard(String runName, IDynamicStatusStoreService dss) throws DynamicStatusStoreException {
        IpPortImpl.deleteDss(runName, this.host.getPrefixHost(), Integer.toString(port), dss);
    }

}
