/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cloud;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Cloud Container
 * 
 * The Cloud Container provides the ability to integration test applications across one or more cloud platforms.   It is not intended to 
 * allow deep testing of a container, for that level of testing you should use a dedicated container manager like the Docker, Kubernetes or Podman(to be written) Managers.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CloudManagerField
public @interface CloudContainer {

    /**
     * The <code>cloudContainerTag</code> is used to identify the Cloud Container to other Managers or Shared Environments.  If a test is using multiple 
     * Cloud Containers, each separate Cloud Container must have a unique tag. If two Cloud Containers use the same tag, they will refer to the 
     * same Cloud Container.
     */
    public String cloudContainerTag() default "PRIMARY";

    /**
     * The <code>image</code> attribute provides the Image that is used to create the Cloud Container.  The image name must not 
     * include the Registry as this is provided in the CPS.   If using a public official image from DockerHub,  then the 
     * image name must be prefixed with <code>library/</code>, for example <code>library/httpd:latest</code>, the cloud Manager will
     * not default to the library namespace like the Docker commands do.
     */
    public String image();

    /**
     * The <code>start</code> attribute indicates whether the Cloud Container should be started automatically. If the 
     * test needs to perform some work before the container is started, then <code>start=false</code> should be used, after which 
     * <code>ICloudContainer.start()</code> can be called to start the container.
     */
    public boolean start() default true;
    
    /**
     * If the Cloud Container is to be automatically started, the startOrder dictates when they are started.
     * Cloud Containers with a startOrder=1 will start first,  then 2,  then 3 etc. 
     */
    public int startOrder() default 1;
    
    /**
     * exposedPorts indicate which ports are to be exposed by the provider.   Unlike the Docker Manager, the Cloud container 
     * cannot automatically detect which ports are exposed during the image build,  therefore ANY port 
     * that needs to be opened to the container will need to be declared here.
     */
    public CloudContainerPort[] exposedPorts() default{};
    
    /**
     * Defines the environment properties to provided to the container
     */
    public CloudContainerEnvProp[] environmentProperties() default{};
    
    
    /**
     * Provides any run arguments for the container, if required.
     */
    public String[] runArguments() default {};

}
