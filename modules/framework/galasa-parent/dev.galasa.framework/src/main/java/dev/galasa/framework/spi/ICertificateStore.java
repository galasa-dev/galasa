/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

/**
 * <p>
 * Used by the Galasa Framework to initialise the various the certificate store
 * if a non FPF certificate store is defined.
 * </p>
 * 
 * <p>
 * The Certificate Store should request the URI from the bootstrap to allow for 
 * any certificates to be loaded before the initialisation of anything else.
 * </p>
 * 
 *  
 *
 */
public interface ICertificateStore {
	
	void shutdown() throws CertificateStoreException;

}
