/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity;

import java.security.KeyStore;
import java.util.Map;

import dev.galasa.zossecurity.datatypes.RACFAccessType;
import dev.galasa.zossecurity.datatypes.RACFCertificateTrust;
import dev.galasa.zossecurity.datatypes.RACFCertificateType;
import dev.galasa.zos.IZosImage;

/**
 * The ZosSecurityManager provides access to the manage userids/profiles/classes
 * on RACF.
 * <p>
 * You can allocated/manage/free userids, CICS Class Sets, Profiles, Keyrings
 * and Certificates.
 * <p>
 * To gain access to the ZosSecurityManager include a field of type IZosSecurity
 * in your Galasa class.
 * 
 *  
 * 
 */
public interface IZosSecurity {

	/**
	 * Allocate a new userid on the run image. Will be clean with a password set, but
	 * no passphrase
	 * 
	 * @return A userid
	 * @throws ZosSecurityManagerException
	 */
	public IZosUserid allocateUserid() throws ZosSecurityManagerException;

	/**
	 * Get the primary run userid
	 * 
	 * @return
	 * @throws ZosSecurityManagerException
	 */
	public IZosUserid getRunUserid() throws ZosSecurityManagerException;

	/**
	 * Manually free an allocated userid. This will be automatically performed at
	 * the end of a run.
	 * 
	 * @param resource - The userid
	 * @throws ZosSecurityManagerException
	 */
	public void freeUserid(IZosUserid resource) throws ZosSecurityManagerException;

	/**
	 * Allocate a full CICS Security Class Set on the run image. Will be clean, ie no
	 * profiles defined
	 * 
	 * @return The allocated set.
	 * @throws ZosSecurityManagerException
	 */
	public IZosCicsClassSet allocateCicsClassSet() throws ZosSecurityManagerException;

	/**
	 * Free a CICS Security Class Set. This will be automatically performed at the
	 * end of a run.
	 * 
	 * @param classSet - The set to be freed
	 * @throws ZosSecurityManagerException
	 */
	public void freeCicsClassSet(IZosCicsClassSet classSet) throws ZosSecurityManagerException;

	/**
	 * Create a new profile on the run image.
	 * 
	 * @param className - The class to create the profile in
	 * @param name      - The name of the profiles
	 * @param uacc      - The uacc to assign, or null
	 * @return The profile
	 * @throws ZosSecurityManagerException
	 */
	public IZosProfile createProfile(String className, String name, RACFAccessType uacc) throws ZosSecurityManagerException;

	/**
	 * Create a new profile on the specified image/sysplex.
	 * @param className
	 * @param name    - The name of the profiles
	 * @param uacc    - The uacc to assign, or null
	 * @param refresh - issue SETROPTS REFRESH
	 * @return The profile
	 * @throws ZosSecurityManagerException
	 * @throws ZosSecurityManagerException
	 */
	public IZosProfile createProfile(String className, String name, RACFAccessType uacc, boolean refresh) throws ZosSecurityManagerException;

	/**
	 * Create a new profile on the specified image/sysplex.
	 * 
	 * @param image - The image/sysplex
	 * @param className
	 * @param name - The name of the profiles
	 * @param uacc - The uacc to assign, or null
	 * @return The profile
	 * @throws ZosSecurityManagerException
	 * @throws ZosSecurityManagerException
	 */
	public IZosProfile createProfile(String image, String className, String name, RACFAccessType uacc) throws ZosSecurityManagerException;

	/**
	 * 
	 * 
	 * @param className
	 * @param name
	 * @param args
	 * @param uacc
	 * @return
	 * @throws ZosSecurityManagerException
	 */
	public IZosProfile createProfile(String className, String name, Map<String, String> args, RACFAccessType uacc) throws ZosSecurityManagerException;

	/**
	 * Create a new profile on the specified image/sysplex.
	 * 
	 * @param image    - The image/sysplex
	 * @param className 
	 * @param name    - The name of the profiles
	 * @param uacc    - The uacc to assign, or null
	 * @param refresh - issue SETROPTS REFRESH
	 * @return The profile
	 * @throws ZosSecurityManagerException
	 * @throws ZosSecurityManagerException
	 */
	public IZosProfile createProfile(String image, String className, String name, RACFAccessType uacc, boolean refresh) throws ZosSecurityManagerException;

	/**
	 * Create a new profile on the specified image/sysplex.
	 * 
	 * @param image      - The image/sysplex
	 * @param className - The name of the class
	 * @param name      - The name of the profiles
	 * @param args      - Map of additional arguments which will be added as
	 *                  KEY(VALUE)
	 * @param uacc      - The uacc to assign, or null
	 * @param refresh   - issue SETROPTS REFRESH
	 * @return The profile
	 * @throws ZosSecurityManagerException
	 * @throws ZosSecurityManagerException
	 */
	public IZosProfile createProfile(String image, String className, String name, Map<String, String> args, RACFAccessType uacc, boolean refresh) throws ZosSecurityManagerException;

	/**
	 * Free the profile. This will be performed automatically at the end of the run.
	 * 
	 * @param profile - The profile to free
	 * @throws ZosSecurityManagerException
	 */
	public void freeProfile(IZosProfile profile) throws ZosSecurityManagerException;

	/**
	 * Delete the profile. Unless you specifically want the profile deleted, best
	 * let the Resource Manager to clean this resource, so you run will perform
	 * faster.
	 * 
	 * @param profile - The profile to be deleted
	 * @throws ZosSecurityManagerException
	 */
	public void deleteProfile(IZosProfile profile) throws ZosSecurityManagerException;

	/**
	 * Delete the profile. Unless you specifically want the profile deleted, best
	 * let the Resource Manager to clean this resource, so you run will perform
	 * faster.
	 * 
	 * @param profile - The profile to be deleted
	 * @param refresh - issue SETROPTS REFRESH
	 * @throws ZosSecurityManagerException
	 */
	public void deleteProfile(IZosProfile profile, boolean refresh) throws ZosSecurityManagerException;

	/**
	 * Create a new Keyring for a userid on the run image.
	 * 
	 * @param userid - The userid the keyring is to be attached to
	 * @param label  - The label to use
	 * @return The keyring
	 * @throws ZosSecurityManagerException
	 */
	public IZosKeyring createKeyring(IZosUserid userid, String label) throws ZosSecurityManagerException;

	/**
	 * Create a new Keyring for a userid on the run image.
	 * 
	 * @param userid - The userid the keyring is to be attached to
	 * @param label  - The label to use
	 * @return The keyring
	 * @throws ZosSecurityManagerException
	 */
	public IZosKeyring createKeyring(String userid, String label) throws ZosSecurityManagerException;

	/**
	 * Free the keyring. This will be performed automatically at the end of the run
	 * 
	 * @param keyring - The keyring to be freed
	 * @throws ZosSecurityManagerException
	 */
	public void freeKeyring(IZosKeyring keyring) throws ZosSecurityManagerException;

	/**
	 * Delete the keyring. Unless you specifically want the profile deleted, best
	 * let the Resource Manager to clean this resource, so you run will perform
	 * faster.
	 * 
	 * @param keyring
	 * @throws ZosSecurityManagerException
	 */
	public void deleteKeyring(IZosKeyring keyring) throws ZosSecurityManagerException;

	/**
	 * Import a certificate into RACF.
	 * <p>
	 * The keystore must contain only one certificate and password needs to be the
	 * same for the keystore and the private key.
	 * 
	 * @param userid   - The userid the certificate is to be attached to.
	 * @param label    - The label to use in RACF
	 * @param keyStore - The KeyStore, will be converted to PKCS12
	 * @param password - The password of the keystore AND the private key of the
	 *                 certificate
	 * @param type     - The certificate type, null will default to NONE
	 * @return THe Certificate
	 * @throws ZosSecurityManagerException
	 */
	public IZosCertificate createCertificate(IZosUserid userid, String label, KeyStore keyStore, String password, RACFCertificateType type) throws ZosSecurityManagerException;

	/**
	 * Import a certificate into RACF.
	 * <p>
	 * The keystore must contain only one certificate and password needs to be the
	 * same for the keystore and the private key.
	 * <p>
	 * NOTE: The "special" userid that runs the RACF commands MUST have read access
	 * to the test run's userid's datasets.
	 * 
	 * @param userid   - The userid the certificate is to be attached to.
	 * @param label    - The label to use in RACF
	 * @param keyStore - The KeyStore, will be converted to PKCS12
	 * @param password - The password of the keystore AND the private key of the
	 *                 certificate
	 * @param type     - The certificate type, null will default to NONE
	 * @return THe Certificate
	 * @throws ZosSecurityManagerException
	 */
	public IZosCertificate createCertificate(String userid, String label, KeyStore keyStore, String password, RACFCertificateType type) throws ZosSecurityManagerException;

	/**
	 * Import a certificate into RACF.
	 * <p>
	 * The keystore must contain only one certificate and password needs to be the
	 * same for the keystore and the private key.
	 * <p>
	 * NOTE: The "special" userid that runs the RACF commands MUST have read access
	 * to the test run's userid's datasets.
	 * 
	 * @param image     - The image/syspex
	 * @param userid   - The userid the certificate is to be attached to.
	 * @param label    - The label to use in RACF
	 * @param keyStore - The KeyStore, will be converted to PKCS12
	 * @param password - The password of the keystore AND the private key of the
	 *                 certificate
	 * @param type     - The certificate type, null will default to NONE
	 * @return THe Certificate
	 * @throws ZosSecurityManagerException
	 */
	public IZosCertificate createCertificate(String image, IZosUserid userid, String label, KeyStore keyStore, String password, RACFCertificateType type) throws ZosSecurityManagerException;

	/**
	 * Import a certificate into RACF.
	 * <p>
	 * The keystore must contain only one certificate and password needs to be the
	 * same for the keystore and the private key.
	 * <p>
	 * NOTE: The "special" userid that runs the RACF commands MUST have read access
	 * to the test run's userid's datasets.
	 * 
	 * @param image     - The image/syspex
	 * @param userid   - The userid the certificate is to be attached to.
	 * @param label    - The label to use in RACF
	 * @param keyStore - The KeyStore, will be converted to PKCS12
	 * @param password - The password of the keystore AND the private key of the
	 *                 certificate
	 * @param type     - The certificate type, null will default to NONE
	 * @return THe Certificate
	 * @throws ZosSecurityManagerException
	 */
	public IZosCertificate createCertificate(String image, String userid, String label, KeyStore keyStore, String password, RACFCertificateType type) throws ZosSecurityManagerException;

	/**
	 * Import a certificate into RACF.
	 * <p>
	 * The keystore must contain only one certificate and password needs to be the
	 * same for the keystore and the private key.
	 * <p>
	 * NOTE: The "special" userid that runs the RACF commands MUST have read access
	 * to the test run's userid's datasets.
	 * 
	 * @param userid   - The userid the certificate is to be attached to.
	 * @param label    - The label to use in RACF
	 * @param keyStore - The KeyStore, will be converted to PKCS12
	 * @param password - The password of the keystore AND the private key of the
	 *                 certificate
	 * @param type     - The certificate type, null will default to NONE
	 * @param trust    - The trust level of the certificate, null means the
	 *                 parameter is not supplied on the RACF command
	 * @return THe Certificate
	 * @throws ZosSecurityManagerException
	 */
	public IZosCertificate createCertificate(IZosUserid userid, String label, KeyStore keyStore, String password, RACFCertificateType type, RACFCertificateTrust trust) throws ZosSecurityManagerException;

	/**
	 * Import a certificate into RACF.
	 * <p>
	 * The keystore must contain only one certificate and password needs to be the
	 * same for the keystore and the private key.
	 * <p>
	 * NOTE: The "special" userid that runs the RACF commands MUST have read access
	 * to the test run's userid's datasets.
	 * 
	 * @param userid   - The userid the certificate is to be attached to.
	 * @param label    - The label to use in RACF
	 * @param keyStore - The KeyStore, will be converted to PKCS12
	 * @param password - The password of the keystore AND the private key of the
	 *                 certificate
	 * @param type     - The certificate type, null will default to NONE
	 * @param trust    - The trust level of the certificate, null means the
	 *                 parameter is not supplied on the RACF command
	 * @return THe Certificate
	 * @throws ZosSecurityManagerException
	 */
	public IZosCertificate createCertificate(String userid, String label, KeyStore keyStore, String password, RACFCertificateType type, RACFCertificateTrust trust) throws ZosSecurityManagerException;

	/**
	 * Import a certificate into RACF.
	 * <p>
	 * The keystore must contain only one certificate and password needs to be the
	 * same for the keystore and the private key.
	 * <p>
	 * NOTE: The "special" userid that runs the RACF commands MUST have read access
	 * to the test run's userid's datasets.
	 * 
	 * @param image     - The image/syspex
	 * @param userid   - The userid the certificate is to be attached to.
	 * @param label    - The label to use in RACF
	 * @param keyStore - The KeyStore, will be converted to PKCS12
	 * @param password - The password of the keystore AND the private key of the
	 *                 certificate
	 * @param type     - The certificate type, null will default to NONE
	 * @param trust    - The trust level of the certificate, null means the
	 *                 parameter is not supplied on the RACF command
	 * @return THe Certificate
	 * @throws ZosSecurityManagerException
	 */
	public IZosCertificate createCertificate(String image, IZosUserid userid, String label, KeyStore keyStore, String password, RACFCertificateType type, RACFCertificateTrust trust) throws ZosSecurityManagerException;

	/**
	 * Import a certificate into RACF.
	 * <p>
	 * The keystore must contain only one certificate and password needs to be the
	 * same for the keystore and the private key.
	 * <p>
	 * NOTE: The "special" userid that runs the RACF commands MUST have read access
	 * to the test run's userid's datasets.
	 * 
	 * @param image     - The image/syspex
	 * @param userid   - The userid the certificate is to be attached to.
	 * @param label    - The label to use in RACF
	 * @param keyStore - The KeyStore, will be converted to PKCS12
	 * @param password - The password of the keystore AND the private key of the
	 *                 certificate
	 * @param type     - The certificate type, null will default to NONE
	 * @param trust    - The trust level of the certificate, null means the
	 *                 parameter is not supplied on the RACF command
	 * @return THe Certificate
	 * @throws ZosSecurityManagerException
	 */
	public IZosCertificate createCertificate(String image, String userid, String label, KeyStore keyStore, String password, RACFCertificateType type, RACFCertificateTrust trust) throws ZosSecurityManagerException;

	/**
	 * Free this certificate. This will be performed automatically at the end of the
	 * run
	 * 
	 * @param certificate The certificate to free
	 * @throws ZosSecurityManagerException
	 */
	public void freeCertificate(IZosCertificate certificate) throws ZosSecurityManagerException;

	/**
	 * Delete the certificate. Unless you specifically want the profile deleted,
	 * best let the Resource Manager to clean this resource, so you run will perform
	 * faster.
	 * 
	 * @param certificate The certificate to delete
	 * @throws ZosSecurityManagerException
	 */
	public void deleteCertificate(IZosCertificate certificate) throws ZosSecurityManagerException;

	/**
	 * Generate a new Self-Signed Certificate with private/public key and return it
	 * in a PKCS12 keystore for use in RACF.
	 * 
	 * @param alias             - The alias to use
	 * @param distinguishedName - The full Distinguished Name
	 * @param keySize           - The size of the key to use
	 * @param durationDays      - The duration in Days
	 * @param keyAlgorithm      - The algoritm to use for the key, can be null,
	 *                          defaults to RSA
	 * @param signatureAlgoritm - The algoritm to use for the signature, can be
	 *                          null, defaults to SHA1withRSA
	 * @return A new keystore with the certificate in.
	 * @throws ZosSecurityManagerException
	 */
	public KeyStore generateSelfSignedCertificate(String alias, String distinguishedName, int keySize, int durationDays, String keyAlgorithm, String signatureAlgoritm) throws ZosSecurityManagerException;

	/**
	 * Generate a new Self-Signed Certificate with private/public key and return it
	 * in a PKCS12 keystore for use in RACF.
	 * 
	 * @param alias                - The alias to use
	 * @param distinguishedName    - The full Distinguished Name
	 * @param keySize              - The size of the key to use
	 * @param durationDays         - The duration in Days
	 * @param keyAlgorithm         - The algoritm to use for the key, can be null,
	 *                             defaults to RSA
	 * @param signatureAlgoritm    - The algoritm to use for the signature, can be
	 *                             null, defaults to SHA1withRSA
	 * @param certificateAuthority - The certificate is to be a certificate
	 *                             authority
	 * @return A new keystore with the certificate in.
	 * @throws ZosSecurityManagerException
	 */
	public KeyStore generateSelfSignedCertificate(String alias, String distinguishedName, int keySize, int durationDays, String keyAlgorithm, String signatureAlgoritm, boolean certificateAuthority)
			throws ZosSecurityManagerException;

	/**
	 * Generate a new Self-Signed Certificate with private/public key and return it
	 * in a PKCS12 keystore for use in RACF.
	 * 
	 * @param alias             - The alias to use
	 * @param distinguishedName - The full Distinguished Name
	 * @param keySize           - The size of the key to use
	 * @param durationDays      - The duration in Days
	 * @param signingKeyStore   -The keystore containing the signing certificate
	 * @param signingLabel      - The label of the signing certificate
	 * @param signingPassword   - The signing keystore password
	 * @return A new keystore with the certificate in.
	 * @throws ZosSecurityManagerException
	 */
	public KeyStore generateSignedCertificate(String alias, String distinguishedName, int keySize, int durationDays, KeyStore signingKeyStore, String signingLabel, String signingPassword) throws ZosSecurityManagerException;

	/**
	 * Generate a new Self-Signed Certificate with private/public key and return it
	 * in a PKCS12 keystore for use in RACF.
	 * 
	 * @param alias                - The alias to use
	 * @param distinguishedName    - The full Distinguished Name
	 * @param keySize              - The size of the key to use
	 * @param durationDays         - The duration in Days
	 * @param signingKeyStore      -The keystore containing the signing certificate
	 * @param signingLabel         - The label of the signing certificate
	 * @param signingPassword      - The signing keystore password
	 * @param certificateAuthority - The certificate is to be a certificate
	 *                             authority
	 * @return A new keystore with the certificate in.
	 * @throws ZosSecurityManagerException
	 */
	public KeyStore generateSignedCertificate(String alias, String distinguishedName, int keySize, int durationDays, KeyStore signingKeyStore, String signingLabel, String signingPassword, boolean certificateAuthority) throws ZosSecurityManagerException;

	/**
	 * Create a new Id Map for a userid.
	 * 
	 * @param userid        - The userid the id map is to be attached to
	 * @param label         - The label to use
	 * @param distributedID - The distributed id to set
	 * @param registry      - the registry to set
	 * @return The keyring
	 * @throws ZosSecurityManagerException
	 */
	public IZosIdMap createIdMap(String userid, String label, String distributedID, String registry) throws ZosSecurityManagerException;

	/**
	 * Create a new Id Map for a userid.
	 * 
	 * @param userid        - The userid the id map is to be attached to
	 * @param label         - The label to use
	 * @param distributedID - The distributed id to set
	 * @param registry      - the registry to set
	 * @return The keyring
	 * @throws ZosSecurityManagerException
	 */
	public IZosIdMap createIdMap(IZosUserid userid, String label, String distributedID, String registry) throws ZosSecurityManagerException;

	/**
	 * Free the id map. This will be performed automatically at the end of the run
	 * 
	 * @param idmap - The idmap to be freed
	 * @throws ZosSecurityManagerException
	 */
	public void freeIdMap(IZosIdMap idmap) throws ZosSecurityManagerException;

	/**
	 * Delete the idmap. Unless you specifically want the id map deleted, best let
	 * the Resource Manager to clean this resource, so your run will perform faster.
	 * 
	 * @param idmap
	 * @throws ZosSecurityManagerException
	 */
	public void deleteIdMap(IZosIdMap idmap) throws ZosSecurityManagerException;

	/**
	 * Create a Kerberos client principal. This will create the kerbname and the
	 * required association with the passed in service principal (see
	 * {@link #createKerberosPrincipal(IZosUserid, String)} fr the passed
	 * userid.
	 * 
	 * @param servicePrincipal - service principal with which to associate this
	 *                         client
	 * @param clientUserid     - zOS Userid for this principal
	 * @return
	 * @throws ZosSecurityManagerException
	 */
	public IZosKerberosPrincipal createKerberosClientPrincipal(IZosKerberosPrincipal servicePrincipal, IZosUserid clientUserid) throws ZosSecurityManagerException;

	/**
	 * Create a Kerberos principal, generally to be used as a service principal in
	 * {@link #createKerberosClientPrincipal(IZosKerberosPrincipal, IZosUserid)}
	 * 
	 * @param serviceUserid - zOS Userid for this principal
	 * @param realm         - realm to use, see
	 *                      {@link #getDefaultKerberosRealm()}
	 * 
	 * @return
	 * @throws ZosSecurityManagerException
	 */
	public IZosKerberosPrincipal createKerberosPrincipal(IZosUserid serviceUserid, String realm) throws ZosSecurityManagerException;

	/**
	 * Free a Kerberos Principal created for this test
	 * 
	 * @param principal
	 * @throws ZosSecurityManagerException
	 */
	public void freePrincipal(IZosKerberosPrincipal principal) throws ZosSecurityManagerException;

	/**
	 * Get the default Kerberos realm for an image
	 * 
	 * @return
	 * @throws ZosSecurityManagerException
	 */
	public String getDefaultKerberosRealm() throws ZosSecurityManagerException;

	/**
	 * Get the default KDC for an image
	 * @return
	 * @throws ZosSecurityManagerException
	 */
	public String getDefaultKerberosDomainController() throws ZosSecurityManagerException;

	/**
	 * Retrieve a kerberos token from a Kerberos Domain Controller on the host or ip
	 * address passed in as kdc, for the clientPrincipal and serverPrincipal passed
	 * 
	 * @param servicePrincipal
	 * @param clientPrincipal
	 * @param kdc
	 * @return
	 * @throws ZosSecurityManagerException
	 */
	public KerberosToken retrieveKerberosToken(IZosKerberosPrincipal servicePrincipal, IZosKerberosPrincipal clientPrincipal, String kdc) throws ZosSecurityManagerException;

	/**
	 * 
	 * 
	 * @param servicePrincipal
	 * @param clientPrincipal
	 * @param kdc
	 * @return
	 */
	public KerberosInitiator createKerberosInitiator(IZosKerberosPrincipal servicePrincipal, IZosKerberosPrincipal clientPrincipal, String kdc);

	/**
	 * Set the run userid
	 * 
	 * @param user
	 * @throws ZosSecurityManagerException 
	 */
	public void setRunUserid(IZosUserid user) throws ZosSecurityManagerException;

	/**
	 * Reset the run user to default
	 */
	public void resetRunUserid();

	public void setResourceReporting(boolean enabled);

	public void setOutputReporting(boolean enabled);

	public IZosImage getZosImage() throws ZosSecurityManagerException;

}
