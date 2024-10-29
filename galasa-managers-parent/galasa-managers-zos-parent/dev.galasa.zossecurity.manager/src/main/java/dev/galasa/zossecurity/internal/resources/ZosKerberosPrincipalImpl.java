/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.zossecurity.IZosKerberosPrincipal;
import dev.galasa.zossecurity.IZosUserid;
import dev.galasa.zossecurity.ZosSecurityManagerException;
import dev.galasa.zossecurity.internal.ZosSecurityImpl;
import dev.galasa.zossecurity.internal.ZosSecurityImpl.ResourceType;

public class ZosKerberosPrincipalImpl implements IZosKerberosPrincipal {

	private final ZosSecurityImpl zosSecurity;
	
	private final String principalName;
	private final String realm;
	private final ZosUseridImpl user;
	private final String kerberosPrincipal;
	private final String sysplexId;
	private final String runName;

	private static final Log logger = LogFactory.getLog(ZosKerberosPrincipalImpl.class);

	protected ZosKerberosPrincipalImpl(ZosSecurityImpl zosSecurity, String principalName, String realm, IZosUserid user, String kerberosPrincipal) throws ZosSecurityManagerException {
		this.zosSecurity = zosSecurity;
		this.principalName = principalName;
		this.realm = realm;
		this.user = (ZosUseridImpl) user;
		this.kerberosPrincipal = kerberosPrincipal;
		this.sysplexId = zosSecurity.getZosImage().getSysplexID();
		this.runName = zosSecurity.getRunName();
	}

	public ZosKerberosPrincipalImpl(ZosSecurityImpl zosSecurity, String kerberosPrincipal, String sysplexId, String runName) {
		this.zosSecurity = zosSecurity;
		this.principalName = null;
		this.realm = null;
		this.user = null;
		this.kerberosPrincipal = kerberosPrincipal;
		this.sysplexId = sysplexId;
		this.runName = runName;
	}

	@Override
	public void free() throws ZosSecurityManagerException {
		zosSecurity.dssFree(ResourceType.ZOS_KERBEROS_PRINCIPAL.getName(), getResourceName());
		logger.debug("zOS Kerberos Principal '" + getResourceName() + "' was freed");
	}

	@Override
	public String getPrincipalName() {
		return this.principalName;
	}

	@Override
	public String getRealm() {
		return this.realm;
	}

	@Override
	public IZosUserid getUserid() {
		return this.user;
	}

	@Override
	public String getPassword() {
		return this.user.getPassword();
	}
	
	public static IZosKerberosPrincipal createPrincipal(ZosSecurityImpl zosSecurity, IZosUserid user, String realm) throws ZosSecurityManagerException {

		if (realm == null || realm.trim().isEmpty()) {
			throw new ZosSecurityManagerException("The realm is missing");
		}
		realm = realm.trim();
		
		ZosUseridImpl zosuser = (ZosUseridImpl) user;
		String principalName = generatePrincipalName(user);

		String kerberosPrincipal = principalName + "/" + realm + "/" + zosuser.getUserid();
		ZosKerberosPrincipalImpl principal = new ZosKerberosPrincipalImpl(zosSecurity, principalName, realm, zosuser, kerberosPrincipal);
		zosSecurity.dssRegister(ResourceType.ZOS_KERBEROS_PRINCIPAL.getName(), kerberosPrincipal);

		return principal;
	}
	
	public String getResourceName() {
		return this.principalName + "/" + this.realm + "/" + this.user.getUserid();
	}

	@Override
	public String toString() {
		return "[zOS Security Kerberos Principal] " + getResourceName();
	}

	public static String generatePrincipalName(IZosUserid user) {
		return user.getUserid().toLowerCase() + "_service";
	}

	public void delete() throws ZosSecurityManagerException {
		this.zosSecurity.dssUnregister(ResourceType.ZOS_KERBEROS_PRINCIPAL.getName(), kerberosPrincipal, sysplexId, runName);
	}
}
