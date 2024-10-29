/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.selenium.SeleniumManagerException;

/**
 * Selenium Driver Version for Containerised Node
 * 
 * @galasa.cps.property
 * 
 * @galasa.name selenium.image.node.version
 * 
 * @galasa.description Provides the version number for the docker image that will be used for both the provisioning of docker and kubernetes selenium nodes.
 * 
 * @galasa.required no
 * 
 * @galasa.valid_values 4.0.0-beta-2-20210317
 * 
 * @galasa.examples 
 * <code>selenium.image.node.version=4.0.0-beta-2-20210317</code>
 * 
 */
public class SeleniumDockerNodeVersion extends CpsProperties {

    public static String get() throws SeleniumManagerException {
        return getStringWithDefault(SeleniumPropertiesSingleton.cps(), "4.0.0-beta-2-20210317", "image", "node.version");
    }

}