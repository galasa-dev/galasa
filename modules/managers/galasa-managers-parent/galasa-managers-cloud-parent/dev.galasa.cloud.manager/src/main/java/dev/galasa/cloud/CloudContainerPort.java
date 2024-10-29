/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cloud;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Cloud Container - Port
 * 
 * A Port to be exposed from the Cloud Container.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CloudContainerPort {

    /**
     * A name given to the port,  not used except for getContainerExposedPort()
     */
    public String name() default "";
    
    /**
     * The port number
     */
    public int    port();
    
    /**
     * The type of port, http, tcp, udp
     */
    public String type() default "http";

}
