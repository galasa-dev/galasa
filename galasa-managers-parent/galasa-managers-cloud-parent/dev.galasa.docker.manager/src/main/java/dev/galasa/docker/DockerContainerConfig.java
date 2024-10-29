/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.galasa.docker.internal.DockerManagerField;
import dev.galasa.framework.spi.ValidAnnotatedFields;

/**
 * Docker Container Configuation
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}DockerContainerConfig</code> annotation provides an object to manually configure certain aspects
 * of a containers run. Within the annotation, volumes can be requests, for both binding and provisioning. Look at the Docker volume annotation 
 * description for more details. The IDockerContainerConfig object it self allows for non provisioing configurations to be set at test time and 
 * ammended between container startups. The IDockerContainer object needs to use the startWithConfig() method to take use of the customised 
 * startup config
 * 
 * @galasa.examples
 * <pre>
   {@literal @}DockerContainerConfig(
        dockerVolumes =  {
            {@literal @}DockerVolume(mountPath = "/tmp/testvol"),
        }
    )
    public IDockerContainerConfig config;
   </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ValidAnnotatedFields({ IDockerContainerConfig.class })
@DockerManagerField
public @interface DockerContainerConfig {

    /**
     * Multiple volumes can be mounted within a single configuration
     * 
     * @return
     */
    public DockerVolume[] dockerVolumes() default {};
    
}