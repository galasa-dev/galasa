/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.example.docker.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import dev.galasa.example.docker.IDockerResource;

public class DockerManagerImplTest {

    @Test
    public void testResourceCreationWithTag() {
        String expectedTag = "TEST_TAG";
        IDockerResource resource = new DockerResourceImpl(expectedTag);
        
        assertNotNull(resource);
        assertEquals(expectedTag, resource.getTag());
    }

    @Test
    public void testResourceCreationWithNullTagThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            new DockerResourceImpl(null);
        });
    }
}

