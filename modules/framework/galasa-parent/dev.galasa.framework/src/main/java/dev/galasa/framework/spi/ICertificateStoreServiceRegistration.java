/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import javax.validation.constraints.NotNull;

/**
 * The Certificate store service will only be initialised if there is a defined 
 * certificate store that is a non local file. Overrides are expected to be used 
 * for any local certificates if they are required for execution.
 * 
 *  
 *
 */
public interface ICertificateStoreServiceRegistration {
	/**
     * Registers the service with the framework, ensuring only one service is
     * operational at one time.
     * 
     * @param frameworkInitialisation
     * @throws ConfidentialTextException
     */
    void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws ConfidentialTextException;

}
