/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cloud.internal;

import dev.galasa.cloud.spi.ICloudContainerPort;

public class CloudContainerPortImpl implements ICloudContainerPort {
	
	private final String name;
	private final int    port;
	private final String type;
	
	public CloudContainerPortImpl(String name,
			int port,
			String type) {
		this.name = name;
		this.port = port;
		this.type = type;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int getPort() {
		return this.port;
	}

	@Override
	public String getType() {
		return this.type;
	}

}
