/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.java.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.java.IJavaInstallation;
import dev.galasa.java.JavaManagerException;

public interface IJavaManagerSpi {
    
    void registerJavaInstallationForTag(@NotNull String tag, @NotNull IJavaInstallation javaInstallation) throws JavaManagerException;

    @NotNull
    IJavaInstallation getInstallationForTag(@NotNull String tag) throws JavaManagerException;
    
}
