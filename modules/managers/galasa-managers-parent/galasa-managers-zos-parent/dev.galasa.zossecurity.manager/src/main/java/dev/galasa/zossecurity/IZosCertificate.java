/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity;

import dev.galasa.zossecurity.datatypes.RACFCertificateType;


/**
 * Represents a Certificate that has been imported into RACF
 * 
 *  
 *
 */
public interface IZosCertificate {

	/**
	 * Retrieves the userid the certificate was attached to
	 * 
	 * @return The userid
	 */
	public String getUserid();
	
	/**
	 * Retrieves the certificate label used in RACF
	 * @return The label
	 */
	public String getLabel();
		
	/**
	 * Delete the certificate from RACF
	 * 
	 * @throws ZosSecurityManagerException
	 */
	public void delete() throws ZosSecurityManagerException;
	
	/**
	 * Connect this certificate to a RACF keyring
	 * 
	 * @param keyring
	 * @throws ZosSecurityManagerException
	 */
	public void connectKeyring(IZosKeyring keyring) throws ZosSecurityManagerException;
	
	/**
	 * Connect this certificate to a RACF keyring
	 * 
	 * @param keyring
	 * @param defaultCertificate - Mark this certificate as the default in the keyring
	 * @throws ZosSecurityManagerException
	 */
	public void connectKeyring(IZosKeyring keyring, boolean defaultCertificate) throws ZosSecurityManagerException;
	
	/**
	 * Connect this certificate to a RACF keyring
	 * 
	 * @param keyring
	 * @param defaultCertificate - Mark this certificate as the default in the keyring
	 * @param usage - The usage to set when connecting
	 * @throws ZosSecurityManagerException
	 */
	public void connectKeyring(IZosKeyring keyring, boolean defaultCertificate, RACFCertificateType usage) throws ZosSecurityManagerException;
	
	/**
	 * Remove this certificate from a RACF keyring
	 * 
	 * @param keyring
	 * @throws ZosSecurityManagerException
	 */
	public void removeKeyring(IZosKeyring keyring) throws ZosSecurityManagerException;
	
	/**
	 * Retrieves the certificate type
	 * @return
	 */
	public RACFCertificateType getType();
	
	/**
	 * List the certificate
	 * @return 
	 * @throws ZosSecurityManagerException
	 */
	public String list() throws ZosSecurityManagerException;
	
	/**
	 * Free the certificate. This will be performed automatically at the end of the run
	 *
	 * @throws ZosSecurityManagerException
	 */
	public void free() throws ZosSecurityManagerException;
	
}
