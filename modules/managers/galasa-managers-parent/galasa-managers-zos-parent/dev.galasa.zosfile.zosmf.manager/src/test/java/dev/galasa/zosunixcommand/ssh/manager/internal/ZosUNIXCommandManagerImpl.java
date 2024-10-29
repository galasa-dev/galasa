/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosunixcommand.ssh.manager.internal;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.spi.IZosUNIXCommandSpi;


/**
 * Dummy interface for test 
 */
public class ZosUNIXCommandManagerImpl extends AbstractManager implements IZosUNIXCommandSpi {
    @Override
    public @NotNull IZosUNIXCommand getZosUNIXCommand(IZosImage image) {
        return null;
    }
}
