/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.example.docker;

/**
 * IDockerResource
 *
 * Resource interface injected into test classes via @DockerResource annotation.
 * Add methods here that tests will use to interact with the resource.
 */
public interface IDockerResource {
    
    /**
     * Get the tag for this resource instance
     *
     * @return the resource tag
     */
    String getTag();
    
    // TODO: Add resource methods here
}

