/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.net.URI;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.PrincipalUtil;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreKeyAccess;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.spi.IZosFileSpi;
import dev.galasa.zossecurity.IZosCertificate;
import dev.galasa.zossecurity.IZosCicsClassSet;
import dev.galasa.zossecurity.IZosIdMap;
import dev.galasa.zossecurity.IZosKerberosPrincipal;
import dev.galasa.zossecurity.IZosKeyring;
import dev.galasa.zossecurity.IZosPreDefinedProfile;
import dev.galasa.zossecurity.IZosProfile;
import dev.galasa.zossecurity.IZosSecurity;
import dev.galasa.zossecurity.IZosUserid;
import dev.galasa.zossecurity.KerberosInitiator;
import dev.galasa.zossecurity.KerberosToken;
import dev.galasa.zossecurity.ZosSecurityManagerException;
import dev.galasa.zossecurity.datatypes.RACFAccessType;
import dev.galasa.zossecurity.datatypes.RACFCertificateTrust;
import dev.galasa.zossecurity.datatypes.RACFCertificateType;
import dev.galasa.zossecurity.internal.properties.CicsSharedClassets;
import dev.galasa.zossecurity.internal.properties.CreateUserid;
import dev.galasa.zossecurity.internal.properties.KerberosDomainController;
import dev.galasa.zossecurity.internal.properties.KerberosRealm;
import dev.galasa.zossecurity.internal.properties.OutputReporting;
import dev.galasa.zossecurity.internal.properties.PredefinedProfiles;
import dev.galasa.zossecurity.internal.properties.ResourceReporting;
import dev.galasa.zossecurity.internal.properties.ServerApikey;
import dev.galasa.zossecurity.internal.properties.ServerUrl;
import dev.galasa.zossecurity.internal.properties.SetroptsDelay;
import dev.galasa.zossecurity.internal.properties.UseridDefaultGroup;
import dev.galasa.zossecurity.internal.properties.UseridDefaultGroups;
import dev.galasa.zossecurity.internal.properties.UseridPool;
import dev.galasa.zossecurity.internal.resources.RacfOutputProcessing;
import dev.galasa.zossecurity.internal.resources.ZosCertificateImpl;
import dev.galasa.zossecurity.internal.resources.ZosCicsClassSetImpl;
import dev.galasa.zossecurity.internal.resources.ZosCicsSharedClassSetImpl;
import dev.galasa.zossecurity.internal.resources.ZosIdMapImpl;
import dev.galasa.zossecurity.internal.resources.ZosKerberosClientPrincipalImpl;
import dev.galasa.zossecurity.internal.resources.ZosKerberosPrincipalImpl;
import dev.galasa.zossecurity.internal.resources.ZosKeyringImpl;
import dev.galasa.zossecurity.internal.resources.ZosPreDefinedProfileImpl;
import dev.galasa.zossecurity.internal.resources.ZosProfileImpl;
import dev.galasa.zossecurity.internal.resources.ZosUseridImpl;

public class ZosSecurityImpl implements IZosSecurity {

	private static final Log logger = LogFactory.getLog(ZosSecurityImpl.class);

	public enum HttpMethod {
		GET,
		PUT,
		POST,
		DELETE
	}
	
	public enum ResourceType {
		ZOS_CERTIFICATE("zoscertificate"),
		ZOS_CICS_CLASS_SET("zoscicsclassset"),
		ZOS_ID_MAP("zosidmap"),
		ZOS_KERBEROS_PRINCIPAL("zoskerberosprincipal"),
		ZOS_KEYRING("zoskeyring"),
		ZOS_PRE_DEFINED_PROFILE_PERMIT("zospredefinedprofilepermit"),
		ZOS_PROFILE("zosprofile"),
		ZOS_USERID("zosuserid");

		private String name;

		ResourceType(String name) {
			this.name = name;
		}
		
		public String getName() {
			return this.name; 
		}
	}
	
	public final static Pattern ZOS_CERTIFICATE_PATTERN                = Pattern.compile("^" + ResourceType.ZOS_CERTIFICATE.getName()                + ".run\\.(\\w+)\\.(\\w+)\\/(\\w+)\\/([^\\.]+)\\.sysplex\\.(\\w+)$");
	public final static Pattern ZOS_CICS_CLASS_SET_PATTERN             = Pattern.compile("^" + ResourceType.ZOS_CICS_CLASS_SET.getName()             + ".run\\.(\\w+)\\.(\\w+)\\.sysplex\\.(\\w+)$");
	public final static Pattern ZOS_ID_MAP_PATTERN                     = Pattern.compile("^" + ResourceType.ZOS_ID_MAP.getName()                     + ".run\\.(\\w+)\\.(\\w+)\\/([\\S]+)\\.sysplex\\.(\\w+)$");
	public final static Pattern ZOS_KERBEROS_PRINCIPAL_PATTERN         = Pattern.compile("^" + ResourceType.ZOS_KERBEROS_PRINCIPAL.getName()         + ".run\\.(\\w+)\\.([\\S]+)\\.sysplex\\.(\\w+)$");
	public final static Pattern ZOS_KEYRING_PATTERN                    = Pattern.compile("^" + ResourceType.ZOS_KEYRING.getName()                    + ".run\\.(\\w+)\\.(\\w+)\\/([\\S]+)\\.sysplex\\.(\\w+)$");
	public final static Pattern ZOS_PRE_DEFINED_PROFILE_PERMIT_PATTERN = Pattern.compile("^" + ResourceType.ZOS_PRE_DEFINED_PROFILE_PERMIT.getName() + ".run\\.(\\w+)\\.(\\w+)\\/([\\S]+)\\/(\\w+)\\.sysplex\\.(\\w+)$");
	public final static Pattern ZOS_PROFILE_PATTERN                    = Pattern.compile("^" + ResourceType.ZOS_PROFILE.getName()                    + ".run\\.(\\w+)\\.(\\w+)\\/([\\S]+)\\.sysplex\\.(\\w+)$");
	public final static Pattern ZOS_USERID_PATTERN                     = Pattern.compile("^" + ResourceType.ZOS_USERID.getName()                     + ".run\\.(\\w+)\\.(\\w+)\\.sysplex\\.(\\w+)$");
	
	private IFramework framework;
	private final IDynamicStatusStoreService dss;
	private IZosManagerSpi zosManager;
	private IZosFileSpi zosFileManager;
	private IHttpManagerSpi httpManager;

	public final ArrayList<IZosCicsClassSet> preAllocatedCicsClassSets = new ArrayList<>();

	private final HashMap<String, IHttpClient> zossecServerClients = new HashMap<String, IHttpClient>();

	private final HashMap<String, HashSet<String>> classesRequiringRefresh = new HashMap<String, HashSet<String>>();

	private int certificateStoreNumber;
	private IZosUserid runUser;

	private IZosImage image;
	private IZosUserid imageUser;

	private boolean resourceReporting;
	private boolean outputReporting;
	private final Map<String, String> zosSecurityServerQueryParams = new HashMap<String, String>();

	private int setroptsDelay;
	private IZosFileHandler zosFileHandler;
	private String runDatasetHLQ;
	private List<String> useridPool;
	private List<String> cicsSharedClassSets;
	private boolean createUserid;
	private String useridDefaultGroup;
	private List<String> useridDefaultGroups;

	public ZosSecurityImpl(ZosSecurityManagerImpl zosSecurityManagerImpl, IZosImage image) throws ZosSecurityManagerException {
		this.framework = zosSecurityManagerImpl.getFramework();
		this.dss = zosSecurityManagerImpl.getDss();
		this.zosManager = zosSecurityManagerImpl.getZosManager();
		this.zosFileManager = zosSecurityManagerImpl.getZosFileManager();
		this.httpManager = zosSecurityManagerImpl.getHttpManager();
		this.image = image;
		try {
			this.resourceReporting = ResourceReporting.get(getZosImage().getSysplexID());
			this.outputReporting = OutputReporting.get(getZosImage().getSysplexID());
			this.setroptsDelay = SetroptsDelay.get();
			this.useridPool = UseridPool.get(image.getSysplexID());
			this.cicsSharedClassSets = CicsSharedClassets.get(image.getSysplexID());
			this.createUserid = CreateUserid.get();
		} catch(ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("Unable to obtain manager properties", e);
		}

		if (this.resourceReporting) {
			logger.info("Resource Reporting has been enabled by configuration properties");
		}
		if (this.outputReporting) {
			logger.info("Output Reporting has been enabled by configuration properties");
		}		
		try {
			ICredentialsUsernamePassword creds = (ICredentialsUsernamePassword) getZosImage().getDefaultCredentials();
			this.imageUser = new ZosUseridImpl(this, creds.getUsername(), creds.getPassword(), null, image);
		} catch (ZosManagerException e) {
			throw new ZosSecurityManagerException("Problem getting default credentials fo image " + image.getImageID(), e);
		}
	}

	public ZosSecurityImpl(IFramework framework, IDynamicStatusStoreService dss, IHttpManagerSpi httpManager) throws ZosSecurityManagerException {
		this.framework = framework;
		this.dss = dss;
		this.zosManager = null;
		this.zosFileManager = null;
		this.httpManager = httpManager;
		this.image = null;
		this.resourceReporting = true;
		this.outputReporting = true;
		try {
			this.setroptsDelay = SetroptsDelay.get();
		} catch(ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("Unable to obtain manager properties", e);
		}
	}

	public IZosUserid allocateUserid(boolean runUser) throws ZosSecurityManagerException {
		IZosUserid userid = allocateUserid();
		if (runUser) {
			setRunUserid(userid); 
		}
		
		return userid;
	}

	@Override
	public IZosUserid getRunUserid() throws ZosSecurityManagerException {
		if (this.runUser == null) {
			this.runUser = this.imageUser;
		}
		return this.runUser;
	}

	@Override
	public IZosUserid allocateUserid() throws ZosSecurityManagerException {
		IZosUserid zosUserid;
		try {
			zosUserid = ZosUseridImpl.allocateUserId(this);
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("Problem allocating zOS Userid for image " + getZosImage(), e);
		}
		if (zosUserid == null) {
			throw new ZosSecurityManagerException("There are no zOS Userids available in the pool for image " + getZosImage());
		}

		return zosUserid;
	}

	@Override
	public void freeUserid(IZosUserid userid) throws ZosSecurityManagerException {
		try {
			((ZosUseridImpl) userid).free();
		} catch (Exception e) {
			throw new ZosSecurityManagerException("Free of userid " + userid.getUserid() + "' failed", e);
		}
	}

	public IZosCicsClassSet allocateCicsClassSet(boolean allowAllAccess, boolean shared) throws ZosSecurityManagerException {
		IZosCicsClassSet zosClassset;
		if (shared) {
			zosClassset = allocateSharedCicsClassSet();
		} else {		
			zosClassset = allocateCicsClassSet();
		}
		if (allowAllAccess) {
			zosClassset.allowAllAccess();
		}
		return zosClassset;
	}

	@Override
	public IZosCicsClassSet allocateCicsClassSet() throws ZosSecurityManagerException {
		IZosCicsClassSet zosClassset;
		if (!preAllocatedCicsClassSets.isEmpty()) {
			zosClassset = preAllocatedCicsClassSets.remove(0);
		} else {
			zosClassset = (IZosCicsClassSet) ZosCicsClassSetImpl.allocateClassset(this);
			if (zosClassset == null) {
				throw new ZosSecurityManagerException("There are no zOS Classsets available in the pool for system '" + image + "'");
			}
		}

		return zosClassset;
	}

	private IZosCicsClassSet allocateSharedCicsClassSet() throws ZosSecurityManagerException {
		IZosCicsClassSet zosClassset = ZosCicsSharedClassSetImpl.allocateClassset(this, getZosImage());
		if (zosClassset == null) {
			throw new ZosSecurityManagerException("There are no zOS Shared Classsets available for system '" + image + "'");
		}

		return zosClassset;
	}

	@Override
	public void freeCicsClassSet(IZosCicsClassSet classSet) throws ZosSecurityManagerException {
		try {
			((ZosCicsClassSetImpl) classSet).free();
		} catch (ZosSecurityManagerException e) {
			throw e;
		} catch (Exception e) {
			throw new ZosSecurityManagerException("Unable to free CICS Class Set", e);
		}
	}

	protected IZosPreDefinedProfile createPredefinedProfile(String className, String profile) throws ZosSecurityManagerException {
		String name = className + "/" + profile;

		logger.info("Associating Pre Defined Profile '" + name + "'");

		List<String> validPredefinedProfiles;
		try {
			validPredefinedProfiles = PredefinedProfiles.get(getZosImage());
		} catch(ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("Unable to retrieve valid predefined profiles", e);
		}

		if (!validPredefinedProfiles.contains(name)) {
			throw new ZosSecurityManagerException("Requested predefined profile '" + name + "' is not authorised");
		}

		IZosImage image = null;


		try {
			image = getZosImage();
		} catch (Exception e) {
			throw new ZosSecurityManagerException("Unable to retrieve image for tag '" + image.getImageID() + "'", e);
		}

		return new ZosPreDefinedProfileImpl(this, image, className, profile);
	}

	@Override
	public IZosProfile createProfile(String className, String name, RACFAccessType uacc) throws ZosSecurityManagerException {
		return createProfile(getZosImage().getImageID(), className, name, null, uacc, true);
	}

	@Override
	public IZosProfile createProfile(String className, String name, RACFAccessType uacc, boolean refresh) throws ZosSecurityManagerException {
		return createProfile(getZosImage().getImageID(), className, name, null, uacc, refresh);
	}

	@Override
	public IZosProfile createProfile(String image, String className, String name, RACFAccessType uacc) throws ZosSecurityManagerException {
		return createProfile(image, className, name, null, uacc, true);
	}

	@Override
	public IZosProfile createProfile(String className, String name, Map<String, String> args, RACFAccessType uacc) throws ZosSecurityManagerException {
		return createProfile(getZosImage().getImageID(), className, name, args, uacc, true);
	}

	@Override
	public IZosProfile createProfile(String image, String className, String name, RACFAccessType uacc, boolean refresh) throws ZosSecurityManagerException {
		return createProfile(image, className, name, null, uacc, refresh);
	}

	@Override
	public IZosProfile createProfile(String image, String className, String name, Map<String, String> args, RACFAccessType uacc, boolean refresh) throws ZosSecurityManagerException {

		IZosImage zosimage = null;
		try {
			zosimage = getZosManager().getImage(image);
		} catch (ZosManagerException e) {
			throw new ZosSecurityManagerException("Unable to retrieve the run image", e);
		}
		IZosProfile newProfile = ZosProfileImpl.createProfile(this, zosimage, className, name, args, uacc, refresh);
		if (newProfile == null) {
			throw new ZosSecurityManagerException("Profile " + className + "/" + name + " is already in use by another run");
		}

		logger.debug("zOS Profile '" + newProfile.toString() + "' was allocated to this run");

		return (IZosProfile) newProfile;
	}

	@Override
	public void freeProfile(IZosProfile profile) throws ZosSecurityManagerException {
		try {
			((ZosProfileImpl) profile).free();
		} catch (ZosSecurityManagerException e) {
			throw e;
		} catch (Exception e) {
			throw new ZosSecurityManagerException("Unable to free profile", e);
		}
	}

	@Override
	public void deleteProfile(IZosProfile profile) throws ZosSecurityManagerException {
		profile.delete(true);
	}

	@Override
	public void deleteProfile(IZosProfile profile, boolean refresh) throws ZosSecurityManagerException {
		profile.delete(refresh);
	}

	@Override
	public IZosKeyring createKeyring(IZosUserid userid, String label) throws ZosSecurityManagerException {
		return createKeyring(userid.getUserid(), label);
	}

	@Override
	public IZosKeyring createKeyring(String userid, String label) throws ZosSecurityManagerException {
		IZosKeyring newKeyring = ZosKeyringImpl.createKeyring(this, getZosImage(), userid, label);
		if (newKeyring == null) {
			throw new ZosSecurityManagerException("Keyring " + userid + "/" + label + " is already in use by another run");
		}

		logger.debug("zOS keyring '" + newKeyring.toString() + "' was allocated to this run");

		return newKeyring;
	}

	@Override
	public void freeKeyring(IZosKeyring keyring) throws ZosSecurityManagerException {
		try {
			((ZosKeyringImpl) keyring).free();
		} catch (ZosSecurityManagerException e) {
			throw e;
		} catch (Exception e) {
			throw new ZosSecurityManagerException("Unable to free keyring", e);
		}
	}

	@Override
	public void deleteKeyring(IZosKeyring keyring) throws ZosSecurityManagerException {
		keyring.delete();
	}

	@Override
	public IZosCertificate createCertificate(IZosUserid userid, String label, KeyStore keyStore, String password,
			RACFCertificateType type) throws ZosSecurityManagerException {
		return createCertificate(userid.getUserid(), label, keyStore, password, type, null);
	}

	@Override
	public IZosCertificate createCertificate(String userid, String label, KeyStore keyStore, String password,
			RACFCertificateType type) throws ZosSecurityManagerException {
		return createCertificate(getZosImage().getImageID(), userid, label, keyStore,
				password, type, null);
	}

	@Override
	public IZosCertificate createCertificate(String image, IZosUserid userid, String label, KeyStore keyStore,
			String password, RACFCertificateType type) throws ZosSecurityManagerException {
		return createCertificate(image, userid.getUserid(), label, keyStore, password, type, null);
	}

	@Override
	public IZosCertificate createCertificate(String image, String userid, String label, KeyStore keyStore,
			String password, RACFCertificateType type) throws ZosSecurityManagerException {
		return createCertificate(image, userid, label, keyStore, password, type, null);
	}

	@Override
	public IZosCertificate createCertificate(IZosUserid userid, String label, KeyStore keyStore, String password,
			RACFCertificateType type, RACFCertificateTrust trust) throws ZosSecurityManagerException {
		return createCertificate(userid.getUserid(), label, keyStore, password, type, trust);
	}

	@Override
	public IZosCertificate createCertificate(String userid, String label, KeyStore keyStore, String password,
			RACFCertificateType type, RACFCertificateTrust trust) throws ZosSecurityManagerException {
		return createCertificate(getZosImage().getImageID(), userid, label, keyStore,
				password, type, trust);
	}

	@Override
	public IZosCertificate createCertificate(String image, IZosUserid userid, String label, KeyStore keyStore,
			String password, RACFCertificateType type, RACFCertificateTrust trust) throws ZosSecurityManagerException {
		return createCertificate(image, userid.getUserid(), label, keyStore, password, type, trust);
	}

	@Override
	public IZosCertificate createCertificate(String image, String userid, String label, KeyStore keyStore,
			String password, RACFCertificateType type, RACFCertificateTrust trust) throws ZosSecurityManagerException {

		IZosImage zosimage = null;
		try {
			zosimage = getZosManager().getImage(image);
		} catch (ZosManagerException e) {
			throw new ZosSecurityManagerException("Unable to retrieve the run image", e);
		}

		certificateStoreNumber++;
		IZosCertificate newCertificate = ZosCertificateImpl.createCertificate(this, zosimage, userid, label, keyStore, password, type, trust, certificateStoreNumber);
		if (newCertificate == null) {
			throw new ZosSecurityManagerException("Certificate " + userid + "/" + label + " is already in use by another run");
		}

		logger.debug("zOS Certificate '" + newCertificate.toString() + "' was allocated to this run");

		return newCertificate;

	}

	@Override
	public void freeCertificate(IZosCertificate certificate) throws ZosSecurityManagerException {
		try {
			((ZosCertificateImpl) certificate).free();
		} catch (ZosSecurityManagerException e) {
			throw e;
		} catch (Exception e) {
			throw new ZosSecurityManagerException("Unable to free certificate", e);
		}
	}

	@Override
	public void deleteCertificate(IZosCertificate certificate) throws ZosSecurityManagerException {
		certificate.delete();
	}

	@Override
	public KeyStore generateSelfSignedCertificate(String alias, String distinguishedName,
			int keySize, int durationDays, String keyAlgorithm, String signatureAlgoritm)
					throws ZosSecurityManagerException {
		return generateSelfSignedCertificate(alias, distinguishedName, keySize, durationDays,
				keyAlgorithm, signatureAlgoritm, false);
	}

	@Override
	public KeyStore generateSelfSignedCertificate(String alias, String distinguishedName,
			int keySize, int durationDays, String keyAlgorithm, String signatureAlgoritm,
			boolean certificateAuthority) throws ZosSecurityManagerException {
		try {
			if (keyAlgorithm == null) {
				keyAlgorithm = "RSA";
			}
			if (signatureAlgoritm == null) {
				signatureAlgoritm = "SHA1withRSA";
			}

			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyAlgorithm);
			keyPairGenerator.initialize(keySize, new SecureRandom());
			KeyPair keyPair = keyPairGenerator.generateKeyPair();

			GregorianCalendar now = new GregorianCalendar();
			GregorianCalendar expire = new GregorianCalendar();
			expire.add(GregorianCalendar.DAY_OF_YEAR, durationDays);

			JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder(signatureAlgoritm);
			ContentSigner contentSigner = contentSignerBuilder.build(keyPair.getPrivate());

			X500Name name = new X500Name(distinguishedName);

			X509v3CertificateBuilder builder =
					new JcaX509v3CertificateBuilder(name, BigInteger.valueOf(System.currentTimeMillis()),
							now.getTime(), expire.getTime(), name, keyPair.getPublic());

			builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(
					certificateAuthority));

			X509CertificateHolder holder = builder.build(contentSigner);

			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert =
					(X509Certificate) cf.generateCertificate(new ByteArrayInputStream(holder.getEncoded()));

			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(null, null);
			keyStore.setKeyEntry(alias, keyPair.getPrivate(), "password".toCharArray(),
					new java.security.cert.Certificate[] {cert});

			logger.info("Selfsigned certificate generated with dn='" + cert.getSubjectDN().getName()
					+ "' and serial '" + cert.getSerialNumber() + "'");

			return keyStore;
		} catch (Exception e) {
			throw new ZosSecurityManagerException("Unable to generate self signed certificate", e);
		}
	}


	@Override
	public KeyStore generateSignedCertificate(String alias, String distinguishedName, int keySize,
			int durationDays, KeyStore signingKeyStore, String signingLabel, String signingPassword)
					throws ZosSecurityManagerException {
		return generateSignedCertificate(alias, distinguishedName, keySize, durationDays,
				signingKeyStore, signingLabel, signingPassword, false);
	}

	@Override
	public KeyStore generateSignedCertificate(String alias, String distinguishedName, int keySize,
			int durationDays, KeyStore signingKeyStore, String signingLabel, String signingPassword,
			boolean certificateAuthority) throws ZosSecurityManagerException {
		try {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

			Key signingKey = signingKeyStore.getKey(signingLabel, signingPassword.toCharArray());
			Certificate[] signingCerts = signingKeyStore.getCertificateChain(signingLabel);
			X509Certificate signingCertificate =
					(X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(
							signingCerts[0].getEncoded()));

			String signatureAlgoritm = signingCertificate.getSigAlgName();

			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(signingKey.getAlgorithm());
			keyPairGenerator.initialize(keySize, new SecureRandom());
			KeyPair keyPair = keyPairGenerator.generateKeyPair();

			GregorianCalendar now = new GregorianCalendar();
			GregorianCalendar expire = new GregorianCalendar();
			expire.add(GregorianCalendar.DAY_OF_YEAR, durationDays);

			JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder(signatureAlgoritm);
			ContentSigner contentSigner = contentSignerBuilder.build((PrivateKey) signingKey);

			X500Name name = new X500Name(distinguishedName);
			@SuppressWarnings("deprecation")
			X500Name issuer = new X500Name(PrincipalUtil.getSubjectX509Principal(signingCertificate).getName());

			X509v3CertificateBuilder builder =
					new JcaX509v3CertificateBuilder(issuer, BigInteger.valueOf(System.currentTimeMillis()),
							now.getTime(), expire.getTime(), name, keyPair.getPublic());

			builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(
					certificateAuthority));

			X509CertificateHolder holder = builder.build(contentSigner);

			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert =
					(X509Certificate) cf.generateCertificate(new ByteArrayInputStream(holder.getEncoded()));

			Certificate[] chain = new Certificate[1 + signingCerts.length];
			chain[0] = cert;
			int i = 1;
			for (Certificate c : signingCerts) {
				chain[i] = c;
				i++;
			}

			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(null, null);
			keyStore.setKeyEntry(alias, keyPair.getPrivate(), "password".toCharArray(), chain);

			logger.info("Certificate generated with dn='" + cert.getSubjectDN().getName()
					+ "' and serial '" + cert.getSerialNumber() + "'");

			return keyStore;
		} catch (Exception e) {
			throw new ZosSecurityManagerException("Unable to generate signed certificate", e);
		}
	}

	@Override
	public IZosIdMap createIdMap(String userid, String label, String distributedID, String registry) throws ZosSecurityManagerException {
		IZosIdMap newIdMap = ZosIdMapImpl.createIdMap(this, getZosImage(), userid, label, distributedID, registry);
		if (newIdMap == null) {
			throw new ZosSecurityManagerException("IDMap " + userid + "/" + label + " is already in use by another run");
		}

		logger.debug("zOS id map '" + newIdMap.toString() + "' was allocated to this run");

		return newIdMap;
	}

	@Override
	public IZosIdMap createIdMap(IZosUserid userid, String label, String distributedID, String registry) throws ZosSecurityManagerException {
		return createIdMap(userid.getUserid(), label, distributedID, registry);
	}

	@Override
	public void freeIdMap(IZosIdMap idmap) throws ZosSecurityManagerException {
		try {
			((ZosIdMapImpl) idmap).free();
		} catch (ZosSecurityManagerException e) {
			throw e;
		} catch (Exception e) {
			throw new ZosSecurityManagerException("Unable to free idmap", e);
		}
	}

	@Override
	public void deleteIdMap(IZosIdMap idmap) throws ZosSecurityManagerException {
		idmap.delete();
	}

	@Override
	public IZosKerberosPrincipal createKerberosClientPrincipal(IZosKerberosPrincipal servicePrincipal, IZosUserid clientUserid) throws ZosSecurityManagerException {

		if (!image.getImageID().equals(servicePrincipal.getUserid().getZosImage().getImageID())) {
			throw new ZosSecurityManagerException("Service Principal image does not match provided image");
		}
		if (!image.getImageID().equals(clientUserid.getZosImage().getImageID())) {
			throw new ZosSecurityManagerException("Client Userid image does not match provided image");
		}

		IZosKerberosPrincipal principal = ZosKerberosClientPrincipalImpl.createPrincipal(this, servicePrincipal, clientUserid);
		if (principal == null) {
			throw new ZosSecurityManagerException("Kerberos Principal " + ZosKerberosClientPrincipalImpl.generatePrincipalName(clientUserid) + " is already in use by another run");
		}
		
		logger.debug("zOS Kerberos Client Principal '" + principal.toString() + "' was allocated to this run");

		return principal;
	}

	@Override
	public IZosKerberosPrincipal createKerberosPrincipal(IZosUserid serviceUserid, String realm) throws ZosSecurityManagerException {

		if (!image.getImageID().equals(serviceUserid.getZosImage().getImageID())) {
			throw new ZosSecurityManagerException("Userid image does not match provided image");
		}

		IZosKerberosPrincipal principal = ZosKerberosPrincipalImpl.createPrincipal(this, serviceUserid, realm);
		if (principal == null) {
			throw new ZosSecurityManagerException("Kerberos Principal " + ZosKerberosPrincipalImpl.generatePrincipalName(serviceUserid) + " is already in use by another run");
		}

		logger.debug("zOS Kerberos Principal '" + ((ZosKerberosPrincipalImpl) principal).getResourceName() + "' was allocated to this run");

		return principal;
	}

	@Override
	public void freePrincipal(IZosKerberosPrincipal principal) throws ZosSecurityManagerException {
		try {
			((ZosKerberosPrincipalImpl) principal).free();
		} catch (ZosSecurityManagerException e) {
			throw e;
		} catch (Exception e) {
			throw new ZosSecurityManagerException("Unable to free idmap", e);
		}
	}

	@Override
	public String getDefaultKerberosRealm() throws ZosSecurityManagerException {
		return KerberosRealm.get(image);
	}

	@Override
	public String getDefaultKerberosDomainController() throws ZosSecurityManagerException {
		return KerberosDomainController.get(image);
	}

	@Override
	public KerberosToken retrieveKerberosToken(IZosKerberosPrincipal servicePrincipal,
			IZosKerberosPrincipal clientPrincipal, String kdc) throws ZosSecurityManagerException {
		KerberosInitiator initiator = createKerberosInitiator(servicePrincipal, clientPrincipal, kdc);
		initiator.create();

		return initiator.initiate();
	}

	@Override
	public KerberosInitiator createKerberosInitiator(IZosKerberosPrincipal servicePrincipal,
			IZosKerberosPrincipal clientPrincipal, String kdc) {
		return new KerberosInitiator(servicePrincipal, clientPrincipal, kdc);
	}

	@Override
	public void setRunUserid(IZosUserid user) throws ZosSecurityManagerException {
		this.runUser = user;
	}

	@Override
	public void resetRunUserid() {
		this.runUser = this.imageUser;
	}


	@Override
	public IZosImage getZosImage() throws ZosSecurityManagerException {
		return image;
	}

	@Override
	public void setResourceReporting(boolean enabled) {
		this.resourceReporting = enabled;

		if (this.resourceReporting) {
			logger.info("Resource Reporting has been enabled");
		} else {
			logger.info("Resource Reporting has been disabled");
		}
	}

	@Override
	public void setOutputReporting(boolean enabled) {
		this.outputReporting = enabled;

		if (this.outputReporting) {
			logger.info("Output Reporting has been enabled");
		} else {
			logger.info("Output Reporting has been disabled");
		}
	}

	public boolean isResourceReporting() {
		return this.resourceReporting;
	}

	public boolean isOutputReporting() {
		return this.outputReporting;
	}

	private IFramework getFramework() {
		return this.framework;
	}

	private IZosManagerSpi getZosManager() {
		return this.zosManager;
	}

	private IZosFileSpi getZosFileManager() {
		return this.zosFileManager;
	}

	private IHttpManagerSpi getHttpManager() {
		return this.httpManager;
	}

	public IHttpClient getZossecServerClient(String sysplexId) throws ZosSecurityManagerException {
		IHttpClient httpClient = zossecServerClients.get(sysplexId);

		if (httpClient != null) {
			return httpClient;
		}

		try {
			httpClient = getHttpManager().newHttpClient();
			URI uri = new URI(ServerUrl.get(sysplexId));
			httpClient.setURI(uri);
			ICredentials creds = null;
			try {
				creds = getFramework().getCredentialsService().getCredentials("w3");
			} catch (CredentialsException e) {
				throw new ZosSecurityManagerException("Problem accessing credentials store", e);
            }
            
            if (creds != null && creds instanceof ICredentialsUsernamePassword) {
                httpClient.setAuthorisation(((ICredentialsUsernamePassword) creds).getUsername(), ((ICredentialsUsernamePassword) creds).getPassword());
            } else {
            	throw new ZosSecurityManagerException("Unable to get w3 credentials");
            }
			String apikey = ServerApikey.get();
			httpClient.addCommonHeader("ejat-zossec-apikey", apikey);
			httpClient.addOkResponseCode(500);
			httpClient.addOkResponseCode(415);
			if (uri.getScheme().equals("https")) {
				httpClient.setTrustingSSLContext();
			}

			zossecServerClients.put(sysplexId, httpClient);
		} catch(Exception e) {
			throw new ZosSecurityManagerException("Unable to create zossec server client for " + image.getSysplexID(), e);
		}

		return httpClient;
	}

	public void addClassToBeRefreshed(String sysplexId, String className) {
		synchronized (classesRequiringRefresh) {

			HashSet<String> sysplexClasses = classesRequiringRefresh.get(sysplexId);

			if (sysplexClasses == null) {
				sysplexClasses = new HashSet<String>();
				classesRequiringRefresh.put(sysplexId, sysplexClasses);
			}			

			sysplexClasses.add(className);
		}
	}

	public void refreshClasses(String sysplexId) throws ZosSecurityManagerException {
		HashSet<String> sysplexClasses = null;
		synchronized (classesRequiringRefresh) {
			sysplexClasses = classesRequiringRefresh.remove(sysplexId);
		}

		if (sysplexClasses == null || sysplexClasses.isEmpty()) {
			logger.debug("No classes required to be refreshed on " + image.getSysplexID() + ", ignoring");
		}

		logger.info("Requesting SETROPTS refresh of " + sysplexClasses);

		try {
			JsonObject jsonBody = new JsonObject();
			JsonArray jsonClasses = new JsonArray();
			for(String c : sysplexClasses) {
				jsonClasses.add(c);
			}
			jsonBody.add("classes", jsonClasses);

			long start = System.currentTimeMillis();
			JsonObject response = clientRequest(sysplexId, HttpMethod.PUT, "/api/refresh", zosSecurityServerQueryParams, jsonBody);
			long end = System.currentTimeMillis();
			RacfOutputProcessing.analyseOutput(response, RacfOutputProcessing.COMMAND.REFRESH, sysplexClasses.toString(), isOutputReporting());

			logger.debug("SETROPTS command took " + (end - start) + "ms to action, includes mandatory " + setroptsDelay + "ms wait");
		} catch (ZosSecurityManagerException e) {
			throw new ZosSecurityManagerException("REFRESH of " + sysplexClasses.toString() + " failed", e);
		}
	}

	public void dssRegister(String resourceType, String resourceName) throws ZosSecurityManagerException {
		try {
			String sysplexId = getZosImage().getSysplexID();
			String allocated = Instant.now().toString();
			String runName = getRunName();
			
			HashMap<String, String> props = new HashMap<>();
			props.put(resourceType + "." + resourceName + ".sysplex." + sysplexId + ".run", runName);
			props.put(resourceType + ".run." + runName + "." + resourceName +".sysplex." + sysplexId, "active");
			this.dss.put(props);
	
			props = new HashMap<>();
			props.put(resourceName + ".sysplex." + sysplexId + ".run", runName);
			props.put(resourceName + ".sysplex." + sysplexId + ".run." + runName + ".allocated", allocated);
			getDynamicResource(resourceType).put(props);
		} catch (DynamicStatusStoreException e) {
			throw new ZosSecurityManagerException("Problem setting slot for zOS " + resourceType + " " + resourceName, e);
		}
	}
		
	public void dssUnregister(String resourceType, String resourceName, String sysplexId, String runName) throws ZosSecurityManagerException {
		try {
			HashSet<String> props = new HashSet<>();
			props.add(resourceName + ".sysplex." + sysplexId + ".run");
			props.add(resourceName + ".sysplex." + sysplexId + ".run." + runName + ".allocated");
			getDynamicResource(resourceType).delete(props);
			
			props = new HashSet<>();
			props.add(resourceType + "." + resourceName + ".sysplex." + sysplexId + ".run");
			props.add(resourceType + ".run." + runName + "." + resourceName +".sysplex." + sysplexId);
			this.dss.delete(props);
		} catch (DynamicStatusStoreException e) {
			throw new ZosSecurityManagerException("Problem removing slot for zOS " + resourceType + " " + resourceName, e);
		}
	}

	public void dssFree(String resourceType, String resourceName) throws ZosSecurityManagerException {
		try {
			String sysplexId = getZosImage().getSysplexID();
			String runName = getRunName();
			this.dss.put(resourceType + ".run." + runName + "." + resourceName +".sysplex." + sysplexId, "free");
		} catch (DynamicStatusStoreException e) {
			throw new ZosSecurityManagerException("Problem updating slot for zOS " + resourceType + " " + resourceName, e);
		}
	}

	private IDynamicStatusStoreKeyAccess getDynamicResource(String resourceType) {
		return this.dss.getDynamicResource(resourceType);
	}

	public String getRunName() {
		return this.framework.getTestRunName();
	}	
	
	public JsonObject clientRequest(String sysplexId, HttpMethod method, String path, Map<String, String> queryParams, JsonObject body) throws ZosSecurityManagerException {
		if (body == null) {
			body = new JsonObject();
		}
		IHttpClient client = getZossecServerClient(sysplexId);
		HttpClientResponse<JsonObject> response;
		try {
			switch (method) {
			case GET:
				response = client.getJson(buildUri(path, queryParams));
				break;
			case PUT:
				response = client.putJson(buildUri(path, queryParams), body);
				break;
			case POST:
				response = client.postJson(buildUri(path, queryParams), body);
				break;
			case DELETE:
				response = client.deleteJson(buildUri(path, queryParams));
				break;
			default:
				throw new ZosSecurityManagerException("Invalid HTTP method \"" + method + "\"");
			}
		} catch (HttpClientException e) {
			throw new ZosSecurityManagerException("Server request failed", e);
		}
		return response.getContent();
	}

	private String buildUri(String path, Map<String, String> queryParams) {
		if (queryParams.isEmpty()) {
			return path;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(path);
		sb.append("?");
		for (Entry<String, String> entry: queryParams.entrySet()) {
			if (!sb.toString().endsWith("?")) {
				sb.append("&");
			}
			sb.append(entry.getKey());
			sb.append("=");
			sb.append(entry.getValue());
		}
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return "[zOS Security] " + this.image;
	}

	public IZosFileHandler getZosFileHandler() throws ZosSecurityManagerException {
		if (this.zosFileHandler == null) {
			try {
				this.zosFileHandler = getZosFileManager().getZosFileHandler();
			} catch (ZosFileManagerException e) {
				throw new ZosSecurityManagerException("Unable to get zOS File Handler", e); 
			}
		}
		return this.zosFileHandler;
			
	}

	public String getRunDatasetHLQ(IZosImage image) throws ZosSecurityManagerException {
		if (this.runDatasetHLQ == null) {
			try {
				this.runDatasetHLQ = getZosManager().getRunDatasetHLQ(image);
			} catch (ZosManagerException e) {
				throw new ZosSecurityManagerException("Unable to get Run Dataset HLQ", e);
			}
		}
		return this.runDatasetHLQ;
	}

	public List<String> getUseridPool() {
		return this.useridPool;
	}

	public String getUseridFromPool(boolean createUserid) throws ZosSecurityManagerException {
		String sysplexId = getZosImage().getSysplexID();
		String resourceType = ResourceType.ZOS_USERID.getName();
		for (String userName: this.useridPool) {
			
			Map<String, String> allocatedUserids;
			try {
				allocatedUserids = this.dss.getPrefix(resourceType + "." + userName + ".sysplex." + sysplexId + ".run");
			} catch (DynamicStatusStoreException e) {
				throw new ZosSecurityManagerException("Problem getting userid from pool for image " + getZosImage(), e);
			}
			if (allocatedUserids.isEmpty()) {
				dssRegister(resourceType, userName);
				return userName;
			}
		}
		throw new ZosSecurityManagerException("No Userids available in pool for image " + getZosImage());
	}
	
	public String getCicsClassSetFromPool() throws ZosSecurityManagerException {
		String sysplexId = getZosImage().getSysplexID();
		String resourceType = ResourceType.ZOS_CICS_CLASS_SET.getName();
		for (String ClassSetName: this.cicsSharedClassSets) {
			
			Map<String, String> allocatedClassSets;
			try {
				allocatedClassSets = this.dss.getPrefix(resourceType + "." + ClassSetName + ".sysplex." + sysplexId + ".run");
			} catch (DynamicStatusStoreException e) {
				throw new ZosSecurityManagerException("Problem getting CICS Class Set from pool for image " + getZosImage(), e);
			}
			if (allocatedClassSets.isEmpty()) {
				dssRegister(resourceType, ClassSetName);
				return ClassSetName;
			}
		}
		throw new ZosSecurityManagerException("No CICS Class Sets available in pool for image " + getZosImage());
	}

	public boolean createUserid() {
		return createUserid;
	}

	public String getUseridDefaultGroup() throws ZosSecurityManagerException {
		if (useridDefaultGroup == null) {
			useridDefaultGroup = UseridDefaultGroup.get();
		}
		return useridDefaultGroup;
	}

	public List<String> getUseridGroups() throws ZosSecurityManagerException {
		if (useridDefaultGroups == null) {
			useridDefaultGroups = UseridDefaultGroups.get();
		}
		return useridDefaultGroups;
	}
}
