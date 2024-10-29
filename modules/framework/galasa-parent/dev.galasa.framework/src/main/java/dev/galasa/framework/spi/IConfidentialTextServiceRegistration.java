/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import javax.validation.constraints.NotNull;

/**
 * The confidential text services provides a manager with the ability to
 * registered passwords, usernames, keys and other confidnetial texts so that
 * they can be obscured inside logs and outputs.
 * 
 *  
 */
public interface IConfidentialTextServiceRegistration {

    /**
     * Registers the service with the framework, ensuring only one service is
     * operational at one time.
     * 
     * @param frameworkInitialisation
     * @throws ConfidentialTextException
     */
    void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws ConfidentialTextException;

}
