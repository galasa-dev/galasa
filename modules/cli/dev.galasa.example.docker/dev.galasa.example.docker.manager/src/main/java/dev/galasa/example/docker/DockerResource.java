/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.example.docker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.example.docker.internal.DockerManagerField;
import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * Docker Resource Annotation
 * 
 * This annotation is used to inject a Docker resource into a test class.
 * 
 * Example usage:
 * <pre>
 * {@literal @}DockerResource
 * public IDockerResource resource;
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ValidAnnotatedFields({ IDockerResource.class })
@DockerManagerField
public @interface DockerResource {
    
    /**
     * The tag for this resource instance
     * 
     * @return the resource tag, defaults to "PRIMARY"
     */
    public String resourceTag() default "PRIMARY";
}

