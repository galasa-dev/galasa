/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common;

import java.net.URL;
import java.util.Properties;

public interface BootstrapLoader<Ex extends Exception> {
    public Properties getBootstrapProperties(URL bootstrapUrl) throws Ex ;
}
