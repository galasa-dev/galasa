/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosconsole.oeconsol.manager.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosconsole.ZosConsoleManagerException;

/**
 * The oeconsol path
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosconsole.oeconsole.[imageid].command.path
 * 
 * @galasa.description The path to the oeconsol command
 * 
 * @galasa.required No
 * 
 * @galasa.default oeconsol
 * 
 * @galasa.examples 
 * <code>zosconsole.oeconsole.command.path=oeconsol</code><br>
 * <code>zosconsole.MFSYSA.oeconsol.command.path=/tools/oeconsol</code>
 *
 */
public class OeconsolPath extends CpsProperties {

    public static String get(@NotNull String imageId) throws ZosConsoleManagerException {
    	return getStringWithDefault(OeconsolPropertiesSingleton.cps(), "oeconsol", "oeconsol", "command.path", imageId);
    }

}
