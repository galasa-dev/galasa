/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity;

import jakarta.xml.bind.DatatypeConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.MessageProp;

/**
 * This class represents the initiator of the kerberos security context. It is
 * created with a service, a client and a kdc. Requested properties, such as
 * mutual authentication and confidentiality can then be set before calling
 * {@link #create()} to create the security context. {@link #initiate()} will
 * then be called, (and potentially {@link #initiate(KerberosToken)} if further
 * initiation is required).
 */
public class KerberosInitiator {

	private static final Log logger = LogFactory.getLog(KerberosInitiator.class);

	private GSSContext context;

	private final IZosKerberosPrincipal service;
	private final IZosKerberosPrincipal client;

	private final String kdc;

	private boolean requestMutualAuth = false;
	private boolean requestConf = false;
	private boolean requestCredDeleg = false;

	/**
	 * Construct with service client and kdc
	 * 
	 * @param service
	 * @param client
	 * @param kdc
	 */
	//TODO
	public KerberosInitiator(IZosKerberosPrincipal service, IZosKerberosPrincipal client, String kdc) {

		this.service = service;
		this.client = client;

		this.kdc = kdc;
	}

	/**
	 * Returns true if mutual authentication has been requested
	 * 
	 * @return - true if mutual authentication has been requested
	 */
	public boolean isRequestMutualAuth() {
		return requestMutualAuth;
	}

	/**
	 * Set true to request mutual authentication
	 * 
	 * @param requestMutualAuth
	 */
	public void setRequestMutualAuth(boolean requestMutualAuth) {
		this.requestMutualAuth = requestMutualAuth;
	}

	/**
	 * Returns true if confidentiality has been requested
	 * 
	 * @return - true if confidentiality has been requested
	 */
	public boolean isRequestConf() {
		return requestConf;
	}

	/**
	 * Set true to request confidentiality
	 * 
	 * @param requestConf
	 */
	public void setRequestConf(boolean requestConf) {
		this.requestConf = requestConf;
	}

	/**
	 * Returns true if credential delegation has been requested
	 * 
	 * @return - true if credential delegation has been requested
	 */
	public boolean isRequestCredDeleg() {
		return requestCredDeleg;
	}

	/**
	 * Set true to request credential delegation
	 * 
	 * @param requestCredDeleg
	 */
	public void setRequestCredDeleg(boolean requestCredDeleg) {
		this.requestCredDeleg = requestCredDeleg;
	}

	/**
	 * Return true when the security context has been established
	 * 
	 * @return - true when the security context has been established
	 */
	public boolean isContextEstablished() {
		return context.isEstablished();
	}

	/**
	 * Create the security context by logging into the KDC as the client and
	 * populating the context from the subject
	 * 
	 * @throws ZosSecurityManagerException
	 */
	public void create() throws ZosSecurityManagerException {

		// Dispose in case this initiator has already been used
		dispose();

		// TODO: look for a threadsafe way of setting these
		System.setProperty("java.security.krb5.realm", service.getRealm());
		System.setProperty("java.security.krb5.kdc", kdc);
		System.setProperty("javax.security.auth.useSubjectCredsOnly", "true");

		// Create the JAAS login configuration and login to the KDC
		//TODO
//		Configuration config = new Krb5LoginConfiguration();

		logger.debug("Attempting login to the KDC at '" + kdc + "' as '"
				+ client.getPrincipalName() + "'");

		//TODO
//		LoginContext lgnCtx;
//		try {
//			lgnCtx = new LoginContext("null", null, new LoginCallbackHandler(
//					client.getPrincipalName(), client.getPassword()), config);
//			lgnCtx.login();
//		} catch (LoginException e) {
//			throw new ZosSecurityManagerException("Unable to login to the KDC", e);
//		}
//
//		// Create the context from the subject
//		this.context = Subject.doAs(lgnCtx.getSubject(),
//				new PrivilegedAction<GSSContext>() {
//					public GSSContext run() {
//
//						try {
//							// OIDs for the Krb5 mechanism, and for the
//							// principal name format
//							Oid krb5MechOid = new Oid("1.2.840.113554.1.2.2");
//							Oid krb5PrincNameOid = new Oid(
//									"1.2.840.113554.1.2.2.1");
//
//							// Obtain the GSS manager
//							GSSManager manager = GSSManager.getInstance();
//
//							// Create the client credential
//							GSSName clientName = manager.createName(
//									client.getPrincipalName(),
//									GSSName.NT_USER_NAME, krb5MechOid);
//							GSSCredential clientCred = manager
//									.createCredential(clientName,
//											GSSCredential.DEFAULT_LIFETIME,
//											krb5MechOid,
//											GSSCredential.INITIATE_ONLY);
//
//							// Create the server name
//							GSSName serverName = manager.createName(
//									service.getPrincipalName(),
//									krb5PrincNameOid);
//
//							// Create the context
//							GSSContext context = manager.createContext(
//									serverName, krb5MechOid, clientCred,
//									GSSContext.DEFAULT_LIFETIME);
//
//							// Set any requested properties
//							context.requestMutualAuth(requestMutualAuth);
//							context.requestCredDeleg(requestCredDeleg);
//							context.requestConf(requestConf);
//
//							return context;
//
//						} catch (GSSException e) {
//							logger.error(
//									"There was an error attempting to create the security context",
//									e);
//							return null;
//						}
//					}
//				});

		// Confirm that the context was actually created
		if (this.context == null) {
			throw new ZosSecurityManagerException(
					"The security context has not been created. Check preceding log entries for details of the error.");
		}
	}

	/**
	 * Initiate the security context without a token. This is equivalent to
	 * {@link #initiate(KerberosToken)} where the token is null, and is always
	 * the first called during initiation
	 * 
	 * @return - a Kerberos token
	 * @throws ZosSecurityManagerException
	 */
	public KerberosToken initiate() throws ZosSecurityManagerException {
		return initiate(null);
	}

	/**
	 * Initiate the security context with a token, or without if inToken is
	 * null. Initiate will always first be called without a token, and may be
	 * called with a token if the acceptor produces further tokens when
	 * accepting the security context. {@link #isContextEstablished()} can be
	 * called to determine whether further tokens are likely to be required to
	 * establish the context.
	 * 
	 * @param inToken
	 * @return - a Kerberos token
	 * @throws ZosSecurityManagerException
	 */
	public KerberosToken initiate(KerberosToken inToken)
			throws ZosSecurityManagerException {

		byte[] inBytes = new byte[0];
		if (inToken != null) {
			inBytes = inToken.getBytes();
		}

		byte[] tokenBytes;
		try {
			tokenBytes = context.initSecContext(inBytes, 0, inBytes.length);
		} catch (GSSException e) {
			throw new ZosSecurityManagerException(
					"Unable to initiate security context", e);
		}

		return new KerberosToken(tokenBytes);
	}

	/**
	 * This method accepts and returns base64 encoded messages. It will decode
	 * the input, call {@link #unwrap(byte[])}, encode the result and return it.
	 * 
	 * @param base64EncodedWrappedMessage
	 * @return - decoded string
	 * @throws ZosSecurityManagerException
	 */
	public String unwrap(String base64EncodedWrappedMessage)
			throws ZosSecurityManagerException {
		
		// TODO; remove. Only for debugging a particular issue
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			throw new ZosSecurityManagerException("Interrupted", e);
		}

		// decode
		byte[] wrappedMessage = DatatypeConverter
				.parseBase64Binary(base64EncodedWrappedMessage);
		
		// unwrap
		byte[] unwrappedMessage = unwrap(wrappedMessage);

		// encode
		return DatatypeConverter.printBase64Binary(unwrappedMessage);
	}

	/**
	 * This method will use the context to unwrap a message wrapped on the other
	 * side of the context
	 * 
	 * @param wrappedMessage
	 * @return - decoded bytes
	 * @throws ZosSecurityManagerException
	 */
	public byte[] unwrap(byte[] wrappedMessage) throws ZosSecurityManagerException {
		try {
			return context.unwrap(wrappedMessage, 0, wrappedMessage.length,
					new MessageProp(requestConf));
		} catch (GSSException e) {
			throw new ZosSecurityManagerException("Unable to unwrap message", e);
		}
	}

	/**
	 * Dispose of the underlying security context, releasing any associated
	 * resources.
	 * 
	 */
	public void dispose() {
		if (context != null) {
			try {
				context.dispose();
			} catch (GSSException e) {
				logger.error(
						"There was an error attempting to dispose of the security context. Processing will continue, but there may be issues if this initiator is re-used.",
						e);
			}
			context = null;
		}
	}
	
	@Override
	public void finalize() {
		dispose();
	}
}
