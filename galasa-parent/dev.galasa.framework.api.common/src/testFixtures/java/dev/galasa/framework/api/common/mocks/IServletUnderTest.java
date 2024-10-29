/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.spi.IFramework;

public interface IServletUnderTest {
    void setFramework(IFramework framework);

    void setFileSystem(IFileSystem fileSystem);
}