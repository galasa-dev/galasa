/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal;

import javax.validation.constraints.NotNull;

import dev.galasa.ipnetwork.IIpPort;

public class ZosIpPortImpl implements IIpPort {

    private final ZosIpHostImpl host;
    private final int        port;
    private final String     type;
    
    public ZosIpPortImpl(@NotNull ZosIpHostImpl host, int port, @NotNull String type) {
        this.host = host;
        this.port = port;
        this.type = type;
    }
    
	@Override
	public int getPortNumber() {
		return this.port;
	}

	@Override
	public ZosIpHostImpl getHost() {
		return this.host;
	}

	@Override
	public String getType() {
		return this.type;
	}

}
