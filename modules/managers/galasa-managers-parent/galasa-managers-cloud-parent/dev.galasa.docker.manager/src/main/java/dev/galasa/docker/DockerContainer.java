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

import dev.galasa.framework.spi.ValidAnnotatedFields;
import dev.galasa.docker.internal.DockerManagerField;

/**
 * Docker Container
 * 
 * @galasa.annotation
 * 
 * @galasa.description The <code>{@literal @}DockerContainer</code> annotation requests the Docker Manager to allocate a slot and start a container 
 * on the infrastructure Docker Engines. The test can request as many containers as required within 
 * the limits set by the Docker Manager configuration.
 * 
 * @galasa.examples 
 * <code>{@literal @}DockerContainer(image="library/httpd:latest")<br>
 * public IDockerContainer httpdContainer;<br>
 * {@literal @}DockerContainer(image="privateimage", start=false)<br>
 * public IDockerContainer container1;<br>
 * </code>
 * 
 * @galasa.extra
 * The <code>IDockerContainer</code> interface gives the test access to the IPv4/6 address and the exposed port numbers of the Docker Container. 
 * The interface also enables the test to execute commands and retrieve the log and transfer files that are sent to 
 * and from the container.<br><br>
 * See {@link DockerContainer} and {@link IDockerContainer} to find out more.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@ValidAnnotatedFields({ IDockerContainer.class })
@DockerManagerField
public @interface DockerContainer {

    /**
     * The <code>dockerContainerTag</code> is used to identify the Docker Container to other Managers or Shared Environments.  If a test is using multiple 
     * Docker Containers, each separate Docker Container must have a unique tag. If two Docker Containers use the same tag, they will refer to the 
     * same Docker Container.
     * @return The tag for this container.
     */
    public String dockerContainerTag() default "PRIMARY";

    /**
     * The <code>image</code> attribute provides the Docker Image that is used to create the Docker Container.  The image name must not 
     * include the Docker Registry as this is provided in the CPS.   If using a public official image from DockerHub,  then the 
     * image name must be prefixed with <code>library/</code>, for example <code>library/httpd:latest</code>, the Docker Manager will
     * not default to the library namespace like the Docker commands do.
     * @return the name of the image.
     */
    public String image();

    /**
     * The <code>start</code> attribute indicates whether the Docker Container should be started automatically. If the 
     * test needs to perform some work before the container is started, then <code>start=false</code> should be used, after which 
     * <code>IDockerContainer.start()</code> can be called to start the container.
     * @return true if the docker container should be started automatically. false otherwise.
     */
    public boolean start() default true;

    /**
     * The <code>dockerEngineTag</code> will be used in the future so that a container can be run on a specific Docker Engine type.
     * You would not normally need to provide a Docker Engine tag.
     * @return The docker engine tag associate with this container.
     */
    public String dockerEngineTag() default "PRIMARY";

}