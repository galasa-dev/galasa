/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.example.docker.internal;

import java.util.Objects;

import dev.galasa.example.docker.IDockerResource;

/**
 * Docker Resource Implementation
 *
 * This class implements the IDockerResource interface and represents
 * a Docker resource that is injected into test classes via the
 * @DockerResource annotation.
 *
 * Each resource instance is identified by a unique tag, which allows tests to request
 * multiple different resources of the same type. The tag is specified in the annotation
 * and defaults to "PRIMARY" if not provided.
 *
 * This is an internal implementation class and should not be used directly by tests.
 * Tests should only interact with the IDockerResource interface.
 *
 * @see IDockerResource
 * @see DockerResource
 */
public class DockerResourceImpl implements IDockerResource {
    
    /**
     * The unique identifier for this resource instance.
     * Used to distinguish between multiple resources of the same type.
     */
    private final String tag;
    
    /**
     * Constructs a new Docker resource with the specified tag.
     *
     * @param tag the unique identifier for this resource instance, must not be null
     * @throws NullPointerException if tag is null
     */
    public DockerResourceImpl(String tag) {
        this.tag = Objects.requireNonNull(tag, "tag cannot be null");
    }

    /**
     * Returns the unique tag identifier for this resource instance.
     *
     * The tag is used to identify and distinguish between multiple resources
     * of the same type within a test. It corresponds to the resourceTag parameter
     * in the @DockerResource annotation.
     *
     * @return the resource tag, never null
     */
    @Override
    public String getTag() {
        return this.tag;
    }
}

