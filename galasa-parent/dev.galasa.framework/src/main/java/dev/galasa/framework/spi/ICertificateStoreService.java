/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.security.cert.X509Certificate;

import javax.validation.constraints.NotNull;

public interface ICertificateStoreService {
	
	/**
	 * <p>
	 * Retrieves the PEM certificate as a string.
	 * </p>
	 * 
	 * @param id
	 * @return String certificate
	 * @throws CertificateStoreException if no certificate found
	 */
	public X509Certificate getX509Certificate(@NotNull String id) throws CertificateStoreException;
	
	/**
	 * <p>
	 * Returns the default keystore. This will be populated by any certificates defined:
	 * certificates.default.ids=XXXX,YYYY,etc
	 * certificates.x509.XXXX.pem=...
	 * certificates.x509.YYYY.der=...
	 * </p>
	 * @return
	 * @throws CertificateStoreException
	 */
	public IKeyStore getDefaultKeystore() throws CertificateStoreException;
	
	/**
	 * Returns a defined group of certificates inside a keystore. 
	 * 
	 * This will be populated by any certificates defined:
	 * <pre>
	   certificates.&lt;GroupId&gt;.ids=XXXX,YYYY,etc
	   certificates.x509.XXXX.pem=...
	   certificates.x509.YYYY.der=...
	   </pre>
	 * 
	 * @param id
	 * @return
	 * @throws CertificateStoreException
	 */
	public IKeyStore getKeystore(@NotNull String id) throws CertificateStoreException;
	
	/**
	 * <p>
	 * Generate a Keystore populated with the list of certificates from the certificate 
	 * store using thier property ID
	 * </p>
	 * 
	 * <p>
	 * If no id's are passed then an empty Keystore is generated and passed back, with 
	 * the assumption the tester has the desired certificates in a test resource.
	 * </p>
	 * @param certificateId
	 * @return
	 * @throws CertificateStoreException
	 */
	public IKeyStore getKeyStore(@NotNull String... certificateId) throws CertificateStoreException;
	
	/**
	 * <p>
	 * Generate a Keystore populated with the list of certificates from the certificate 
	 * store using thier property ID
	 * </p>
	 * 
	 * <p>
	 * If no id's are passed then an empty Keystore is generated and passed back, with 
	 * the assumption the tester has the desired certificates in a test resource.
	 * </p>
	 * @param certificateId
	 * @return
	 * @throws CertificateStoreException
	 */
	public IKeyStore getDefaultKeyStoreWithExtraCertificates(@NotNull String... certificateId) throws CertificateStoreException;


}
