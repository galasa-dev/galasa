/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.io.InputStream;
import java.security.KeyStore;

/**
 * A class to represent a Java Keystore and allow for easy appending and generation
 * 
 *  
 *
 */
public interface IKeyStore {
	/**
	 * Returns the Java keystore
	 * 
	 * @return KeyStore
	 */
	public KeyStore getKeyStore();
	
	/**
	 * <p>
	 * Allows a Base64 encoded PEM cert to be appended to the Keystore
	 * </p>
	 * 
	 * @param cert
	 * @throws CertificateStoreException
	 */
	public void appendPemAsString(String aliasId, String cert) throws CertificateStoreException;
	
	/**
	 * <p>
	 * Allows a PEM cert file to be appended to the Keystore
	 * </p>
	 * @param aliasId
	 * @param certFile
	 * @throws CertificateStoreException
	 */
	public void appendPem(String aliasId, InputStream certFile) throws CertificateStoreException;
	
	/**
	 * <p>
	 * Allows a Base64 encoded DER cert to be appended to the Keystore
	 * </p>
	 * @param aliasId
	 * @param certFile
	 * @throws CertificateStoreException
	 */
	public void appendDer(String aliasId, InputStream certFile) throws CertificateStoreException;
	
	/**
	 * <p>
	 * Allows a certificate to be retrieved from the the certificate store based on its
	 * ID tag and added to this keystore.
	 * </p>
	 * 
	 * @param certificateId
	 * @throws CertificateStoreException
	 */
	public void appendCertficateById(String aliasId, String certificateId) throws CertificateStoreException;

}
