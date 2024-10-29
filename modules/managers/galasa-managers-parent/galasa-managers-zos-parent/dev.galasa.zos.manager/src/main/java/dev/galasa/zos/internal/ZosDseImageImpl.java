/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal;

import dev.galasa.zos.ZosManagerException;

public class ZosDseImageImpl extends ZosBaseImageImpl {

    public ZosDseImageImpl(ZosManagerImpl zosManager, String imageId, String clusterId) throws ZosManagerException {
        super(zosManager, imageId, clusterId);
    }

}
