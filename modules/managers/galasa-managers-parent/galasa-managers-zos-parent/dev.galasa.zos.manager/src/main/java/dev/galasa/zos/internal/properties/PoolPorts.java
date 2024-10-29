/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal.properties;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;

public class PoolPorts extends CpsProperties {

	public static @NotNull @NotNull List<String> get(String image) throws ZosManagerException, ConfigurationPropertyStoreException {
		return getStringList(ZosPropertiesSingleton.cps(), "image", "ports", image);
	}
}
