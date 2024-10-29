/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zostsocommand.ssh.manager.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zostsocommand.ZosTSOCommandManagerException;

/**
 * The tsocmd path
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zostsocommand.[imageid].tsocmd.command.path
 * 
 * @galasa.description The path to the tsocmd command
 * 
 * @galasa.required No
 * 
 * @galasa.default tsocmd
 * 
 * @galasa.examples 
 * <code>zostsocommand.command.tsocmd.path=tsocmd</code><br>
 * <code>zostsocommand.MFSYSA.tsocmd.command.path=/tools/tsocmd</code>
 *
 */
public class TsocmdPath extends CpsProperties {

    public static String get(@NotNull String imageId) throws ZosTSOCommandManagerException {
        return getStringWithDefault(ZosTSOCommandSshPropertiesSingleton.cps(), "tsocmd", "command", "path", imageId);
    }

}
