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
 * Docker Volume
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}DockerVolume</code> annotation provides the capability to bind or provision docker volumes. The 
 * volumes were desgined with three Docker volume use cases in mind:
 * 
 * <ol>
 * <li>Mounting configuration - in this usecase any volume to be mounted contains configuration data and must not be edited by the running 
 *     container, as this could affect parallelization of test running. Therefore, in the DockerVolume annotation, if a volume name is provided 
 *     (aka already exists), the mount will be read only.</li>
 * <li>Sharing volumes - when a volume is required for multiple containers to use to share data. This shoult not be a provided volume, so it 
 *     is expected that a volume name will not be passed to the DockerVolume annotation, and the docker engine will generate a name. This 
 *     volume will be tagged for later reference. Current limitation is that the config used to provision the volume must be used for all 
 *     containers wanting to mount the same volume. This results in the path having to be the same in all containers.</li>
 * <li>Persisting data - There may be a use case for a volume to exsist outside the life span of the test. For this I have encorparated a 
 *     boolean called persist on the DockerVolume annotation. This is not indefinate, but controlled by resource management. A good default 
 *     would probably be 24 hours, but can utimately be set by the user with a CPS property.</li>
 * </ol>
 * 
 * @galasa.examples
 * <code>{@literal @}DockerContainerConfig(
 *      dockerVolumes =  {
 *           // A read only mount, as a specific volume was requested.    
 *           {@literal @}DockerVolume(volumeName = config, mountPath = "/configs"),
 *           // A data volume that will persist past the life of the test
 *           {@literal @}DockerVolume(mountPath = "/data", persist = true),
 *           // A sharing volume that will be cleanup post test.
 *           {@literal @}DockerVolume(mountPath = "/mnt/appShare"),
 *       }
 *   )
 *   public IDockerContainerConfig config;
 * </code>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ValidAnnotatedFields({ IDockerVolume.class })
@DockerManagerField
public @interface DockerVolume {

    /**
     * By default it is expected that Galasa should provision and control the volume. This field should only be used if beinding to an
     * already exisitng volume.
     * 
     * @return
     */
    public String existingVolumeName() default "";

    /**
     * @return Where to mount the volume on the container.
     */
    public String mountPath();

    /**
     * When wanting to reference a mount that is going to be provisioned, this tag will be used.
     * 
     * @return The tag for this volume
     */
    public String volumeTag();

    /**
     * The <code>dockerEngineTag</code> will be used in the future so that a volume can be allocated on a specific Docker Engine type.
     * You would not normally need to provide a Docker Engine tag.
     * 
     * @return
     */
    public String dockerEngineTag() default "PRIMARY";

    /**
     * This field is used to protect this volume. If this volume is intended to be mounted to multiple containers, which you do not want 
     * editing the contents, set this to be true
     * 
     * @return true if the volume is read-only, false otherwise.
     */
    public boolean readOnly() default false;

}