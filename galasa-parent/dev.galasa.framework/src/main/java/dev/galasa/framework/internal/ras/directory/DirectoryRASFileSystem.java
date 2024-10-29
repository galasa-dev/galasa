/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.ras.directory;

import dev.galasa.framework.spi.ras.ResultArchiveStoreFileSystem;
import dev.galasa.framework.spi.ras.ResultArchiveStoreFileSystemProvider;

/**
 * The Directory RAS File System, basically use the shared generic version
 *
 *  
 *
 */
public class DirectoryRASFileSystem extends ResultArchiveStoreFileSystem {

    public DirectoryRASFileSystem(ResultArchiveStoreFileSystemProvider fileSystemProvider) {
        super(fileSystemProvider);
    }

}
