/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cloud;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Cloud Container - Environment Properties
 * 
 * If any environment properties are required to be passed to the container at startup, they can be specified via here.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CloudContainerEnvProp {

    /**
     * The name of the environment property
     */
    public String name();
    
    /**
     * The value of the environment property
     */
    public String value();

}
