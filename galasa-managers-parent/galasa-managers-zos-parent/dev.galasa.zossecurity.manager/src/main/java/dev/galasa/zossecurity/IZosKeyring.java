/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity;

import dev.galasa.zossecurity.datatypes.RACFCertificateType;


/**
 * Represents a keyring that has been created.
 * 
 *  
 *
 */
public interface IZosKeyring {

	/**
	 * Retrieve the userid this keyring has been attached to
	 * 
	 * @return The userid
	 */
	public String getUserid();
	/**
	 * Retieve the label of this keyring
	 * 
	 * @return The Label
	 */
	public String getLabel();
		
	/**
	 * Delete this keyring
	 * 
	 * @throws ZosSecurityManagerException
	 */
	public void delete() throws ZosSecurityManagerException;
	
	/**
	 * Connect this keyring to a certificate
	 * 
	 * @param certificate - The certificate to connect to 
	 * @param defaultCertificate - Mark this certificate as the default in the keyring
	 * @param usage - The USAGE to use when connecting
	 * @throws ZosSecurityManagerException
	 */
	public void connectCertificate(IZosCertificate certificate, boolean defaultCertificate, RACFCertificateType usage) throws ZosSecurityManagerException;
	
	/**
	 * Connect this keyring to a certificate
	 * 
	 * @param certificate - The certificate to connect to 
	 * @param defaultCertificate - Mark this certificate as the default in the keyring
	 * @throws ZosSecurityManagerException
	 */
	public void connectCertificate(IZosCertificate certificate, boolean defaultCertificate) throws ZosSecurityManagerException;
	
	/**
	 * Connect this keyring to a certificate which outside the control of Galasa
	 * 
	 * @param userid - The userid that owns the certificate
	 * @param label  - The label of the certificate
	 * @param defaultCertificate - Mark this certificate as the default in the keyring
	 * @throws ZosSecurityManagerException
	 */
	public void connectCertificate(String userid, String label, boolean defaultCertificate) throws ZosSecurityManagerException;
	
	/**
	 * Connect this keyring to a certificate
	 * 
	 * @param certificate - The certificate to connect to 
	 * @throws ZosSecurityManagerException
	 */
	public void connectCertificate(IZosCertificate certificate) throws ZosSecurityManagerException;
	
	/**
	 * Connect this keyring to a certificate which outside the control of Galasa
	 * 
	 * @param userid - The userid that owns the certificate
	 * @param label  - The label of the certificate
	 * @throws ZosSecurityManagerException
	 */
	public void connectCertificate(String userid, String label) throws ZosSecurityManagerException;
	
	/**
	 * Remove a certificate from this keyring
	 * 
	 * @param certificate - The certificate to remove 
	 * @throws ZosSecurityManagerException
	 */
	public void removeCertificate(IZosCertificate certificate) throws ZosSecurityManagerException;
	
	/**
	 * Remove a non Galasa certificate from this keyring
	 * 
	 * @param userid - The userid that owns the certificate
	 * @param label  - The label of the certificate
	 * @throws ZosSecurityManagerException
	 */
	public void removeCertificate(String userid, String label) throws ZosSecurityManagerException;
	
	/**
	 * List the keyring
	 * @return 
	 * @throws ZosSecurityManagerException
	 */
	public String list() throws ZosSecurityManagerException;
	
	/**
	 * Free the keyring. This will be performed automatically at the end of the run
	 *
	 * @throws ZosSecurityManagerException
	 */
	public void free() throws ZosSecurityManagerException;
	
}
