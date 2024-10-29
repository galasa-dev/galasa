/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.kubernetes.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.kubernetes.IKubernetesNamespace;

/**
 * Provides the SPI for the Kubernetes Manager for Manager to Manager communication.
 * Should not be used by Test code
 * 
 *  
 *
 */
public interface IKubernetesManagerSpi {

    /**
     * Retrieve the Kubernetes Namespace that has a specific tag.
     * 
     * @param namespaceTag The tag to be used
     * @return {@link dev.galasa.kubernetes.IKubernetesNamespace} if the tag is found, or null if not
     */
    IKubernetesNamespace getNamespaceByTag(@NotNull String namespaceTag);

}
