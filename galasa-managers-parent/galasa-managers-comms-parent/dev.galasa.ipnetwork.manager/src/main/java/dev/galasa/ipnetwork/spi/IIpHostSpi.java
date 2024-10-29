/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ipnetwork.spi;

import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.ipnetwork.IIpPort;
import dev.galasa.ipnetwork.IpNetworkManagerException;

public interface IIpHostSpi extends IIpHost {

    IIpPort provisionPort(String type) throws IpNetworkManagerException;

    String getPrefixHost();

}
