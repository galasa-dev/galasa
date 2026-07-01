/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.example.docker.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DockerManagerField
 * 
 * Internal annotation used to mark fields that should be processed by this manager.
 * This annotation is applied to the public annotation (DockerResource)
 * to indicate that it should be handled by this manager.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface DockerManagerField {
    // Marker annotation - no fields needed
}

